package it.intre.quotesdownloader.downloader;

import it.intre.quotesdownloader.model.Quote;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public abstract class QuoteDownloader {

    protected final Logger logger = LogManager.getLogger(QuoteDownloader.class);

    public abstract Quote getQuote(final String symbol);

}
