package com.example.suav;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TimePicker;

import com.airmap.airmapsdk.models.Coordinate;
import com.airmap.airmapsdk.models.shapes.AirMapPolygon;

public class FlightBriefing extends Activity {

    TextView txtMain;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_briefing);

        txtMain = (TextView) findViewById(R.id.txtMain);

        Bundle bundle = getIntent().getExtras();

        if(bundle != null) {
            txtMain.setText(bundle.getString("Briefing"));
        }
    }
}
