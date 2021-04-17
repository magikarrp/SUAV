package com.example.suav;

import com.airmap.airmapsdk.models.Coordinate;

import java.util.Date;

public class writeDatabaseHelper {

    Date startDate;
    Date endDate;
    Coordinate takeOffCoordinate;
    String maxAltitude;

    public writeDatabaseHelper(Date startDate, Date endDate, Coordinate takeoffCoordinate, String maxAltitude) {

        this.endDate = endDate;
        this.startDate = startDate;
        this.takeOffCoordinate = takeoffCoordinate;
        this.maxAltitude = maxAltitude;

    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public Coordinate getTakeOffCoordinate() {
        return takeOffCoordinate;
    }

    public void setTakeOffCoordinate(Coordinate takeOffCoordinate) {
        this.takeOffCoordinate = takeOffCoordinate;
    }

    public String getMaxAltitude() {
        return this.maxAltitude;
    }

    public void setMaxAltitude(String maxAltitude) {
        this.maxAltitude = maxAltitude;
    }






}
