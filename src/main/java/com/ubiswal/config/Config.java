package com.ubiswal.config;

import java.util.List;

public class Config {
    private String apiKey;// alpha vantage api key
    private List<String> stockSymbols;
    private String newsApiKey; //newsapi api key

    public String getApiKey() {
        return apiKey;
    }

    public List<String> getStockSymbols() {
        return stockSymbols;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    public void setStockSymbols(List<String> stockSymbols) {
        this.stockSymbols = stockSymbols;
    }

    public String getNewsApiKey() {
        return this.newsApiKey;
    }

    public void setNewsApiKey(String newsApiKey) {
        this.newsApiKey = newsApiKey;
    }


}
