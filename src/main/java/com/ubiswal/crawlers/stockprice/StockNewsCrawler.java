package com.ubiswal.crawlers.stockprice;


import com.amazonaws.SdkClientException;
import com.amazonaws.services.s3.AmazonS3;
import org.apache.http.HttpException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.*;

public class StockNewsCrawler {
    private String apiKey;
    private AmazonS3 s3Client;
    private Map<String, String> stockSymbols;
    private String s3BucketName;

    public StockNewsCrawler(AmazonS3 s3Client, String apiKey, Map<String, String> stockSymbols, String s3BucketName) {
        this.apiKey = apiKey;
        this.s3Client = s3Client;
        this.stockSymbols = stockSymbols;
        this.s3BucketName = s3BucketName;
    }

    public String sendGet(final String stockSymbol, final String fromDate, final String toDate) throws HttpException {

        URL urlForGetRequest = null;
        try {
            urlForGetRequest = new URL(String.format("http://newsapi.org/v2/everything?q=%s&from=%s&to=%s&sortBy=popularity&apiKey=%s", stockSymbol, fromDate, toDate, apiKey));
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

    public void collectStockNewsForAll() {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        Date date = new Date();
        String rootFolderName = formatter.format(date);
        String newsDate = formatter.format(date);
        Calendar calendar = GregorianCalendar.getInstance(); // creates a new calendar instance
        calendar.setTime(date);   // assigns calendar to given date
        int hour = calendar.get(Calendar.HOUR_OF_DAY); // gets hour in 24h format

        for (Map.Entry<String, String> symbol : stockSymbols.entrySet()) {
            try {
                String content = sendGet(symbol.getValue(), newsDate, newsDate);
                String s3FileName = String.format("%s/%s/%s/news.json", rootFolderName, hour, symbol.getKey());
                uploadToS3(s3FileName, content);
            } catch (HttpException e) {
                System.out.println("Failed while uploading  stocks for " + symbol + " because " + e.getMessage());
            }
        }
    }

}

