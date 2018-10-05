package it.intre.quotesdownloader.downloader;

import it.intre.quotesdownloader.model.Quote;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class FileQuoteDownloader extends QuoteDownloader {

    private Map<String, Quote> oldQuotesMap;
    private Map<String, ArrayList<String>> linesMap;

    public FileQuoteDownloader(int interval) {
        super(interval);
        this.oldQuotesMap = new HashMap<>();
        this.linesMap = new HashMap<>();
    }

    public Quote getQuote(final String symbol) {
        try {
            this.logger.trace("Getting quote for {}...", symbol);
            Quote quote;
            Quote oldQuote = this.oldQuotesMap.getOrDefault(symbol, null);
            Long currentTimestamp = this.getIntervalTimestamp();
            if (oldQuote != null && currentTimestamp.equals(oldQuote.getTimestamp())) {
                quote = oldQuote;
            } else {
                quote = this.getFileQuote(symbol, currentTimestamp);
                this.oldQuotesMap.put(symbol, quote);
            }
            this.logger.trace("Got quote: {}", quote);
            return quote;
        } catch (Exception e) {
            this.logger.error("getQuote exception:", e);
            return null;
        }
    }

    private Quote getFileQuote(final String symbol, final Long currentTimestamp) throws IOException {
        Quote quote = null;
        if (!this.linesMap.containsKey(symbol)) {
            this.readFile(symbol);
        }
        ArrayList<String> lines = this.linesMap.get(symbol);
        if (!lines.isEmpty()) {
            String line = lines.get(0);
            lines.remove(0);
            quote = this.parseLine(line);
            quote.setSymbol(symbol);
            quote.setTimestamp(currentTimestamp);
        }
        return quote;
    }

    private void readFile(final String symbol) throws IOException {
        ArrayList<String> lines = new ArrayList<>();
        try (BufferedReader bufferedReader = new BufferedReader(new FileReader("./data/" + symbol + ".csv"))) {
            String line = bufferedReader.readLine();
            while (line != null) {
                lines.add(line);
                line = bufferedReader.readLine();
            }
            this.linesMap.put(symbol, lines);
        } catch (Exception e) {
            this.logger.error("readFile exception:", e);
            throw e;
        }
    }

    private Quote parseLine(final String line) {
        Quote quote = new Quote();
        String[] lineParts = line.split(",");
        BigDecimal price = new BigDecimal(lineParts[0]);
        BigDecimal volume = new BigDecimal(lineParts[1]);
        quote.setPrice(price);
        quote.setVolume(volume);
        return quote;
    }

}
