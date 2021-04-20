package com.example.suav;

import android.widget.EditText;

import com.airmap.airmapsdk.models.Coordinate;

import java.util.Date;

public class writeDatabaseHelper {

    private Double longitude;
    private Double latitude;
    private String pinComment, pinRating, message, message1, startDate, endDate, date, takeOffCoordinate, maxAltitude;

    public writeDatabaseHelper(String startDateString, String endDateString, String takeOffCoordinate, String maxAltitude, String message, String message1) {
        this.startDate = startDateString;
        this.endDate = endDateString;
        this.takeOffCoordinate = takeOffCoordinate;
        this.maxAltitude = maxAltitude;
        this.message = message;
        this.message1 = message1;
    }

    public String getFlightID() {
        return flightID;
    }

    public void setFlightID(String flightID) {
        this.flightID = flightID;
    }

    String flightID;

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public writeDatabaseHelper() {
    }

    public writeDatabaseHelper(String startDate, String endDate, String takeoffCoordinate, String maxAltitude) {

        this.endDate = endDate;
        this.startDate = startDate;
        this.takeOffCoordinate = takeoffCoordinate;
        this.maxAltitude = maxAltitude;

    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public String getPinComment() {
        return pinComment;
    }

    public void setPinComment(String pinComment) {
        this.pinComment = pinComment;
    }

    public String getPinRating() {
        return pinRating;
    }

    public void setPinRating(String pinRating) {
        this.pinRating = pinRating;
    }

    public writeDatabaseHelper(String pinRating, String pinComment, double lat, double lon) {
        this.longitude = lon;
        this.latitude = lat;
        this.pinRating = pinRating;
        this.pinComment = pinComment;

    }

    public String getStartDate() {
        return startDate;
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    public String getEndDate() {
        return endDate;
    }

    public void setEndDate(String endDate) {
        this.endDate = endDate;
    }

    public String getTakeOffCoordinate() {
        return takeOffCoordinate;
    }

    public void setTakeOffCoordinate(String takeOffCoordinate) {
        this.takeOffCoordinate = takeOffCoordinate;
    }

    public String getMaxAltitude() {
        return this.maxAltitude;
    }

    public void setMaxAltitude(String maxAltitude) {
        this.maxAltitude = maxAltitude;
    }

    @Override
    public String toString() {
        return "writeDatabaseHelper{" +
                "startDate='" + startDate + '\'' +
                ", endDate='" + endDate + '\'' +
                ", takeOffCoordinate='" + takeOffCoordinate + '\'' +
                ", maxAltitude='" + maxAltitude + '\'' +
                ", message='" + message + '\'' +
                ", message1='" + message1 + '\'' + '}';
    }
}