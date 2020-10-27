package com.verityfoods.data.model;


import java.util.Objects;

public class Brand implements Comparable<Brand> {
    private int counter = 0;
    private String name;
    private int id;

    public Brand() {
    }

    public Brand(String name) {
        this.name = name;
        this.counter += 1;
        this.id = this.counter;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Brand)) return false;
        Brand brand = (Brand) o;
        assert brand != null;
        return getName().equals(brand.getName());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getName());
    }

    @Override
    public int compareTo(Brand o) {
        int nameDiff = this.name.compareToIgnoreCase(o.name);
        if (nameDiff != 0) {
            return nameDiff;
        }
        return 0;
    }
}
