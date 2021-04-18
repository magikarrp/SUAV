package com.example.suav;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.util.Log;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.airmap.airmapsdk.AirMapException;
import com.airmap.airmapsdk.models.Coordinate;
import com.airmap.airmapsdk.models.flight.AirMapFlightPlan;
import com.airmap.airmapsdk.models.rules.AirMapRuleset;
import com.airmap.airmapsdk.models.shapes.AirMapPolygon;
import com.airmap.airmapsdk.networking.callbacks.AirMapCallback;
import com.airmap.airmapsdk.networking.services.AirMap;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static com.airmap.airmapsdk.models.shapes.AirMapGeometry.getGeoJSONFromGeometry;

public class FlightPlanning extends Activity {

    private TextView txtTitle;
    private EditText edtAltitude;
    private AirMapPolygon polygon;
    private Coordinate takeoffCoordinate;
    private DatePicker datePicker;
    private TimePicker timePickerStart, timePickerEnd;
    private ProgressBar pgrsPlanLoad;
    private FirebaseDatabase rootNode;
    private DatabaseReference reference;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_flight);

        txtTitle = (TextView) findViewById(R.id.txtTitle);
        edtAltitude = (EditText) findViewById(R.id.edtAltitude);
        datePicker = (DatePicker) findViewById(R.id.datePicker);
        timePickerStart = (TimePicker) findViewById(R.id.timePickerStart);
        timePickerEnd = (TimePicker) findViewById(R.id.timePickerEnd);
        pgrsPlanLoad = (ProgressBar) findViewById(R.id.pgrsPlanLoad);

        pgrsPlanLoad.setVisibility(View.GONE);

        Bundle bundle = getIntent().getExtras();

        // We need to make sure the airmap object does not get destroyed and reinit it if it was
        if(!AirMap.hasBeenInitialized()) {
            AirMap.init(getApplicationContext());
            AirMap.setAuthToken(getApplicationContext().getApplicationContext().getSharedPreferences(getString(R.string.auth_preference_file_key), Context.MODE_PRIVATE).getString("auth_token", ""));
        }

        // If we don't get any information from the previous activity, set default values
        if(bundle.getString("polygon") == null) {
            ArrayList<Coordinate> coordinates = new ArrayList<>();
            coordinates.add(new Coordinate(42.355802283858,-71.0643620966118));
            coordinates.add(new Coordinate(42.354402695082804,-71.06639529452146));
            coordinates.add(new Coordinate(42.355864029703724,-71.06787145190795));
            coordinates.add(new Coordinate(42.355802283858,-71.0643620966118));
            polygon = new AirMapPolygon(coordinates);

            takeoffCoordinate = new Coordinate(42.355402695082804,-71.06539529452146);
        } else {
            // Import values from bundle/saved instance state
        }
    }

    /* Sends a plan to Airmap API to receive a plan briefing */
    public void createPlan(View v) {
        // Make loading circle visible
        pgrsPlanLoad.setVisibility(View.VISIBLE);

        // First we need to get all possible rulesets for the area
        AirMap.getRulesets(getGeoJSONFromGeometry(polygon), new AirMapCallback<List<AirMapRuleset>>() {
            @Override
            protected void onSuccess(List<AirMapRuleset> response) {
                // We want to display all of the required rulesets for this flight geometry
                ArrayList<String> rulesetIds = new ArrayList<>();

                for(AirMapRuleset ruleset : response) {
                    if(ruleset.getType() == AirMapRuleset.Type.Required)
                        // We only need the rulesetIDs for the planning request
                        rulesetIds.add(ruleset.getId());
                }

                if(rulesetIds.isEmpty())
                    rulesetIds.add(response.get(0).getId());

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

                    // Make sure the input end time was after the input start time
                    if(endDate.after(startDate)){
                        flightPlan.setStartsAt(startDate);
                        flightPlan.setEndsAt(endDate);

                        AirMap.createFlightPlan(flightPlan, new AirMapCallback<AirMapFlightPlan>() {
                            @Override
                            protected void onSuccess(AirMapFlightPlan response) {
                                // Now that our flight plan has been registered with AirMap, we want to get a briefing to see if we are in compliance with the regulations of the area
                                Intent goToBriefing = new Intent(getApplicationContext(), FlightBriefing.class);
                                goToBriefing.putExtra("PlanID", response.getPlanId());
                                goToBriefing.putExtra("AuthToken", AirMap.getAuthToken());

                                pgrsPlanLoad.setVisibility(View.GONE);

                                // Write to FireBase Database
                                rootNode = FirebaseDatabase.getInstance();
                                reference = rootNode.getReference("Pins");
                                String flightID = response.getFlightId();

                                //Test to see if flightID is null and edtAltitude is null
                                Log.i("testSuccess ", flightID);
                                Log.i("testSuccess ", edtAltitude.getText().toString());


                                writeDatabaseHelper writeHelper = new writeDatabaseHelper(startDate, endDate, takeoffCoordinate, edtAltitude.getText().toString());
                                reference.child("testUSERID").setValue(writeHelper);
                                startActivity(goToBriefing);
                            }

                            @Override
                            protected void onError(AirMapException e) {
                                Log.e(getResources().getString(R.string.planning_submission_error), e.toString());
                                Toast.makeText(getApplicationContext(), getResources().getString(R.string.planning_submission_error_toast), Toast.LENGTH_LONG).show();
                                pgrsPlanLoad.setVisibility(View.GONE);
                            }
                        });
                    } else {
                        Toast.makeText(getApplicationContext(), getResources().getString(R.string.planning_etime_error), Toast.LENGTH_LONG).show();
                    }


                } catch (Exception e) {
                    Toast.makeText(getApplicationContext(), getResources().getString(R.string.planning_altitude_error), Toast.LENGTH_LONG).show();
                }

            }

            @Override
            protected void onError(AirMapException e) {
                Log.e(getResources().getString(R.string.ruleset_error), e.toString());
                Toast.makeText(getApplicationContext(), getResources().getString(R.string.ruleset_error_toast), Toast.LENGTH_LONG).show();
            }
        });

    }

    /* Make sure that we maintain the state of the user's input on loads */
    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        if(!AirMap.hasBeenInitialized()) {
            AirMap.init(getApplicationContext());
            AirMap.setAuthToken(getApplicationContext().getApplicationContext().getSharedPreferences(getString(R.string.auth_preference_file_key), Context.MODE_PRIVATE).getString("auth_token", ""));
        }

        timePickerStart.setHour(savedInstanceState.getInt("StartHour"));
        timePickerStart.setHour(savedInstanceState.getInt("StartMinute"));
        timePickerEnd.setHour(savedInstanceState.getInt("EndHour"));
        timePickerEnd.setHour(savedInstanceState.getInt("EndMinute"));
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        outState.putString("AuthToken", AirMap.getAuthToken());
        outState.putInt("StartHour", timePickerStart.getHour());
        outState.putInt("EndHour", timePickerEnd.getHour());
        outState.putInt("StartMinute", timePickerStart.getMinute());
        outState.putInt("EndMinute", timePickerEnd.getMinute());

        super.onSaveInstanceState(outState);
    }
}
