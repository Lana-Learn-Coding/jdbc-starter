package io.lana.sqlstarter.app.product;

import java.text.DecimalFormat;

public class Product {
    public static final String BORDER = "+-------+-----------------+--------------+----------+----------+-----+";

    public static final String FORMAT = "| %-5s | %-15s | %-12s | %-8s | %-8s | %-3s |";

    public static final DecimalFormat PRICE_FORMATTER = new DecimalFormat("#.00");

    private Integer code;

    private String name;

    private String producer;

    private Integer quantity;

    private Double price;

    private Integer vat;

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getProducer() {
        return producer;
    }

    public void setProducer(String producer) {
        this.producer = producer;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    public Integer getVat() {
        return vat;
    }

    public void setVat(Integer vat) {
        this.vat = vat;
    }

    @Override
    public String toString() {
        return String.format(FORMAT, getCode(), getName(), getProducer(), getQuantity(),
            PRICE_FORMATTER.format(getPrice()), getVat());
    }
}
