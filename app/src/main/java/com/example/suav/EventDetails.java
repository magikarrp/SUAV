package com.example.suav;
import android.os.Bundle;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.widget.Toolbar;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class EventDetails extends AppCompatActivity {
    private writeDatabaseHelper dataGrab;
    private String dateString, startDateString, endDateString, takeOffCoordinate, flightID, maxAltitude;
    private FirebaseDatabase rootNode;
    private DatabaseReference reference;
    private double lon, lat;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.event_details);

        Button btnSubEvent = (Button) findViewById(R.id.btnSubEvent);
        EditText edtEventName = (EditText) findViewById(R.id.edtEventName);             //not null
        EditText edtDescription = (EditText) findViewById(R.id.edtDescription);             //not null
        TextView txtAdd = (TextView) findViewById(R.id.txtAdd);

        initMenu();
        edtEventName = (EditText) findViewById(R.id.edtEventDetailsName);
        TextView txtDisc = (TextView) findViewById(R.id.txtDisc);


        txtAdd.setText(getIntent().getExtras().getString("address"));

        btnSubEvent.setOnClickListener (new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //Write to database
                FirebaseDatabase rootNode = FirebaseDatabase.getInstance();
                DatabaseReference reference = rootNode.getReference("Pins").child("Personal");

                //Convert data to string for easier database storage
                String pinName = edtEventName.getText().toString();
                String pinComment = "Description: " + edtDescription.getText().toString();
                Bundle bundle = getIntent().getExtras();
                flightID = bundle.getString("PlanID");

                dataGrab = new writeDatabaseHelper(bundle.getString("startDate"), bundle.getString("endDate"), bundle.getString("takeoffcoord"), bundle.getString("altitude"), pinName, pinComment);
                dataGrab.setDate(dateString);
                dataGrab.setFlightID(flightID);

                startDateString = dataGrab.getStartDate();
                endDateString = dataGrab.getEndDate();
                takeOffCoordinate = dataGrab.getTakeOffCoordinate();
                maxAltitude = dataGrab.getMaxAltitude();


               // writeDatabaseHelper writeHelper = new writeDatabaseHelper(pinComment, lat, lon);
               // reference.child(pinName).setValue(writeHelper);'
                //Write to database.
                writeDatabaseHelper writeHelper = new writeDatabaseHelper(startDateString, endDateString, takeOffCoordinate, maxAltitude, pinName, pinComment);
                reference.child(flightID).setValue(writeHelper);

                Intent intent = new Intent(EventDetails.this, FlightBriefing.class);
                intent.putExtra("PlanID", getIntent().getExtras().getString("PlanID"));
                startActivity(intent);
            }
        });}

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
