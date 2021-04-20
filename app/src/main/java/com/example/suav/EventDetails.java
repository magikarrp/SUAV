package com.example.suav;

import android.os.Bundle;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class EventDetails extends AppCompatActivity {
    private EditText message;
    private EditText message1;
    private writeDatabaseHelper dataGrab;
    private String dateString, startDateString, endDateString, takeOffCoordinate, flightID, maxAltitude;
    private double longitude,latitude;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_event_details);

        message = (EditText) findViewById(R.id.eventMessage);
        message1 = (EditText) findViewById(R.id.eventMessage1);

        FirebaseDatabase rootNode = FirebaseDatabase.getInstance();
        DatabaseReference reference = rootNode.getReference("Pins").child("Personal");

        dateString = dataGrab.getDate();
        startDateString = dataGrab.getStartDate();
        endDateString = dataGrab.getEndDate();
        takeOffCoordinate = dataGrab.getTakeOffCoordinate();
        longitude = dataGrab.getLongitude();
        latitude = dataGrab.getLatitude();
        flightID = dataGrab.getFlightID();
        maxAltitude = dataGrab.getMaxAltitude();

        //Write to database.
        writeDatabaseHelper writeHelper = new writeDatabaseHelper(startDateString, endDateString, takeOffCoordinate, maxAltitude, message.getText().toString(), message1.getText().toString());
        reference.child(flightID).setValue(writeHelper);








    }



}
