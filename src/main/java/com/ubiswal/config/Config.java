package com.ubiswal.config;
import java.util.List;

public class Config {
    private String apiKey;
    private List<String> stockSymbols;

    public String getApiKey(){
        return apiKey;
    }

    public List<String> getStockSymbols(){
        return stockSymbols;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    public void setStockSymbols(List<String> stockSymbols) {
        this.stockSymbols = stockSymbols;
    }
}
