package it.intre.quotesdownloader.downloader;

import it.intre.quotesdownloader.model.Quote;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

public class RandomQuoteDownloader extends QuoteDownloader {

    private Map<String, Quote> oldQuotesMap;
    private IexQuoteDownloader iexQuoteDownloader;

    public RandomQuoteDownloader(int interval) {
        super(interval);
        this.oldQuotesMap = new HashMap<>();
        this.iexQuoteDownloader = new IexQuoteDownloader();
    }

    public Quote getQuote(final String symbol) {
        try {
            this.logger.trace("Getting quote for {}...", symbol);
            Quote quote;
            Quote oldQuote = this.oldQuotesMap.containsKey(symbol) ?
                    this.oldQuotesMap.get(symbol) :
                    this.iexQuoteDownloader.getQuote(symbol);
            Long currentTimestamp = this.getIntervalTimestamp();
            if (currentTimestamp.equals(oldQuote.getTimestamp())) {
                quote = oldQuote;
            } else {
                quote = new Quote(
                        symbol,
                        this.getRandomValue(oldQuote.getPrice(), 2),
                        this.getRandomValue(new BigDecimal(10000), 0),
                        currentTimestamp
                );
                this.oldQuotesMap.put(symbol, quote);
            }
            this.logger.trace("Got quote: {}", quote);
            return quote;
        } catch (Exception e) {
            this.logger.error("getQuote exception:", e);
            return null;
        }
    }

    private BigDecimal getRandomValue(final BigDecimal oldValue, final int scale) {
        double volatility = Math.random() * 1.2;
        double rawPercentageVariation = Math.random() >= 0.5 ? Math.random() : -Math.random();
        double percentageVariation = rawPercentageVariation * volatility;
        double variation = oldValue.doubleValue() * percentageVariation / 100;
        double newValue = oldValue.doubleValue() + variation;
        return new BigDecimal(newValue).setScale(scale, BigDecimal.ROUND_HALF_UP);
    }

}
