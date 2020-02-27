package com.ubiswal.crawlers.main;
import com.ubiswal.crawlers.stockprice.StockPriceCrawler;
import java.io.IOException;
import java.util.ArrayList;
import org.apache.http.HttpException;


public class Main {
    public static void main(String args[]) throws HttpException {
        System.out.println("Hello world!");
        ArrayList <String> symbols = new ArrayList<>();
        symbols.add("INTC");
        StockPriceCrawler s = new StockPriceCrawler(symbols);
        s.sendGet();
    }
}
