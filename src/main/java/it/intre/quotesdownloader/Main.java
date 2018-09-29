package it.intre.quotesdownloader;

import it.intre.messagedispatcher.consumer.Consumer;
import it.intre.messagedispatcher.consumer.KafkaConsumer;
import it.intre.messagedispatcher.model.KafkaConfiguration;
import it.intre.messagedispatcher.model.KafkaRecord;
import it.intre.messagedispatcher.model.Record;
import it.intre.messagedispatcher.producer.KafkaProducer;
import it.intre.messagedispatcher.producer.Producer;
import it.intre.quotesdownloader.common.Constants;
import it.intre.quotesdownloader.downloader.IexQuoteDownloader;
import it.intre.quotesdownloader.downloader.QuoteDownloader;
import it.intre.quotesdownloader.model.Quote;
import org.apache.commons.cli.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;

public class Main {

    private static final Logger logger = LogManager.getLogger(Main.class);

    public static void main(String[] args) {
        CommandLine commandLine = getCommandLine(args);
        final String host = commandLine.getOptionValue("host");
        final String port = commandLine.getOptionValue("port");

        logger.info("Waiting for stocks...");
        List<String> stocksSymbols = new ArrayList<>();
        KafkaConfiguration inputConfiguration = new KafkaConfiguration(host, port, Constants.GROUP_ID, Constants.CLIENT_ID, Constants.INPUT_TOPIC);
        Consumer consumer = new KafkaConsumer<>(inputConfiguration, String.class, String.class);
        while (stocksSymbols.isEmpty()) {
            List<Record<String, String>> recordList = consumer.receive();
            for (Record<String, String> record : recordList) {
                String stockSymbol = record.getValue();
                stocksSymbols.add(stockSymbol);
                logger.info("Added stock: {}", stockSymbol);
            }
            consumer.commit();
        }
        logger.info("Stocks read");

        Map<String, Quote> quotesMap = new HashMap<>();
        QuoteDownloader quoteDownloader = new IexQuoteDownloader();
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
        return options;
    }

    private static boolean isNewQuote(Map<String, Quote> quotesMap, Quote quote) {
        return quote != null &&
                (
                        !quotesMap.containsKey(quote.getSymbol()) ||
                                !Objects.equals(quote.getTimestamp(), quotesMap.get(quote.getSymbol()).getTimestamp())
                );
    }

}
