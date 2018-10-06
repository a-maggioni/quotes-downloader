package it.intre.quotesdownloader.downloader;

import it.intre.quotesdownloader.model.Quote;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.time.Instant;

public abstract class QuoteDownloader {

    protected final Logger logger = LogManager.getLogger(QuoteDownloader.class);

    private Long intervalMillis;

    protected QuoteDownloader() {
    }

    protected QuoteDownloader(int interval) {
        this.intervalMillis = interval * 1000L;
    }

    public abstract Quote getQuote(final String symbol);

    protected Long getIntervalTimestamp() {
        return (Instant.now().toEpochMilli() / this.intervalMillis) * this.intervalMillis;
    }

}
