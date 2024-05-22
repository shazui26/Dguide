package com.ex.dguide.firbasehelper;

public class Places {
    public double latitude;
    public double longitude;
    public String placeName;
    public String description;
    public String address;
    public String category;
    public String username;

    public Places() {
    }

    public Places(double latitude, double longitude, String placeName, String description, String address, String category, String username) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.placeName = placeName;
        this.description = description;
        this.address = address;
        this.category = category;
        this.username = username;

    }




    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public String getPlaceName() {
        return placeName;
    }

    public String getDescription() {
        return description;
    }

    public String getAddress() {
        return address;
    }

    public String getCategory() {
        return category;
    }

    public String getUsername() {
        return username;
    }

}

