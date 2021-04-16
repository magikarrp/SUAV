package com.example.suav;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.airmap.airmapsdk.AirMapException;
import com.airmap.airmapsdk.models.Coordinate;
import com.airmap.airmapsdk.models.flight.AirMapFlightBriefing;
import com.airmap.airmapsdk.models.rules.AirMapRule;
import com.airmap.airmapsdk.models.shapes.AirMapPolygon;
import com.airmap.airmapsdk.networking.callbacks.AirMapCallback;
import com.airmap.airmapsdk.networking.services.AirMap;

import java.util.ArrayList;

public class FlightBriefing extends Activity {

    ListView lstRules;


    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_briefing);

        lstRules = (ListView) findViewById(R.id.lstRules);

        Bundle bundle = getIntent().getExtras();

        if(!AirMap.hasBeenInitialized()) {
            AirMap.init(getApplicationContext());
            if(savedInstanceState == null)
                AirMap.setAuthToken(bundle.getString("AuthToken"));
            else
                AirMap.setAuthToken(savedInstanceState.getString("AuthToken"));
            Log.i("New init", AirMap.getUserId());
        }

        String flightPlanID = bundle.getString("PlanID");

        AirMap.getFlightBrief(flightPlanID, new AirMapCallback<AirMapFlightBriefing>() {
            @Override
            protected void onSuccess(AirMapFlightBriefing response) {
                ArrayList<String> ruleList = new ArrayList<>();

                for(AirMapRule rule : response.getRulesets().get(0).getRules()) {
                    String ruleString = rule.getShortText();

                    ruleString += "   -   " + rule.getStatus().toString();
                    ruleList.add(ruleString);
                }

                ArrayAdapter<String> listAdapter = new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_list_item_1, ruleList);

                lstRules.setAdapter(listAdapter);
            }

            @Override
            protected void onError(AirMapException e) {
                Log.e("Airmap Briefing Error: ", e.toString());
                Toast.makeText(getApplicationContext(), "Error connecting to the AirMap Flight Briefing API, please try again later", Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        if(!AirMap.hasBeenInitialized()) {
            AirMap.init(getApplicationContext());
            AirMap.setAuthToken(savedInstanceState.getString("AuthToken"));
            Log.i("Restore Init", AirMap.getUserId());
        }
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        outState.putString("AuthToken", AirMap.getAuthToken());

        super.onSaveInstanceState(outState);
    }
}
