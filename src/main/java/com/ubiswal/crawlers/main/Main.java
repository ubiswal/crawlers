package com.ubiswal.crawlers.main;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.ubiswal.crawlers.stockprice.StockPriceCrawler;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.http.HttpException;


public class Main {
    public static void main(String args[]) throws HttpException {
        List<String> symbols = Arrays.asList("INTC", "MSFT");
        // Crete S3 client
        final AmazonS3 s3 = AmazonS3ClientBuilder.standard().withRegion(Regions.US_EAST_1).build();

        StockPriceCrawler s = new StockPriceCrawler(s3, "2Q4MMOXUEBV2E9XY", symbols, "stocks-testing");
        s.collectStockPricesForAll();
    }
}
