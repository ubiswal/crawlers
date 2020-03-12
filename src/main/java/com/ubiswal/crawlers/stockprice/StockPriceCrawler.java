package com.ubiswal.crawlers.stockprice;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.SdkClientException;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.Bucket;
import com.amazonaws.services.s3.model.S3Object;

import java.net.*;
import java.io.*;

import org.apache.http.HttpException;

public class StockPriceCrawler {
    private String apiKey;
    private AmazonS3 s3Client;
    private List<String> stockSymbols;
    private String s3BucketName;

    public StockPriceCrawler(AmazonS3 s3Client, String apiKey, List<String> stockSymbols, String s3BucketName) {
        this.apiKey = apiKey;
        this.s3Client = s3Client;
        this.stockSymbols = stockSymbols;
        this.s3BucketName = s3BucketName;
    }

    public String sendGet(String stockSymbol) throws HttpException {

        URL urlForGetRequest = null;
        try {
            urlForGetRequest = new URL(String.format("https://www.alphavantage.co/query?function=TIME_SERIES_INTRADAY&symbol=%s&interval=5min&apikey=%s", stockSymbol, apiKey));
            String readLine = null;
            HttpURLConnection connection = null;
            connection = (HttpURLConnection) urlForGetRequest.openConnection();
            connection.setRequestMethod("GET");
            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                BufferedReader in = new BufferedReader(
                        new InputStreamReader(connection.getInputStream()));
                StringBuffer response = new StringBuffer();
                while ((readLine = in.readLine()) != null) {
                    response.append(readLine);
                }
                in.close();
                return response.toString();

            } else {
                throw new HttpException("GET REQUEST DID NOT WORK!! Got a response code of " + responseCode);
            }
        } catch (IOException e) {
            throw new HttpException("GET REQUEST DID NOT WORK!! " + e.getMessage());
        }
    }

    private void uploadToS3(String s3KeyName, String content) throws HttpException {
        try {
            s3Client.putObject(s3BucketName, s3KeyName, content);
        } catch (SdkClientException e) {
            throw new HttpException("Failed to upload to s3 because " + e.getMessage());
        }
    }

    public void collectStockPricesForAll() {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        Date date = new Date();
        Calendar calendar = GregorianCalendar.getInstance(); // creates a new calendar instance
        calendar.setTime(date);   // assigns calendar to given date
        int hour = calendar.get(Calendar.HOUR_OF_DAY); // gets hour in 24h format

        String rootFolderName = formatter.format(date);
        for (String symbol : stockSymbols) {
            try {
                String content = sendGet(symbol);
                String s3FileName = String.format("%s/%s/%s/stock.json", rootFolderName, hour, symbol);
                uploadToS3(s3FileName, content);
            } catch (HttpException e) {
                System.out.println("Failed while uploading  stocks for " + symbol + " because " + e.getMessage());
            }
        }
    }

}
