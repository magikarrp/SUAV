package com.example.suav;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.airmap.airmapsdk.AirMapException;
import com.airmap.airmapsdk.models.pilot.AirMapPilot;
import com.airmap.airmapsdk.networking.callbacks.AirMapCallback;
import com.airmap.airmapsdk.networking.services.AirMap;

public class ProfileActivity extends Activity {

    EditText edtFirstName, edtLastName, edtUserName, edtEmail;
    TextView txtFirstName, txtLastName, txtUserName, txtEmail;
    Button btnSave;
    ProgressBar pgrsPilotLoad;

    AirMapPilot pilot;

    // Must receive auth token from bundle
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        txtFirstName = (TextView) findViewById(R.id.txtFirstName);
        txtLastName = (TextView) findViewById(R.id.txtLastName);
        txtUserName = (TextView) findViewById(R.id.txtUserName);
        txtEmail = (TextView) findViewById(R.id.txtEmail);
        edtFirstName = (EditText) findViewById(R.id.edtFirstName);
        edtLastName = (EditText) findViewById(R.id.edtLastName);
        edtUserName = (EditText) findViewById(R.id.edtUserName);
        edtEmail = (EditText) findViewById(R.id.edtEmail);
        btnSave = (Button) findViewById(R.id.btnSave);
        pgrsPilotLoad = (ProgressBar) findViewById(R.id.pgrsWeatherLoad);

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
            if(savedInstanceState == null)
                AirMap.setAuthToken(bundle.getString("AuthToken"));
            else
                AirMap.setAuthToken(savedInstanceState.getString("AuthToken"));
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
                Log.e("Airmap error", e.toString());
                Toast.makeText(getApplicationContext(), "Error connecting to the AirMap Pilot API, please try again later", Toast.LENGTH_LONG).show();
                pgrsPilotLoad.setVisibility(View.GONE);
            }
        });

    }

    private void putPilotInfoInTexts() {
        edtFirstName.setText(pilot.getFirstName());
        edtLastName.setText(pilot.getLastName());
        edtEmail.setText(pilot.getEmail());
        edtUserName.setText(pilot.getUsername());
    }

    private void updatePilotInfoFromTexts() {
        pilot.setFirstName(edtFirstName.getText().toString());
        pilot.setLastName(edtLastName.getText().toString());
        pilot.setEmail(edtEmail.getText().toString());
        pilot.setUsername(edtUserName.getText().toString());
    }

    public void saveChanges(View v) {
        this.updatePilotInfoFromTexts();
        AirMap.updatePilot(pilot, new AirMapCallback<AirMapPilot>() {
            @Override
            protected void onSuccess(AirMapPilot response) {
                pilot = response;
                putPilotInfoInTexts();
                Toast.makeText(getApplicationContext(), "Information Updated!", Toast.LENGTH_SHORT).show();
            }

            @Override
            protected void onError(AirMapException e) {
                Log.e("Airmap error", e.toString());
                Toast.makeText(getApplicationContext(), "Error connecting to the AirMap Pilot API, please try again later", Toast.LENGTH_LONG).show();
            }
        });
    }

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
            AirMap.setAuthToken(savedInstanceState.getString("AuthToken"));
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

}