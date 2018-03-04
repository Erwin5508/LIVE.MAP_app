package com.example.android.livemap.Database;

/**
 * Created by ErwinF on 2/26/2018.
 */

public class Users {

    private String user_id;
    private double latitude;
    private double longitude;

    public Users() {

    }

    public Users(String user_id, double latitude, double longitude) {
        this.user_id = user_id;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public String getUser_id() {
        return user_id;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }
}
