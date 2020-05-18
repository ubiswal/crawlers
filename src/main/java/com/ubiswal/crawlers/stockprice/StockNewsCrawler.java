package com.ubiswal.crawlers.stockprice;


import com.amazonaws.SdkClientException;
import com.amazonaws.services.s3.AmazonS3;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ubiswal.utils.MiscUtils;
import lombok.Getter;
import lombok.Setter;
import org.apache.http.HttpException;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.*;

@JsonIgnoreProperties(ignoreUnknown = true)
class Source{
    @Getter
    @Setter
    private String name;
}
@JsonIgnoreProperties(ignoreUnknown = true)
class Article{
    @Getter
    @Setter
    private Source source;
    @Getter
    @Setter
    private String author;
    @Getter
    @Setter
    private String description;
    @Getter
    @Setter
    private String url;
    @Getter
    @Setter
    private String urlToImage;
}

@JsonIgnoreProperties(ignoreUnknown = true)
class ListOfArticles{
    @Getter
    @Setter
    private List<Article> articles;
}

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
            urlForGetRequest = new URL(String.format("http://newsapi.org/v2/everything?q=%s&from=%s&to=%s&language=en&sortBy=popularity&apiKey=%s", stockSymbol, fromDate, toDate, apiKey));
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
            System.out.println(String.format("Uploading %s to S3", s3KeyName));
            s3Client.putObject(s3BucketName, s3KeyName, content);
        } catch (SdkClientException e) {
            throw new HttpException("Failed to upload to s3 because " + e.getMessage());
        }
    }

    public void collectStockNewsForAll() {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        Date date = new Date();
        formatter.setTimeZone(TimeZone.getTimeZone("UTC"));
        String newsDate = formatter.format(date);
        Calendar calendar = GregorianCalendar.getInstance(); // creates a new calendar instance
        calendar.setTime(date);   // assigns calendar to given date

        List<List<String>> batches = MiscUtils.partition(new ArrayList<>(stockSymbols.keySet()), 5);
        for (List<String> batch : batches) {
            for (String symbol : batch) {
                try {
                    String content = sendGet(stockSymbols.get(symbol), newsDate, newsDate);
                    // The parsing to json is purely for validation purposes
                    ObjectMapper mapper = new ObjectMapper();
                    mapper.readValue(content, ListOfArticles.class);
                    String s3FileName = MiscUtils.getS3FolderPath(date, symbol, "news.json");
                    uploadToS3(s3FileName, content);
                } catch (HttpException e) {
                    System.out.println("Failed while uploading  stocks for " + symbol + " because " + e.getMessage());
                } catch (JsonParseException e) {
                    System.out.println(String.format("Failed to parse downloaded news for symbol %s", symbol));
                    e.printStackTrace();
                } catch (JsonMappingException e) {
                    System.out.println(String.format("Failed to parse downloaded news for symbol %s", symbol));
                    e.printStackTrace();
                } catch (IOException e) {
                    System.out.println(String.format("Failed to parse downloaded news for symbol %s", symbol));
                    e.printStackTrace();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            try {
                System.out.println(String.format("Fetched news for %s. Going to sleep for 2 mins.", batch));
                Thread.sleep(120*1000, 0);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}

