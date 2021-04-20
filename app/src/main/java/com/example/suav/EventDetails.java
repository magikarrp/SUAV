package com.example.suav;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.airmap.airmapsdk.networking.services.AirMap;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class EventDetails extends AppCompatActivity {


    private FirebaseDatabase rootNode;
    private DatabaseReference reference;
    private double lon, lat;
    private String flightID;

    private Button btnCreatePlan;
    private EditText edtEventName, edtEventDescription;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.pin_details);

        edtEventName = (EditText) findViewById(R.id.edtEventDetailsName);
        edtEventDescription = (EditText) findViewById(R.id.edtEventDescription);

        initMenu();

        flightID = getIntent().getExtras().getString("PlanID");

        btnCreatePlan.setOnClickListener (new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //Write to database
                rootNode = FirebaseDatabase.getInstance();
                reference = rootNode.getReference("Pins").child("Pins");

                //Convert data to string for easier database storage
                String pinName = edtEventName.getText().toString();
                String pinComment = "Description: " + edtEventDescription.getText().toString();


               // writeDatabaseHelper writeHelper = new writeDatabaseHelper(pinComment, lat, lon);
               // reference.child(pinName).setValue(writeHelper);

                Intent intent = new Intent(EventDetails.this, FlightBriefing.class);
                intent.putExtra("PlanID", flightID);
                startActivity(intent);
            }
        });



    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public void setLon(double lon) {
        this.lon = lon;
    }

    private void initMenu() {
        Toolbar t = (Toolbar) findViewById(R.id.pd_toolbar);
        t.setTitle(getString(R.string.pd_menu_title));
        t.inflateMenu(R.menu.default_menu);
    }
}
