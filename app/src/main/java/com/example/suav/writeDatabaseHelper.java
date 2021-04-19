package com.example.suav;

import com.airmap.airmapsdk.models.Coordinate;

import java.util.Date;

public class writeDatabaseHelper {

    String startDate;
    String endDate;
    String takeOffCoordinate;
    String maxAltitude;
    Double longitude;
    Double latitude;
    String pinComment;
    String pinRating;

    public writeDatabaseHelper() {
    }

    public writeDatabaseHelper(String startDate, String endDate, String takeoffCoordinate, String maxAltitude) {

        this.endDate = endDate;
        this.startDate = startDate;
        this.takeOffCoordinate = takeoffCoordinate;
        this.maxAltitude = maxAltitude;

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
                '}';
    }
}