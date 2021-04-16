package com.example.suav;

import com.airmap.airmapsdk.AirMapException;
import com.airmap.airmapsdk.models.Coordinate;
import com.airmap.airmapsdk.models.flight.AirMapFlightBriefing;
import com.airmap.airmapsdk.models.flight.AirMapFlightPlan;
import com.airmap.airmapsdk.models.pilot.AirMapPilot;
import com.airmap.airmapsdk.models.rules.AirMapRuleset;
import com.airmap.airmapsdk.models.shapes.AirMapPolygon;
import com.airmap.airmapsdk.networking.callbacks.AirMapCallback;
import com.airmap.airmapsdk.networking.services.AirMap;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FlightPlanning extends Activity {

    TextView txtTitle;
    EditText edtAltitude;
    AirMapPolygon polygon;
    Coordinate takeoffCoordinate;
    DatePicker datePicker;
    TimePicker timePickerStart, timePickerEnd;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_flight);

        txtTitle = (TextView) findViewById(R.id.txtTitle);
        edtAltitude = (EditText) findViewById(R.id.edtAltitude);
        datePicker = (DatePicker) findViewById(R.id.datePicker);
        timePickerStart = (TimePicker) findViewById(R.id.timePickerStart);
        timePickerEnd = (TimePicker) findViewById(R.id.timePickerEnd);

        Bundle bundle = getIntent().getExtras();

        // If we don't get any information from the previous activity, set default values
        if(bundle == null) {
            ArrayList<Coordinate> coordinates = new ArrayList<>();
            coordinates.add(new Coordinate(42.355802283858,-71.0643620966118));
            coordinates.add(new Coordinate(42.354402695082804,-71.06639529452146));
            coordinates.add(new Coordinate(42.355864029703724,-71.06787145190795));
            coordinates.add(new Coordinate(42.355802283858,-71.0643620966118));
            polygon = new AirMapPolygon(coordinates);

            takeoffCoordinate = new Coordinate(42.355402695082804,-71.06539529452146);
            Log.i("DEFAULT: ", "Setting Default values for flight plan...");
        } else {
            // Import values from saved instance state
        }
    }

    public void createPlan(View v) {
        // First we need to get all possible rulesets for the area
        AirMap.getRulesets(polygon.getGeoJSONFromGeometry(polygon), new AirMapCallback<List<AirMapRuleset>>() {
            @Override
            protected void onSuccess(List<AirMapRuleset> response) {
                // We want to give the user a choice between all rulesets in the area
                ArrayList<String> rulesetIds = new ArrayList<>();

                for(AirMapRuleset ruleset : response) {
                    rulesetIds.add(ruleset.getId());
                }

                // We now need to register a flight plan with airmap so that they have it in their system
                // We can construct this plan from the user's input to the activity
                AirMapFlightPlan flightPlan = new AirMapFlightPlan();
                flightPlan.setPilotId(AirMap.getUserId());
                flightPlan.setGeometry(polygon);
                flightPlan.setTakeoffCoordinate(takeoffCoordinate);
                flightPlan.setRulesetIds(rulesetIds);

                // Use try/catch to make sure something was put in altitude edit text
                try {
                    float maxAltitude = Float.parseFloat(edtAltitude.getText().toString());

                    flightPlan.setMaxAltitude(maxAltitude);

                    // We construct a date from the user's input on the time and date pickers
                    Date startDate = new Date(datePicker.getYear() - 1900, datePicker.getMonth(), datePicker.getDayOfMonth(), timePickerStart.getHour(), timePickerStart.getMinute());
                    Date endDate = new Date(datePicker.getYear() - 1900, datePicker.getMonth(), datePicker.getDayOfMonth(), timePickerEnd.getHour(), timePickerEnd.getMinute());

                    flightPlan.setStartsAt(startDate);
                    flightPlan.setEndsAt(endDate);

                    AirMap.createFlightPlan(flightPlan, new AirMapCallback<AirMapFlightPlan>() {
                        @Override
                        protected void onSuccess(AirMapFlightPlan response) {
                            // Now that our flight plan has been registered with AirMap, we want to get a briefing to see if we are in compliance with the regulations of the area

                            AirMap.getFlightBrief(response.getPlanId(), new AirMapCallback<AirMapFlightBriefing>() {
                                @Override
                                protected void onSuccess(AirMapFlightBriefing response) {
                                    Intent goToBriefing = new Intent(getApplicationContext(), FlightBriefing.class);
                                    goToBriefing.putExtra("Briefing", response.toString());
                                    Log.i("Airmap Success: ", response.toString());
                                    startActivity(goToBriefing);
                                }

                                @Override
                                protected void onError(AirMapException e) {
                                    Log.e("Airmap Briefing Error: ", e.toString());
                                    Toast.makeText(getApplicationContext(), "Error connecting to the AirMap Flight Briefing API, please try again later", Toast.LENGTH_LONG).show();
                                }
                            });
                        }

                        @Override
                        protected void onError(AirMapException e) {
                            Log.e("Airmap Planning Error: ", e.toString());
                            Toast.makeText(getApplicationContext(), "Error connecting to the AirMap Flight Plan API, please try again later", Toast.LENGTH_LONG).show();
                        }
                    });

                } catch (Exception e) {
                    Toast.makeText(getApplicationContext(), "Please input a valid number for altitude.", Toast.LENGTH_LONG).show();
                }

            }

            @Override
            protected void onError(AirMapException e) {
                Toast.makeText(getApplicationContext(), "Error connecting to the AirMap Ruleset API, please try again later", Toast.LENGTH_LONG).show();
            }
        });

    }

}
