package com.example.suav;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.airmap.airmapsdk.AirMapException;
import com.airmap.airmapsdk.models.Coordinate;
import com.airmap.airmapsdk.models.flight.AirMapFlightPlan;
import com.airmap.airmapsdk.models.rules.AirMapRuleset;
import com.airmap.airmapsdk.models.shapes.AirMapPolygon;
import com.airmap.airmapsdk.networking.callbacks.AirMapCallback;
import com.airmap.airmapsdk.networking.services.AirMap;
import static com.airmap.airmapsdk.models.shapes.AirMapGeometry.getGeoJSONFromGeometry;

import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.DatabaseReference;
import com.mapbox.mapboxsdk.geometry.LatLng;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * The Flight Planning activity prompts the user for information about the flight they would like to
 * plan and eventually submit to AirMap.  Here the user inputs things like maximum altitude and time
 * and date of the flight plan. We use this information to construct an AirMapFlightPlan as well as
 * gather a list of the required regulations in the flight plan's area.
 */

public class FlightPlanning extends AppCompatActivity {

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

        initMenu(); // sets up the menu for this activity

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

        Log.i("From Last Intent", bundle.get("path").toString());

        Log.i("Here is my test", "test");

        // If we don't get any information from the previous activity, set default values (should not happen)
        if(bundle.get("path") == null) {
            ArrayList<Coordinate> coordinates = new ArrayList<>();
            coordinates.add(new Coordinate(42.355802283858,-71.0643620966118));
            coordinates.add(new Coordinate(42.354402695082804,-71.06639529452146));
            coordinates.add(new Coordinate(42.355864029703724,-71.06787145190795));
            coordinates.add(new Coordinate(42.355802283858,-71.0643620966118));
            polygon = new AirMapPolygon(coordinates);

            takeoffCoordinate = new Coordinate(42.355402695082804,-71.06539529452146);
            Log.e("DEFAULT", "Setting Default Values!");
        } else {
            // We receive a list of Lat Lon coordinates from the flight path picker activity
            ArrayList<LatLng> path = (ArrayList<LatLng>) bundle.get("path");

            ArrayList<Coordinate> coordinates = new ArrayList<>();

            for(LatLng latlng : path) {
                coordinates.add(new Coordinate(latlng));
            }

            // We need to readd the first pin at the end to close the polygon
            Coordinate startCoord = new Coordinate(path.get(0));

            coordinates.add(startCoord);

            polygon = new AirMapPolygon(coordinates);

            takeoffCoordinate = startCoord;
        }
    }

    public void onCreatePlan(View v) {
        createPlan(false);
    }

    public void onCreateEvent(View v) {
        createPlan(true);
    }

    /* Sends a plan to Airmap API to receive a plan briefing, takes in newEvent which tells it which activity to go to next */
    private void createPlan(boolean newEvent) {
        // Make loading circle visible
        pgrsPlanLoad.setVisibility(View.VISIBLE);

        // First we need to get all possible rulesets for the area
        AirMap.getRulesets(getGeoJSONFromGeometry(polygon), new AirMapCallback<List<AirMapRuleset>>() {
            @RequiresApi(api = Build.VERSION_CODES.M)
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
                                // Now that our flight plan has been registered with AirMap, we want to go to our next activity




                                pgrsPlanLoad.setVisibility(View.GONE);

                                // Write to FireBase Database
                                rootNode = FirebaseDatabase.getInstance();
                                reference = rootNode.getReference("Pins").child("Events");
                                String flightID = response.getPlanId();

                                //Test to see if flightID is null and edtAltitude is null
                                Log.i("testSuccess ", flightID);
                                Log.i("testSuccess ", edtAltitude.getText().toString());

                                //Convert objects into more readable strings for database entry
                                String dateString = startDate.getMonth() + ":" + startDate.getDay() + ":"  +startDate.getYear();
                                String startDateString = dateString + " Time: " + startDate.getHours() + ":" + startDate.getMinutes();
                                String endDateString = endDate.getHours() + ":" + endDate.getMinutes();
                                String takeOffCoordinateString = takeoffCoordinate.getLatitude() + ", " + takeoffCoordinate.getLongitude();

                                // call write helper class and set a new child as flightid with other details as children
                                writeDatabaseHelper writeHelper = new writeDatabaseHelper(startDateString, endDateString, takeOffCoordinateString, edtAltitude.getText().toString());
                                writeHelper.setDate(dateString);
                                writeHelper.setFlightID(flightID);
                                reference.child(flightID).setValue(writeHelper);

                                // Either we get a briefing to see if we are in compliance with the regulations of the area
                                // Or we go to an event planner activity and go to briefing after
                                Intent intent;
                                if(!newEvent) {
                                    intent = new Intent(getApplicationContext(), FlightBriefing.class);
                                    intent.putExtra("PlanID", response.getPlanId());
                                    intent.putExtra("Coordinate", takeoffCoordinate);
                                } else {
                                    intent = new Intent(getApplicationContext(), EventDetails.class);
                                    intent.putExtra("PlanID", response.getPlanId());
                                    intent.putExtra("Coordinate", takeoffCoordinate);
                                    intent.putExtra("startDate", startDateString);
                                    intent.putExtra("endDate", endDateString);
                                    intent.putExtra("takeoffcoord", takeOffCoordinateString);
                                    intent.putExtra("dateString", dateString);
                                    intent.putExtra("altitude", "" + maxAltitude);
                                }

                                startActivity(intent);
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
                        pgrsPlanLoad.setVisibility(View.GONE);
                    }


                } catch (Exception e) {
                    Toast.makeText(getApplicationContext(), getResources().getString(R.string.planning_altitude_error), Toast.LENGTH_LONG).show();
                    pgrsPlanLoad.setVisibility(View.GONE);
                }

            }

            @Override
            protected void onError(AirMapException e) {
                Log.e(getResources().getString(R.string.ruleset_error), e.toString());
                Toast.makeText(getApplicationContext(), getResources().getString(R.string.ruleset_error_toast), Toast.LENGTH_LONG).show();
                pgrsPlanLoad.setVisibility(View.GONE);
            }
        });

    }

    /* Make sure that we maintain the state of the user's input on loads */
    @RequiresApi(api = Build.VERSION_CODES.M)
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

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        outState.putString("AuthToken", AirMap.getAuthToken());
        outState.putInt("StartHour", timePickerStart.getHour());
        outState.putInt("EndHour", timePickerEnd.getHour());
        outState.putInt("StartMinute", timePickerStart.getMinute());
        outState.putInt("EndMinute", timePickerEnd.getMinute());

        super.onSaveInstanceState(outState);
    }

    private void initMenu() {
        Toolbar t = (Toolbar) findViewById(R.id.profile_toolbar);
        t.setTitle(getString(R.string.fp_menu_title));
        t.inflateMenu(R.menu.default_menu);
        t.setOnMenuItemClickListener(item -> {
            switch(item.getItemId()) {
                case R.id.menu_profile:
                    // GO TO PROFILE
                    Intent toProfile = new Intent(FlightPlanning.this, ProfileActivity.class);
                    startActivity(toProfile);
                    return true;
                default:
                    // Should not happen
                    return true;
            }
        });
    }
}
