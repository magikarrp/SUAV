package com.example.suav;

import com.airmap.airmapsdk.models.Coordinate;

import java.util.Date;

public class writeDatabaseHelper {

    FlightPlanning userID;
    Date startDate;

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    public Coordinate getTakeoffCoordinate() {
        return takeoffCoordinate;
    }

    public void setTakeoffCoordinate(Coordinate takeoffCoordinate) {
        this.takeoffCoordinate = takeoffCoordinate;
    }

    public float getMaxAltitude() {
        return maxAltitude;
    }

    public void setMaxAltitude(float maxAltitude) {
        this.maxAltitude = maxAltitude;
    }

    Date endDate;
    Coordinate takeoffCoordinate;
    float maxAltitude;

    public writeDatabaseHelper(Date startDate, Date endDate, Coordinate takeoffCoordinate, float maxAltitude) {
        this.startDate = startDate;
        this.endDate = endDate;

    }

    public writeDatabaseHelper() {
    }

    public writeDatabaseHelper(String text) {
    }

    public FlightPlanning getUserID() {
        return userID;
    }

    public void setUserID(FlightPlanning userID) {
        this.userID = userID;
    }




}
