package it.intre.quotesdownloader.model;

import java.math.BigDecimal;
import java.util.Objects;

public class Quote {

    private String symbol;
    private BigDecimal price;
    private BigDecimal volume;
    private Long timestamp;

    public Quote() {
    }

    public Quote(String symbol, BigDecimal price, BigDecimal volume, Long timestamp) {
        this.symbol = symbol;
        this.price = price;
        this.volume = volume;
        this.timestamp = timestamp;
    }

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public BigDecimal getVolume() {
        return volume;
    }

    public void setVolume(BigDecimal volume) {
        this.volume = volume;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Quote)) return false;
        Quote quote = (Quote) o;
        return Objects.equals(getSymbol(), quote.getSymbol()) &&
                Objects.equals(getPrice(), quote.getPrice()) &&
                Objects.equals(getVolume(), quote.getVolume()) &&
                Objects.equals(getTimestamp(), quote.getTimestamp());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getSymbol(), getPrice(), getVolume(), getTimestamp());
    }

    @Override
    public String toString() {
        return "Quote{" +
                "symbol=" + symbol +
                ", price=" + price +
                ", volume=" + volume +
                ", timestamp=" + timestamp +
                '}';
    }
}
