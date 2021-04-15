package com.example.suav;

public class writeDatabaseHelper {

    String text;
    String userID;
    String location;
    String date;

    public writeDatabaseHelper() {
    }

    public writeDatabaseHelper(String userID, String name, String location, String date) {
        this.date = date;
        this.location = location;
        this.text = name;
    }

    public writeDatabaseHelper(String text) {
        this.text = text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getText() {
        return this.text;
    }

    public String getLocation() {
        return location;
    }

    public java.lang.String getDate() {
        return date;
    }
}
