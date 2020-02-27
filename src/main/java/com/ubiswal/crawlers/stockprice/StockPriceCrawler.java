package com.ubiswal.crawlers.stockprice;

import java.util.List;

import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.Bucket;
import com.amazonaws.services.s3.model.S3Object;

import java.net.*;
import java.io.*;

import org.apache.http.HttpException;

public class StockPriceCrawler {
    public StockPriceCrawler(List<String> stockSymbols) {
        final AmazonS3 s3 = AmazonS3ClientBuilder.standard().withRegion(Regions.DEFAULT_REGION).build();
        List<Bucket> buckets = s3.listBuckets();
        System.out.println("Your Amazon S3 buckets are:");
        for (Bucket b : buckets) {
            System.out.println("* " + b.getName());
        }
    }

    public String sendGet() throws HttpException {

        URL urlForGetRequest = null;
        try {
            urlForGetRequest = new URL("https://www.alphavantage.co/query?function=TIME_SERIES_INTRADAY&symbol=INTC&interval=5min&apikey=2Q4MMOXUEBV2E9XY");

            String readLine = null;
            HttpURLConnection connection = null;
            connection = (HttpURLConnection) urlForGetRequest.openConnection();
            connection.setRequestMethod("GET");
            //connection.setRequestProperty("userId", "a1bcdef"); // set userId its a sample here
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
}
