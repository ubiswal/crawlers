package com.ubiswal.crawlers.stockprice;

import java.text.SimpleDateFormat;
import java.util.*;

import com.amazonaws.SdkClientException;
import com.amazonaws.services.s3.AmazonS3;

import java.net.*;
import java.io.*;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ubiswal.utils.MiscUtils;
import lombok.Getter;
import lombok.Setter;
import org.apache.http.HttpException;

@JsonIgnoreProperties(ignoreUnknown = true)
class TimeSeriesEntry {
    @Getter
    @Setter
    @JsonProperty("1. open")
    private float open;
    @Getter
    @Setter
    @JsonProperty("2. high")
    private float high;
    @Getter
    @Setter
    @JsonProperty("3. low")
    private float low;
    @Getter
    @Setter
    @JsonProperty("4. close")
    private float close;
    @Getter
    @Setter
    @JsonProperty("5. volume")
    private int volume;

}

@JsonIgnoreProperties(ignoreUnknown = true)
class StockPrices {
    //private String symbol;
    @Getter
    @Setter
    @JsonProperty("Time Series (5min)")
    private Map<String, TimeSeriesEntry> timeSeriesEntries;
}


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

    public String getStockPriceForSymbol(String stockSymbol) throws HttpException {
        URL urlForGetRequest;
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
            System.out.println(String.format("Uploading to %s: %s", s3BucketName, s3KeyName));
            s3Client.putObject(s3BucketName, s3KeyName, content);
        } catch (SdkClientException e) {
            throw new HttpException("Failed to upload to s3 because " + e.getMessage());
        }
    }

    public void collectStockPricesForAll() {
        List<List<String>> batches = MiscUtils.partition(stockSymbols, 5);
        for (List<String> batch : batches) {
            for (String symbol : batch) {
                try {
                    String content = getStockPriceForSymbol(symbol);
                    // The parsing into json is purely for validation purposes
                    ObjectMapper mapper = new ObjectMapper();
                    mapper.readValue(content, StockPrices.class);
                    String s3FileName = MiscUtils.getS3FolderPath(symbol, "stock.json");
                    uploadToS3(s3FileName, content);
                } catch (HttpException e) {
                    System.out.println("Failed while uploading  stocks for " + symbol + " because " + e.getMessage());
                } catch (JsonParseException e) {
                    System.out.println(String.format("Failed to parse downloaded response for symbol %s", symbol));
                    e.printStackTrace();
                } catch (JsonMappingException e) {
                    System.out.println(String.format("Failed to parse downloaded response for symbol %s", symbol));
                    e.printStackTrace();
                } catch (IOException e) {
                    System.out.println(String.format("Failed to parse downloaded response for symbol %s", symbol));
                    e.printStackTrace();
                }
            }
            try {
                System.out.println(String.format("Made calls for %s. Going to sleep for 2 minutes", batch));
                Thread.sleep(120 * 1000, 0);
            } catch (InterruptedException e) {
                System.out.println("[WARN] Interrupted while sleeping");
            }
        }
        System.out.println("Done.");
    }

}
