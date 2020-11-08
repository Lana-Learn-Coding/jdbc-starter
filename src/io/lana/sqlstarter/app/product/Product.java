package io.lana.sqlstarter.app.product;

public class Product {
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
        return "Product{" +
               "code=" + code +
               ", name='" + name + '\'' +
               ", producer='" + producer + '\'' +
               ", quantity=" + quantity +
               ", price=" + price +
               ", vat=" + vat +
               '}';
    }
}
