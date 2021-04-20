package com.example.suav;

import android.os.Bundle;
import android.renderscript.Sampler;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Map;

//returns list view of flight plans aka EVENTS "Flights" <---------
//are all flight plans public

public class readDatabaseUsers extends AppCompatActivity {

    private FirebaseDatabase rootNode;
    private DatabaseReference reference;
    private String flightDetails;
    ListView myListView;
    ArrayList<String> myArrayList = new ArrayList<>();

    protected void onCreate (Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        rootNode = FirebaseDatabase.getInstance();
        reference = rootNode.getReference().child("Pins").child("Events");

        // create listView and array adapter for display
        ArrayAdapter<String> myArrayAdapter = new ArrayAdapter<>(readDatabaseUsers.this, android.R.layout.simple_expandable_list_item_1, myArrayList);
        myListView = (ListView) findViewById(R.id.listview1);
        myListView.setAdapter(myArrayAdapter);

        ValueEventListener eventListener = new ValueEventListener() {
            @Override
            // for loop to grab each parent node and the look at the child details
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                reference = rootNode.getReference().child("Pins").child("Events");
                for(DataSnapshot ss : snapshot.getChildren()) {
                    String maxAltitude = ss.child("maxAltitude").getValue(String.class);
                    String startDate = ss.child("startDate").getValue(String.class);
                    String endDate = ss.child("endDate").getValue(String.class);
                    String takeOffCoordinate = ss.child("takeOffCoordinate").getValue(String.class);
                    flightDetails = "Latitude, Longitude: \n" + takeOffCoordinate + " \n Max Altitude: " + maxAltitude + " \n Date: " + startDate + " | " + endDate;
                    myArrayList.add(flightDetails);
                    ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(readDatabaseUsers.this, android.R.layout.simple_list_item_1, myArrayList);
                    myListView.setAdapter(arrayAdapter);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        };reference.addListenerForSingleValueEvent(eventListener);

        reference = rootNode.getReference().child("Pins").child("Personal");
        ValueEventListener personalListener = new ValueEventListener() {
            @Override
            // for loop to grab each parent node and the look at the child details
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot personal : snapshot.getChildren()) {
                    String takeOffCoordinate = personal.child("takeOffCoordinate").getValue(String.class);
                    String message = personal.child("message").getValue(String.class);
                    String message1 = personal.child("message1").getValue(String.class);
                    flightDetails = "Personal Event: \n" + message + ", " + message1 + ", \n" + takeOffCoordinate;
                    myArrayList.add(flightDetails);
                    ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(readDatabaseUsers.this, android.R.layout.simple_list_item_1, myArrayList);
                    myListView.setAdapter(arrayAdapter);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        };reference.addListenerForSingleValueEvent(personalListener);

    }
}