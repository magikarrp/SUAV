package com.example.suav;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.util.Log;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.airmap.airmapsdk.AirMapException;
import com.airmap.airmapsdk.models.Coordinate;
import com.airmap.airmapsdk.models.flight.AirMapFlightPlan;
import com.airmap.airmapsdk.models.pilot.AirMapPilot;
import com.airmap.airmapsdk.models.rules.AirMapRuleset;
import com.airmap.airmapsdk.models.shapes.AirMapPolygon;
import com.airmap.airmapsdk.networking.callbacks.AirMapCallback;
import com.airmap.airmapsdk.networking.services.AirMap;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static com.airmap.airmapsdk.models.shapes.AirMapGeometry.getGeoJSONFromGeometry;

public class ProfileActivity extends Activity {

    EditText edtFirstName;
    TextView txtFirstName;

    // Must receive auth token from bundle
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        txtFirstName = (TextView) findViewById(R.id.txtFirstName);
        edtFirstName = (EditText) findViewById(R.id.edtFirstName);

        Bundle bundle = getIntent().getExtras();

        if(!AirMap.hasBeenInitialized()) {
            AirMap.init(getApplicationContext());
            if(savedInstanceState == null)
                AirMap.setAuthToken(bundle.getString("AuthToken"));
            else
                AirMap.setAuthToken(savedInstanceState.getString("AuthToken"));
            Log.i("New init", AirMap.getUserId());
        }

        AirMap.getPilot(new AirMapCallback<AirMapPilot>() {
            @Override
            protected void onSuccess(AirMapPilot response) {
                edtFirstName.setText(response.getFirstName());
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