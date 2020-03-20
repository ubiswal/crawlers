# Stock Data Crawler
This repo includes code for crawlers that collect data for stock prices and related news for some companies. The list of companies is configurable.

## Sources
  - **News** source is newsapi.org.
  - **Stock prices** source is alphavantage.co .

## Build and Run Steps
To build run the following command from the root directory:

`mvn clean compile assembly:single`

To run locally, use the following command:

`java -jar target/crawlers-0.0.1-jar-with-dependencies.jar`

The configuration and the data reside in an s3 bucket.
Following is a sample configuration file.
```json
{
    "apiKey" : "ALPHAVANTAGE API-KEY",
    "stockSymbols" : ["MSFT", "AAPL", "INTC"],
    "newsApiKey" : "NEWSAPIKEY",
    "stockNewsSearchStrings" : {
        "MSFT":"microsoft",
        "AAPL" : "apple",
        "INTC" : "intel"
    }
}
```
  
## Details
The cron runs every two hours and saves the files in s3.
The file hierarchy looks like this:
```
BUCKET/STOCK_SYMBOL/DATE/HH/news.json
BUCKET/STOCK_SYMBOL/DATE/HH/stock.json

```