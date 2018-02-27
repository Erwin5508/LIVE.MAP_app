package com.example.android.livemap.Database;

/**
 * Created by ErwinF on 2/26/2018.
 */

public class Objectives {

    private String title;
    private String description;
    private double latitude;
    private double longitude;

    public Objectives(String title, String description, double latitude, double longitude) {
        this.title = title;
        this.description = description;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }
}
