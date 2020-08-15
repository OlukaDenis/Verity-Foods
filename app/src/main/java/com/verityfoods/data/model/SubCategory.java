package com.verityfoods.data.model;

import java.io.Serializable;

public class SubCategory implements Serializable {
    private String uuid;
    private String name;

    public SubCategory() {
    }

    public SubCategory(String name) {
        this.name = name;
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
}
