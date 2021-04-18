package com.example.suav;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;

import com.airmap.airmapsdk.AirMapException;
import com.airmap.airmapsdk.models.pilot.AirMapPilot;
import com.airmap.airmapsdk.networking.callbacks.AirMapCallback;
import com.airmap.airmapsdk.networking.services.AirMap;

public class ProfileActivity extends Activity {

    EditText edtFirstName, edtLastName, edtUserName, edtEmail;
    Button btnSave;
    ProgressBar pgrsPilotLoad;

    AirMapPilot pilot;

    // Must receive auth token from bundle
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        initMenu(); // sets up the menu for this activity

        edtFirstName = (EditText) findViewById(R.id.edtFirstName);
        edtLastName = (EditText) findViewById(R.id.edtLastName);
        edtUserName = (EditText) findViewById(R.id.edtUserName);
        edtEmail = (EditText) findViewById(R.id.edtEmail);
        btnSave = (Button) findViewById(R.id.btnSave);
        pgrsPilotLoad = (ProgressBar) findViewById(R.id.pgrsPlanLoad);

        // Only display loading when this is our first request
        if(savedInstanceState == null)
            pgrsPilotLoad.setVisibility(View.VISIBLE);
        else
            pgrsPilotLoad.setVisibility(View.GONE);

        // User cannot commit the pilot info until the pilot object is received from Airmap
        btnSave.setAlpha(.5f);
        btnSave.setClickable(false);

        Bundle bundle = getIntent().getExtras();

        // We need to make sure the airmap object does not get destroyed and reinit it if it was
        if(!AirMap.hasBeenInitialized()) {
            AirMap.init(getApplicationContext());
            AirMap.setAuthToken(getApplicationContext().getApplicationContext().getSharedPreferences(getString(R.string.auth_preference_file_key), Context.MODE_PRIVATE).getString("auth_token", ""));
        }


        AirMap.getPilot(new AirMapCallback<AirMapPilot>() {
            @Override
            protected void onSuccess(AirMapPilot response) {
                pilot = response;
                pgrsPilotLoad.setVisibility(View.GONE);
                // Only update the text boxes if this is the first time that activity loads
                // We don't want to overwrite user input if it was a destroy->create
                if(savedInstanceState == null)
                    putPilotInfoInTexts();

                btnSave.setAlpha(1f);
                btnSave.setClickable(true);

            }

            @Override
            protected void onError(AirMapException e) {
                Log.e(getResources().getString(R.string.airmap_error), e.toString());
                Toast.makeText(getApplicationContext(), getResources().getString(R.string.profile_error_toast), Toast.LENGTH_LONG).show();
                pgrsPilotLoad.setVisibility(View.GONE);
            }
        });

    }

    /* Places current pilot object's information into the edit texts */
    private void putPilotInfoInTexts() {
        edtFirstName.setText(pilot.getFirstName());
        edtLastName.setText(pilot.getLastName());
        edtEmail.setText(pilot.getEmail());
        edtUserName.setText(pilot.getUsername());
    }

    /* Places information in the edit texts into the current pilot object */
    private void updatePilotInfoFromTexts() {
        pilot.setFirstName(edtFirstName.getText().toString());
        pilot.setLastName(edtLastName.getText().toString());
        pilot.setEmail(edtEmail.getText().toString());
        pilot.setUsername(edtUserName.getText().toString());
    }

    /* Makes a call to Airmap that updates the pilot's profile based on what is in the edit texts */
    public void saveChanges(View v) {
        // First update pilot object
        this.updatePilotInfoFromTexts();

        // Pass in updated pilot object to update user's airmap profile
        AirMap.updatePilot(pilot, new AirMapCallback<AirMapPilot>() {
            @Override
            protected void onSuccess(AirMapPilot response) {
                pilot = response;
                putPilotInfoInTexts();
                Toast.makeText(getApplicationContext(), getResources().getString(R.string.profile_submit_success), Toast.LENGTH_SHORT).show();
            }

            @Override
            protected void onError(AirMapException e) {
                Log.e(getResources().getString(R.string.airmap_error), e.toString());
                Toast.makeText(getApplicationContext(), getResources().getString(R.string.profile_error_toast), Toast.LENGTH_LONG).show();
            }
        });
    }

    /* Maintain user's current input on load even if they have not committed it yet */
    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        edtFirstName.setText(savedInstanceState.getString("firstname"));
        edtLastName.setText(savedInstanceState.getString("lastname"));
        edtEmail.setText(savedInstanceState.getString("email"));
        edtUserName.setText(savedInstanceState.getString("username"));

        // We need to make sure the airmap object does not get destroyed and reinit it if it was
        if(!AirMap.hasBeenInitialized()) {
            AirMap.init(getApplicationContext());
            AirMap.setAuthToken(getApplicationContext().getApplicationContext().getSharedPreferences(getString(R.string.auth_preference_file_key), Context.MODE_PRIVATE).getString("auth_token", ""));
        }
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        outState.putString("AuthToken", AirMap.getAuthToken());

        outState.putString("firstname", edtFirstName.getText().toString());
        outState.putString("lastname", edtLastName.getText().toString());
        outState.putString("email", edtEmail.getText().toString());
        outState.putString("username", edtUserName.getText().toString());

        super.onSaveInstanceState(outState);
    }

    private void initMenu() {
        Toolbar t = (Toolbar) findViewById(R.id.profile_toolbar);
        t.setTitle(getString(R.string.profile_menu_title));
        t.inflateMenu(R.menu.profile_menu);
    }

}