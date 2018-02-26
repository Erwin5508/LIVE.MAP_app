package com.example.android.livemap.Database;

/**
 * Created by ErwinF on 2/25/2018.
 */

public class Messages {

    private String user_id;
    private String message;

    public Messages() {

    }

    public Messages(String user_id, String message) {
        this.user_id = user_id;
        this.message = message;
    }

    public String getUser_id() {
        return user_id;
    }

    public String getMessage() {
        return message;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
