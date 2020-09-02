package com.verityfoods.data.model;

import java.io.Serializable;

public class Cart implements Serializable {
    private String category_id;
    private String category_name;
    private String product_id;
    private String product_name;
    private String product_image;
    private int mrp;
    private int quantity;
    private boolean completed;
    private int amount;

    public Cart() {
    }

    public Cart(String category_id, String category_name, String product_id, String product_name, String product_image, int mrp, int quantity, int amount) {
        this.category_id = category_id;
        this.category_name = category_name;
        this.product_id = product_id;
        this.product_name = product_name;
        this.product_image = product_image;
        this.mrp = mrp;
        this.quantity = quantity;
        this.completed = false;
        this.amount = amount;
    }

    public String getCategory_id() {
        return category_id;
    }

    public void setCategory_id(String category_id) {
        this.category_id = category_id;
    }

    public String getCategory_name() {
        return category_name;
    }

    public void setCategory_name(String category_name) {
        this.category_name = category_name;
    }

    public String getProduct_id() {
        return product_id;
    }

    public void setProduct_id(String product_id) {
        this.product_id = product_id;
    }

    public String getProduct_name() {
        return product_name;
    }

    public void setProduct_name(String product_name) {
        this.product_name = product_name;
    }

    public String getProduct_image() {
        return product_image;
    }

    public void setProduct_image(String product_image) {
        this.product_image = product_image;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public boolean isCompleted() {
        return completed;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }

    public int getAmount() {
        return amount;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }

    public int getMrp() {
        return mrp;
    }

    public void setMrp(int mrp) {
        this.mrp = mrp;
    }
}
