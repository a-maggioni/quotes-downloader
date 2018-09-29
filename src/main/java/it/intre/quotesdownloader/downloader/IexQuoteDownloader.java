package it.intre.quotesdownloader.downloader;

import it.intre.quotesdownloader.model.Quote;
import pl.zankowski.iextrading4j.client.IEXTradingClient;
import pl.zankowski.iextrading4j.client.rest.request.stocks.QuoteRequestBuilder;

public class IexQuoteDownloader extends QuoteDownloader {

    private IEXTradingClient iexTradingClient;

    public IexQuoteDownloader() {
        this.iexTradingClient = IEXTradingClient.create();
    }

    public Quote getQuote(final String symbol) {
        try {
            this.logger.trace("Getting quote for {}...", symbol);
            pl.zankowski.iextrading4j.api.stocks.Quote iexQuote = this.iexTradingClient.executeRequest(new QuoteRequestBuilder().withSymbol(symbol).build());
            Quote quote = new Quote(symbol, iexQuote.getLatestPrice(), iexQuote.getLatestVolume(), iexQuote.getLatestUpdate());
            this.logger.trace("Got quote: {}", quote);
            return quote;
        } catch (Exception e) {
            this.logger.error("getQuote exception:", e);
            return null;
        }
    }

}
