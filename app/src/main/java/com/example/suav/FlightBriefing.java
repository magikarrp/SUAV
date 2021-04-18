package com.example.suav;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.airmap.airmapsdk.AirMapException;
import com.airmap.airmapsdk.models.flight.AirMapFlightBriefing;
import com.airmap.airmapsdk.models.flight.AirMapFlightPlan;
import com.airmap.airmapsdk.models.rules.AirMapRule;
import com.airmap.airmapsdk.models.rules.AirMapRuleset;
import com.airmap.airmapsdk.networking.callbacks.AirMapCallback;
import com.airmap.airmapsdk.networking.services.AirMap;

import java.util.ArrayList;
import java.util.List;

public class FlightBriefing extends Activity {

    private ListView lstRules;
    private Button btnBack, btnSubmit;
    private ProgressBar pgrsAPILoad;

    private List<AirMapRuleset> rulesets;
    private boolean viewingRulesets;
    private String flightPlanID;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_briefing);

        lstRules = (ListView) findViewById(R.id.lstRules);
        btnBack = (Button) findViewById(R.id.btnBack);
        btnSubmit = (Button) findViewById(R.id.btnSubmit);
        // Include a loading bar since the API calls may take some time
        pgrsAPILoad = (ProgressBar) findViewById(R.id.pgrsWeatherLoad);

        deactivateButtons();

        viewingRulesets = true;

        Bundle bundle = getIntent().getExtras();

        // We need to make sure the airmap object does not get destroyed and reinit it if it was
        if(!AirMap.hasBeenInitialized()) {
            AirMap.init(getApplicationContext());
            if(savedInstanceState == null)
                AirMap.setAuthToken(bundle.getString("AuthToken"));
            else
                AirMap.setAuthToken(savedInstanceState.getString("AuthToken"));
        }

        if(bundle != null) {
            flightPlanID = bundle.getString("PlanID");
        }

        String flightPlanID = bundle.getString("PlanID");

        lstRules.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                // Only accept input to this list if we have received the rulesets and they are currently displayed
                if( viewingRulesets && rulesets.size() > 0) {
                    // Flip to false because we are now viewing rules of a given ruleset
                    viewingRulesets = false;

                    AirMapRuleset ruleset = rulesets.get(position);

                    ArrayList<String> ruleList = new ArrayList<>();
                    ArrayList<String> violationsList = new ArrayList<>();
                    for(AirMapRule rule : ruleset.getRules()) {
                        String rulestring = rule.getShortText();

                        if(rule.getStatus() == AirMapRule.Status.Conflicting) {
                            rulestring += ("\n\n - Violated");
                        } else {
                            rulestring += ("\n\n - Followed");
                        }
                        ruleList.add(rulestring);
                    }

                    ArrayAdapter<String> listAdapter = new ArrayAdapter<String>(getApplicationContext(), R.layout.listview_layout, ruleList);

                    lstRules.setAdapter(listAdapter);

                    activateButtons();
                }
            }
        });

        pgrsAPILoad.setVisibility(View.VISIBLE);
        AirMap.getFlightBrief(flightPlanID, new AirMapCallback<AirMapFlightBriefing>() {
            @Override
            protected void onSuccess(AirMapFlightBriefing response) {
                rulesets = response.getRulesets();
                if(savedInstanceState == null)
                    viewRulesets();
                pgrsAPILoad.setVisibility(View.GONE);
            }

            @Override
            protected void onError(AirMapException e) {
                Log.e("Airmap Briefing Error: ", e.toString());
                Toast.makeText(getApplicationContext(), "Error connecting to the AirMap Flight Briefing API, please try again later", Toast.LENGTH_LONG).show();
                pgrsAPILoad.setVisibility(View.GONE);
            }
        });
    }

    public void backToRulesets(View v) {
        viewRulesets();
    }

    private void viewRulesets() {
        ArrayList<String> ruleList = new ArrayList<>();
        ArrayList<String> violationList = new ArrayList<>();

        for(int i = 0; i < rulesets.size(); i++) {
            AirMapRuleset ruleset = rulesets.get(i);

            // Get the number of rules violations in each ruleset


            // Ruleset names not displaying, so we will clean up the IDs to be more readable
            String ruleString = ruleset.getId();
            ruleString = ruleString.replace("usa", "USA");
            ruleString = ruleString.replace("_", " ");

            // Capitalize the first letter of each word
            for(int j = 0; j < ruleString.length()-1; j++) {
                char c = ruleString.charAt(j);
                if (c == ' ') {
                    // Replace the character after a space with a capital letter
                    ruleString = ruleString.substring(0, j + 1) + Character.toUpperCase(ruleString.charAt(j + 1)) + ruleString.substring(j + 2);
                }
            }

            int violationCount = 0;
            for(AirMapRule rule : ruleset.getRules()) {
                if(rule.getStatus() == AirMapRule.Status.Conflicting)
                    violationCount++;
            }
            ruleString += "\n\n Violations: " + violationCount;

            ruleList.add(ruleString);
        }

        ArrayAdapter<String> listAdapter = new ArrayAdapter<String>(getApplicationContext(), R.layout.listview_layout, ruleList);

        lstRules.setAdapter(listAdapter);

        viewingRulesets = true;
        deactivateButtons();
    }

    private void activateButtons() {
        btnBack.setAlpha(1);
        btnBack.setClickable(true);
    }

    private void deactivateButtons() {
        btnBack.setAlpha(0);
        btnBack.setClickable(false);
    }

    public void submitPlan(View v) {
        // Disable submission on click
        btnSubmit.setAlpha(.5f);
        btnSubmit.setClickable(false);

        pgrsAPILoad.setVisibility(View.VISIBLE);

        if(rulesets.size() > 0) {
            AirMap.submitFlightPlan(flightPlanID, new AirMapCallback<AirMapFlightPlan>() {
                @Override
                protected void onSuccess(AirMapFlightPlan response) {
                    Log.i("Submission Success!", "Flight id: " + response.getFlightId());
                    Toast.makeText(getApplicationContext(), "Successfully submitted flight!", Toast.LENGTH_SHORT).show();

                    // Go back to main page
                    Intent goToMainActivity = new Intent(getApplicationContext(), LoginActivity.class);
                    goToMainActivity.putExtra("AuthToken", AirMap.getAuthToken());
                    startActivity(goToMainActivity);
                }

                @Override
                protected void onError(AirMapException e) {
                    Log.e("Submission Failure", e.toString());
                    // Display error to user because most airmap errors are in plaintext about the information that they input
                    Toast.makeText(getApplicationContext(), "Failed to submit flight, " +e.toString(), Toast.LENGTH_SHORT).show();
                    // Reenable submission
                    btnSubmit.setAlpha(1);
                    btnSubmit.setClickable(true);
                }
            });
        }
    }


    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        viewingRulesets = savedInstanceState.getBoolean("viewingRulesets");
        if(viewingRulesets) {
            deactivateButtons();
        } else {
            activateButtons();
        }

        ArrayList<String> listEntries = savedInstanceState.getStringArrayList("listEntries");

        ArrayAdapter<String> listAdapter = new ArrayAdapter<String>(getApplicationContext(), R.layout.listview_layout, listEntries);
        lstRules.setAdapter(listAdapter);

        // We need to make sure the airmap object does not get destroyed and reinit it if it was
        if(!AirMap.hasBeenInitialized()) {
            AirMap.init(getApplicationContext());
            AirMap.setAuthToken(savedInstanceState.getString("AuthToken"));
        }
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        outState.putString("AuthToken", AirMap.getAuthToken());
        outState.putBoolean("viewingRulesets", viewingRulesets);

        ArrayList<String> listEntries = new ArrayList<>();
        for(int i = 0; i < lstRules.getAdapter().getCount(); i++) {
            listEntries.add(lstRules.getAdapter().getItem(i).toString());
        }

        outState.putStringArrayList("listEntries", listEntries);



        super.onSaveInstanceState(outState);
    }
}
