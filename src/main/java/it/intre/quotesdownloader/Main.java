package it.intre.quotesdownloader;

import it.intre.messagedispatcher.consumer.Consumer;
import it.intre.messagedispatcher.consumer.KafkaConsumer;
import it.intre.messagedispatcher.model.KafkaConfiguration;
import it.intre.messagedispatcher.model.KafkaRecord;
import it.intre.messagedispatcher.model.Record;
import it.intre.messagedispatcher.producer.KafkaProducer;
import it.intre.messagedispatcher.producer.Producer;
import it.intre.quotesdownloader.common.Constants;
import it.intre.quotesdownloader.downloader.FileQuoteDownloader;
import it.intre.quotesdownloader.downloader.IexQuoteDownloader;
import it.intre.quotesdownloader.downloader.QuoteDownloader;
import it.intre.quotesdownloader.downloader.RandomQuoteDownloader;
import it.intre.quotesdownloader.model.DownloaderType;
import it.intre.quotesdownloader.model.Quote;
import org.apache.commons.cli.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;

public class Main {

    private static final Logger logger = LogManager.getLogger(Main.class);

    public static void main(String[] args) throws ParseException {
        CommandLine commandLine = getCommandLine(args);
        final String host = commandLine.getOptionValue("host");
        final String port = commandLine.getOptionValue("port");
        final int type = ((Number) commandLine.getParsedOptionValue("type")).intValue();
        final DownloaderType downloaderType = DownloaderType.values()[type];
        final int interval = commandLine.hasOption("interval") ?
                ((Number) commandLine.getParsedOptionValue("interval")).intValue() : Constants.DEFAULT_INTERVAL;

        List<String> stocksSymbols = readStocks(host, port);
        Map<String, Quote> quotesMap = new HashMap<>();
        QuoteDownloader quoteDownloader = getDownloader(downloaderType, interval);
        KafkaConfiguration outputConfiguration = new KafkaConfiguration(host, port, Constants.GROUP_ID, Constants.CLIENT_ID, Constants.OUTPUT_TOPIC);
        Producer producer = new KafkaProducer<String, Quote>(outputConfiguration);
        while (true) {
            for (String stockSymbol : stocksSymbols) {
                Quote quote = quoteDownloader.getQuote(stockSymbol);
                if (isNewQuote(quotesMap, quote)) {
                    Record record = new KafkaRecord<>(Constants.OUTPUT_TOPIC, stockSymbol, quote);
                    boolean success = producer.send(record);
                    if (success) {
                        logger.debug("Sent quote: {}", quote);
                        quotesMap.put(stockSymbol, quote);
                    }
                }
            }
        }
    }

    private static List<String> readStocks(final String host, final String port) {
        logger.info("Reading stocks...");
        List<String> stocksSymbols = new ArrayList<>();
        KafkaConfiguration inputConfiguration = new KafkaConfiguration(host, port, Constants.GROUP_ID, Constants.CLIENT_ID, Constants.INPUT_TOPIC);
        Consumer consumer = new KafkaConsumer<>(inputConfiguration, String.class, String.class);
        List<Record<String, String>> recordList = new ArrayList<>();
        while (stocksSymbols.isEmpty() || !recordList.isEmpty()) {
            recordList = consumer.receive();
            for (Record<String, String> record : recordList) {
                String stockSymbol = record.getValue();
                stocksSymbols.add(stockSymbol);
                logger.info("Added stock: {}", stockSymbol);
            }
            consumer.commit();
        }
        logger.info("Stocks read");
        return stocksSymbols;
    }

    private static QuoteDownloader getDownloader(final DownloaderType downloaderType, final int interval) {
        if (downloaderType.equals(DownloaderType.IEX)) {
            return new IexQuoteDownloader();
        } else if (downloaderType.equals(DownloaderType.FILE)) {
            return new FileQuoteDownloader(interval);
        }
        return new RandomQuoteDownloader(interval);
    }

    private static boolean isNewQuote(Map<String, Quote> quotesMap, Quote quote) {
        return quote != null &&
                (
                        !quotesMap.containsKey(quote.getSymbol()) ||
                                quote.getTimestamp() > quotesMap.get(quote.getSymbol()).getTimestamp()
                );
    }

    private static CommandLine getCommandLine(String[] args) {
        CommandLineParser commandLineParser = new DefaultParser();
        HelpFormatter helpFormatter = new HelpFormatter();
        Options options = getOptions();
        CommandLine commandLine = null;
        try {
            commandLine = commandLineParser.parse(options, args);
        } catch (ParseException e) {
            helpFormatter.printHelp("QuotesDownloader", options);
            System.exit(1);
        }
        return commandLine;
    }

    private static Options getOptions() {
        Options options = new Options();
        Option host = new Option("h", "host", true, "Kafka host");
        host.setRequired(true);
        options.addOption(host);
        Option port = new Option("p", "port", true, "Kafka port");
        port.setRequired(true);
        options.addOption(port);
        Option type = new Option("t", "type", true, "Downloader type");
        type.setType(Number.class);
        type.setRequired(true);
        options.addOption(type);
        Option interval = new Option("i", "interval", true, "Downloader interval (seconds)");
        interval.setType(Number.class);
        options.addOption(interval);
        return options;
    }

}
