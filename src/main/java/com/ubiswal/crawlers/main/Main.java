package com.ubiswal.crawlers.main;
import com.ubiswal.crawlers.stockprice.StockPriceCrawler;
import java.util.ArrayList;

public class Main {
    public static void main(String args[]) {
        System.out.println("Hello world!");
        ArrayList <String> symbols = new ArrayList<>();
        symbols.add("INTC");
        StockPriceCrawler s = new StockPriceCrawler(symbols);
    }
}
