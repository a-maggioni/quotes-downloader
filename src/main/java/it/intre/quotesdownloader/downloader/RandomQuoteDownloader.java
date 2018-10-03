package it.intre.quotesdownloader.downloader;

import it.intre.quotesdownloader.model.Quote;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

public class RandomQuoteDownloader extends QuoteDownloader {

    private Map<String, Quote> oldQuotesMap;
    private IexQuoteDownloader iexQuoteDownloader;

    public RandomQuoteDownloader() {
        this.oldQuotesMap = new HashMap<>();
        this.iexQuoteDownloader = new IexQuoteDownloader();
    }

    public Quote getQuote(final String symbol) {
        try {
            this.logger.trace("Getting quote for {}...", symbol);
            Quote oldQuote = this.oldQuotesMap.containsKey(symbol) ? this.oldQuotesMap.get(symbol) : this.iexQuoteDownloader.getQuote(symbol);
            Long currentTimestamp = Instant.now().getEpochSecond();
            if (currentTimestamp.equals(oldQuote.getTimestamp())) {
                return oldQuote;
            } else {
                Quote quote = new Quote(symbol, this.getRandomPrice(oldQuote), this.getRandomVolume(oldQuote), currentTimestamp);
                this.oldQuotesMap.put(symbol, quote);
                this.logger.trace("Got quote: {}", quote);
                return quote;
            }
        } catch (Exception e) {
            this.logger.error("getQuote exception:", e);
            return null;
        }
    }

    private BigDecimal getRandomPrice(final Quote quote) {
        return this.getRandomValue(quote.getPrice(), false,2);
    }

    private BigDecimal getRandomVolume(final Quote quote) {
        return this.getRandomValue(quote.getVolume(), true, 0);
    }

    private BigDecimal getRandomValue(final BigDecimal oldValue, final boolean alwaysGrowing, final int scale) {
        double volatility = Math.random() * 1.2;
        double rawPercentageVariation = (alwaysGrowing || Math.random() >= 0.5) ? Math.random() : -Math.random();
        double percentageVariation = rawPercentageVariation * volatility;
        double variation = oldValue.doubleValue() * percentageVariation / 100;
        double newValue = oldValue.doubleValue() + variation;
        return new BigDecimal(newValue).setScale(scale, BigDecimal.ROUND_HALF_UP);
    }

}
