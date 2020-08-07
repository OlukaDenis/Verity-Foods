package com.verityfoods.data.model;

import java.io.Serializable;

public class Category implements Serializable {
    private String uuid;
    private String name;
    private String image;

    public Category() {
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
}
