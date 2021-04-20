package com.example.suav;

import android.app.Activity;
import android.content.Context;
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
import androidx.appcompat.widget.Toolbar;

import com.airmap.airmapsdk.AirMapException;
import com.airmap.airmapsdk.models.flight.AirMapFlightBriefing;
import com.airmap.airmapsdk.models.flight.AirMapFlightPlan;
import com.airmap.airmapsdk.models.rules.AirMapRule;
import com.airmap.airmapsdk.models.rules.AirMapRuleset;
import com.airmap.airmapsdk.networking.callbacks.AirMapCallback;
import com.airmap.airmapsdk.networking.services.AirMap;

import java.util.ArrayList;
import java.util.List;


/**
 * The FlightBriefing Activity takes a flight plan and requests a briefing for that plan from the
 * AirMap API. Here we display the various rulesets required for the area specified by the flight
 * plan and the number of violations that this plan incurs against these rulesets. The user can
 * select a ruleset to see further details on the rules they may be violating.
 */

public class FlightBriefing extends Activity {

    private ListView lstRules;
    private Button  btnSubmit;
    private ProgressBar pgrsAPILoad;

    private List<AirMapRuleset> rulesets;
    private boolean viewingRulesets;
    private String flightPlanID;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_briefing);

        initMenu(); // setup menu

        lstRules = (ListView) findViewById(R.id.lstRules);
        lstRules.setDivider(null);
        btnSubmit = (Button) findViewById(R.id.btnSubmit);
        // Include a loading bar since the API calls may take some time
        pgrsAPILoad = (ProgressBar) findViewById(R.id.pgrsPlanLoad);

        // Hide the back button to start out with as it is used to go from rules back to rulesets
        deactivateButtons();

        // viewingRulesets tells us what state the lisview is in
        viewingRulesets = true;

        Bundle bundle = getIntent().getExtras();

        // We need to make sure the airmap object does not get destroyed and reinit it if it was
        if(!AirMap.hasBeenInitialized()) {
            AirMap.init(getApplicationContext());
            AirMap.setAuthToken(getApplicationContext().getApplicationContext().getSharedPreferences(getString(R.string.auth_preference_file_key), Context.MODE_PRIVATE).getString("auth_token", ""));
        }

        // Get the flight plan ID from the previous activity
        if(bundle != null) {
            flightPlanID = bundle.getString("PlanID");
        }

        // When user clicks the listview while it is displaying a ruleset, we change the view to display the rules in that ruleset
        lstRules.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                // Only accept input to this list if we have received the rulesets and they are currently displayed
                if( viewingRulesets && rulesets.size() > 0) {
                    // Flip to false because we are now viewing rules of a given ruleset
                    viewingRulesets = false;

                    // Get the ruleset from our list of rulesets based on the listview item that was clicked
                    AirMapRuleset ruleset = rulesets.get(position);

                    // Display all of the rules and whether they are violated or not
                    ArrayList<String> ruleList = new ArrayList<>();
                    for(AirMapRule rule : ruleset.getRules()) {
                        String rulestring = rule.getShortText();

                        if(rule.getStatus() == AirMapRule.Status.Conflicting) {
                            rulestring += ("\n\n" + getResources().getString(R.string.briefing_violation));
                        } else {
                            rulestring += ("\n\n" + getResources().getString(R.string.briefing_following));
                        }
                        ruleList.add(rulestring);
                    }

                    // Place our rules into the listview
                    ArrayAdapter<String> listAdapter = new ArrayAdapter<String>(getApplicationContext(), R.layout.listview_layout, R.id.txtLV, ruleList);

                    lstRules.setAdapter(listAdapter);

                    // Activate the back button so user can return to rulesets
                    activateButtons();
                }
            }
        });

        // Display loading ring
        pgrsAPILoad.setVisibility(View.VISIBLE);
        AirMap.getFlightBrief(flightPlanID, new AirMapCallback<AirMapFlightBriefing>() {
            @Override
            protected void onSuccess(AirMapFlightBriefing response) {
                // On response, populate listview if nothing is there and remove loading circle
                rulesets = response.getRulesets();
                if(savedInstanceState == null)
                    viewRulesets();
                pgrsAPILoad.setVisibility(View.GONE);
            }

            @Override
            protected void onError(AirMapException e) {
                Log.e(getResources().getString(R.string.airmap_error), e.toString());
                Toast.makeText(getApplicationContext(), getResources().getString(R.string.briefing_api_error), Toast.LENGTH_LONG).show();
                pgrsAPILoad.setVisibility(View.GONE);
            }
        });
    }

    /* OnClick function wrapped around a call to viewRulesets */
    public void backToRulesets(View v) {
        viewRulesets();
    }

    /* Populates the listview with descriptions of the values currently stored in rulesets */
    private void viewRulesets() {
        ArrayList<String> ruleList = new ArrayList<>();

        // Iterate over every ruleset for this flight plan
        for(int i = 0; i < rulesets.size(); i++) {
            AirMapRuleset ruleset = rulesets.get(i);

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

            // Counts the number of rules that user's plan violates in each ruleset and displays them to the user
            int violationCount = 0;
            for(AirMapRule rule : ruleset.getRules()) {
                if(rule.getStatus() == AirMapRule.Status.Conflicting)
                    violationCount++;
            }
            ruleString += "\n\n" + getResources().getString(R.string.briefing_violations) + violationCount;

            ruleList.add(ruleString);
        }

        ArrayAdapter<String> listAdapter = new ArrayAdapter<String>(getApplicationContext(), R.layout.listview_layout, R.id.txtLV, ruleList);

        lstRules.setAdapter(listAdapter);

        viewingRulesets = true;

        deactivateButtons();
    }

    /* Displays the back button that returns user to ruleset view */
    private void activateButtons() {
        //btnBack.setAlpha(1);
        //btnBack.setClickable(true);
    }

    /* Hides the back button that returns user to ruleset view */
    private void deactivateButtons() {
        //btnBack.setAlpha(0);
        //btnBack.setClickable(false);
    }

    /* Submits flight plan to airmap */
    public void submitPlan(View v) {
        // Disable submission on click
        btnSubmit.setAlpha(.5f);
        btnSubmit.setClickable(false);

        pgrsAPILoad.setVisibility(View.VISIBLE);

        if(rulesets.size() > 0) {
            AirMap.submitFlightPlan(flightPlanID, new AirMapCallback<AirMapFlightPlan>() {
                @Override
                protected void onSuccess(AirMapFlightPlan response) {
                    Toast.makeText(getApplicationContext(), getResources().getString(R.string.briefing_submission), Toast.LENGTH_SHORT).show();

                    // Go back to main page
                    Intent goToMainActivity = new Intent(getApplicationContext(), LoginActivity.class);
                    goToMainActivity.putExtra("AuthToken", AirMap.getAuthToken());
                    startActivity(goToMainActivity);
                }

                @Override
                protected void onError(AirMapException e) {
                    Log.e(getResources().getString(R.string.briefing_submission_error), e.toString());
                    // Display error to user because most airmap errors are in plaintext about the information that they input
                    Toast.makeText(getApplicationContext(), getResources().getString(R.string.briefing_submission_error_toast) +e.toString(), Toast.LENGTH_SHORT).show();
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

        // If the rulesets were displayed, make sure we toggle the button accordingly
        viewingRulesets = savedInstanceState.getBoolean("viewingRulesets");
        if(viewingRulesets) {
            deactivateButtons();
        } else {
            activateButtons();
        }

        // Repopulate the list with what was there previously so screen doesn't go blank on every reload
        ArrayList<String> listEntries = savedInstanceState.getStringArrayList("listEntries");

        ArrayAdapter<String> listAdapter = new ArrayAdapter<String>(getApplicationContext(), R.layout.listview_layout, R.id.txtLV, listEntries);
        lstRules.setAdapter(listAdapter);

        flightPlanID = savedInstanceState.getString("flightID");

        // We need to make sure the airmap object does not get destroyed and reinit it if it was
        if(!AirMap.hasBeenInitialized()) {
            AirMap.init(getApplicationContext());
            AirMap.setAuthToken(getApplicationContext().getApplicationContext().getSharedPreferences(getString(R.string.auth_preference_file_key), Context.MODE_PRIVATE).getString("auth_token", ""));
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
        outState.putString("flightID", flightPlanID);
        super.onSaveInstanceState(outState);
    }

    private void initMenu() {
        Toolbar t = (Toolbar) findViewById(R.id.briefing_toolbar);
        t.setTitle(getString(R.string.briefing_menu_title));
        t.inflateMenu(R.menu.default_menu);
        t.setOnMenuItemClickListener(item -> {
            switch(item.getItemId()) {
                case R.id.menu_profile:
                    // GO TO PROFILE
                    Intent toProfile = new Intent(FlightBriefing.this, ProfileActivity.class);
                    startActivity(toProfile);
                    return true;
                default:
                    // Should not happen
                    return true;
            }
        });
    }

    @Override
    public void onBackPressed() {
        if (!viewingRulesets) viewRulesets();
        else finish();
    }

}
