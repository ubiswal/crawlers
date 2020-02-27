package com.ubiswal.crawlers.stockprice;
import java.util.List;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.Bucket;
import com.amazonaws.services.s3.model.S3Object;

public class StockPriceCrawler {
    public StockPriceCrawler(List <String> stockSymbols){
        final AmazonS3 s3 = AmazonS3ClientBuilder.standard().withRegion(Regions.DEFAULT_REGION).build();

        List<Bucket> buckets = s3.listBuckets();
        System.out.println("Your Amazon S3 buckets are:");
        for (Bucket b : buckets) {
            System.out.println("* " + b.getName());
        }
        S3Object stringObject = new S3Object("HelloWorld.txt", "Hello World!");
        s3.putObject("stocks_testing", stringObject);
    }
}
