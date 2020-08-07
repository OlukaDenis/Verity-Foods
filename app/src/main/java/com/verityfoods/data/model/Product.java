package com.verityfoods.data.model;

import java.io.Serializable;

public class Product implements Serializable {
    private String uuid;
    private String name;
    private String image;
    private String brand;
    private int mrp_rates;
    private int selling_price;

    public Product() {
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public int getMrp_rates() {
        return mrp_rates;
    }

    public void setMrp_rates(int mrp_rates) {
        this.mrp_rates = mrp_rates;
    }

    public int getSelling_price() {
        return selling_price;
    }

    public void setSelling_price(int selling_price) {
        this.selling_price = selling_price;
    }
}
