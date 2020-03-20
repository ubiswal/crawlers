package com.ubiswal.main;
import com.amazonaws.SdkClientException;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectInputStream;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ubiswal.config.Config;
import com.ubiswal.crawlers.stockprice.StockNewsCrawler;
import com.ubiswal.crawlers.stockprice.StockPriceCrawler;

import java.io.File;
import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.commons.io.FileUtils;
import org.apache.http.HttpException;

class CrawlerCron extends TimerTask {
    private final StockPriceCrawler stockPriceCrawler;
    private final StockNewsCrawler stockNewsCrawler;
    CrawlerCron(StockPriceCrawler stockPriceCrawler, StockNewsCrawler stockNewsCrawler){
        this.stockPriceCrawler = stockPriceCrawler;
        this.stockNewsCrawler = stockNewsCrawler;
    }

    @Override
    public void run() {
        stockPriceCrawler.collectStockPricesForAll();
        stockNewsCrawler.collectStockNewsForAll();
    }
}

public class Main {
    private static final String BUCKETNAME = "stocks-testing" ;

    public static void main(String args[]) throws HttpException, IOException {
        // Crete S3 client
        final AmazonS3 s3 = AmazonS3ClientBuilder.standard().withRegion(Regions.US_EAST_1).build();
        Config cfg = getConfig(s3);

        StockPriceCrawler s = new StockPriceCrawler(s3, cfg.getApiKey(), cfg.getStockSymbols(), BUCKETNAME );
        StockNewsCrawler n = new StockNewsCrawler(s3, cfg.getNewsApiKey(), cfg.getStockNewsSearchStrings(), BUCKETNAME);

        CrawlerCron crawlerCron = new CrawlerCron(s, n);

        Timer timer = new Timer();
        timer.schedule(crawlerCron, 0, 7200000);
    }

    private static Config getConfig(final AmazonS3 s3Client) throws IOException {
        try {
            S3Object s3obj = s3Client.getObject(BUCKETNAME , "config.json");
            S3ObjectInputStream inputStream = s3obj.getObjectContent();
            FileUtils.copyInputStreamToFile(inputStream, new File("config.json"));
        } catch (SdkClientException e) {
            System.out.println("Failed to download config file from s3 because " + e.getMessage());
            throw e;
        } catch (IOException e) {
            System.out.println("Failed to save downloaded config file from s3 because " + e.getMessage());
            throw e;
        }

        //use jackson for json to class conversion for the Config
        ObjectMapper mapper = new ObjectMapper();
        try {
            // JSON file to Java object
            Config config = mapper.readValue(new File("config.json"), Config.class);
            return config;
        } catch (IOException e) {
            System.out.println("Failed to read the downloaded config file from s3 because " + e.getMessage());
            throw e;
        }

    }

}
