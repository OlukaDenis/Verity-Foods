package com.verityfoods.data.model;

import android.os.Parcel;
import android.os.Parcelable;

public class Address implements Parcelable {
    private String uuid;
    private String username;
    private String address;
    private String name;
    private String phone;
    private String email;
    private double latitude;
    private double longitude;
    private boolean isDefault;

    public Address() {
    }


    public String getUuid() {

        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public boolean isDefault() {
        return isDefault;
    }

    public void setDefault(boolean aDefault) {
        isDefault = aDefault;
    }

    protected Address(Parcel in) {
        uuid = in.readString();
        username = in.readString();
        address = in.readString();
        name = in.readString();
        phone = in.readString();
        email = in.readString();
        latitude = in.readDouble();
        longitude = in.readDouble();
        isDefault = in.readByte() != 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(uuid);
        dest.writeString(username);
        dest.writeString(address);
        dest.writeString(name);
        dest.writeString(phone);
        dest.writeString(email);
        dest.writeDouble(latitude);
        dest.writeDouble(longitude);
        dest.writeByte((byte) (isDefault ? 1 : 0));
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<Address> CREATOR = new Creator<Address>() {
        @Override
        public Address createFromParcel(Parcel in) {
            return new Address(in);
        }

        @Override
        public Address[] newArray(int size) {
            return new Address[size];
        }
    };
}
