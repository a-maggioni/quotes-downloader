package it.intre.quotesdownloader.downloader;

import it.intre.quotesdownloader.model.Quote;
import pl.zankowski.iextrading4j.client.IEXTradingClient;
import pl.zankowski.iextrading4j.client.rest.request.stocks.QuoteRequestBuilder;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

public class IexQuoteDownloader extends QuoteDownloader {

    private Map<String, Quote> oldQuotesMap;
    private Map<String, BigDecimal> oldTotalVolumesMap;
    private IEXTradingClient iexTradingClient;

    public IexQuoteDownloader() {
        super();
        this.oldQuotesMap = new HashMap<>();
        this.oldTotalVolumesMap = new HashMap<>();
        this.iexTradingClient = IEXTradingClient.create();
    }

    public Quote getQuote(final String symbol) {
        try {
            this.logger.trace("Getting quote for {}...", symbol);
            Quote quote;
            Quote oldQuote = this.oldQuotesMap.getOrDefault(symbol, null);
            pl.zankowski.iextrading4j.api.stocks.Quote iexQuote = this.iexTradingClient.executeRequest(new QuoteRequestBuilder().withSymbol(symbol).build());
            if (oldQuote != null && iexQuote.getLatestUpdate() <= oldQuote.getTimestamp()) {
                quote = oldQuote;
            } else {
                BigDecimal currentTotalVolume = iexQuote.getLatestVolume();
                BigDecimal oldTotalVolume = this.oldTotalVolumesMap.getOrDefault(symbol, currentTotalVolume);
                BigDecimal volume;
                // TODO: check why sometimes the current total volume is smaller than the previous one
                if (currentTotalVolume.compareTo(oldTotalVolume) >= 0) {
                    volume = currentTotalVolume.subtract(oldTotalVolume);
                    this.oldTotalVolumesMap.put(symbol, currentTotalVolume);
                } else {
                    volume = BigDecimal.ZERO;
                }
                quote = new Quote(symbol, iexQuote.getLatestPrice(), volume, iexQuote.getLatestUpdate());
                this.oldQuotesMap.put(symbol, quote);
            }
            this.logger.trace("Got quote: {}", quote);
            return quote;
        } catch (Exception e) {
            this.logger.error("getQuote exception:", e);
            return null;
        }
    }

}
