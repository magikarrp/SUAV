package com.example.suav;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;

import com.airmap.airmapsdk.AirMapException;
import com.airmap.airmapsdk.models.AirMapWeather;
import com.airmap.airmapsdk.models.AirMapWeatherUpdate;
import com.airmap.airmapsdk.models.Coordinate;
import com.airmap.airmapsdk.networking.callbacks.AirMapCallback;
import com.airmap.airmapsdk.networking.services.AirMap;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.JsonRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;

public class WeatherActivity extends Activity {

    TextView txtCondition, txtTemperature, txtPrecip, txtVisibility, txtWind, txtHumidity, txtNumPlanes;
    ProgressBar pgrsWeatherLoad;

    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weather);

        initMenu(); // sets up the menu for this activity

        txtNumPlanes = (TextView) findViewById(R.id.txtNumPlanes);
        txtCondition = (TextView) findViewById(R.id.txtCondition);
        txtTemperature = (TextView) findViewById(R.id.txtTemperature);
        txtPrecip = (TextView) findViewById(R.id.txtPrecip);
        txtVisibility = (TextView) findViewById(R.id.txtVisibility);
        txtWind = (TextView) findViewById(R.id.txtWind);
        txtHumidity = (TextView) findViewById(R.id.txtHumidity);
        pgrsWeatherLoad = (ProgressBar) findViewById(R.id.pgrsPlanLoad);
        pgrsWeatherLoad.setVisibility(View.GONE);

        // Get a start and end time for the user's flight
        Date startTime = new Date();
        Date endTime = new Date(startTime.getTime() + (4 * 60 * 60 * 1000));

        Bundle bundle = getIntent().getExtras();

        // Get coordinate from previous activity
        Coordinate coordinate;
        if(bundle != null && bundle.getDouble("lon") != 0) {
            try {
                coordinate = new Coordinate(bundle.getDouble("lat"),bundle.getDouble("lon"));
            } catch (Exception e) {
                coordinate = new Coordinate(42.35534150531174,-71.06617418626098);
            }

        } else {
            // Default value: Middle of Boston Common
            coordinate = new Coordinate(42.35534150531174,-71.06617418626098);
        }

        // We need to make sure the airmap object does not get destroyed and reinit it if it was
        if(!AirMap.hasBeenInitialized()) {
            AirMap.init(getApplicationContext());
            AirMap.setAuthToken(getApplicationContext().getApplicationContext().getSharedPreferences(getString(R.string.auth_preference_file_key), Context.MODE_PRIVATE).getString("auth_token", ""));
        }

        // Since this page is static, we only need to call the weather API on first creation (i.e. null saved instance state)
        if(savedInstanceState == null) {
            pgrsWeatherLoad.setVisibility(View.VISIBLE);
            // Ping AirMap's weather API
            AirMap.getWeather(coordinate, startTime, endTime, new AirMapCallback<AirMapWeather>() {
                @Override
                protected void onSuccess(AirMapWeather weather) {
                    AirMapWeatherUpdate update = weather.getUpdates().get(0);

                    // Display Weather conditions to the user
                    txtCondition.setText(update.getCondition());
                    txtHumidity.setText(update.getHumidity() * 100 + getResources().getString(R.string.weather_humidity_unit));
                    txtPrecip.setText(update.getPrecipitation() * 100 + getResources().getString(R.string.weather_precipitation_unit));
                    txtTemperature.setText(update.getTemperature() + getResources().getString(R.string.weather_temperature_unit));
                    txtVisibility.setText(update.getVisibility() + getResources().getString(R.string.weather_visibility_unit));
                    txtWind.setText(update.getWind().getSpeed() + getResources().getString(R.string.weather_wind_unit));
                    pgrsWeatherLoad.setVisibility(View.GONE);
                }

                @Override
                protected void onError(AirMapException e) {
                    pgrsWeatherLoad.setVisibility(View.GONE);
                    Log.e(getResources().getString(R.string.airmap_error), e.toString());
                    Toast.makeText(getApplicationContext(), getResources().getString(R.string.weather_error_toast), Toast.LENGTH_LONG).show();
                }
            });
        }
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        txtCondition.setText(savedInstanceState.getString("condition"));
        txtHumidity.setText(savedInstanceState.getString("humidity"));
        txtPrecip.setText(savedInstanceState.getString("precipitation"));
        txtTemperature.setText(savedInstanceState.getString("temperature"));
        txtVisibility.setText(savedInstanceState.getString("visibility"));
        txtWind.setText(savedInstanceState.getString("wind"));

        // We need to make sure the airmap object does not get destroyed and reinit it if it was
        if(!AirMap.hasBeenInitialized()) {
            AirMap.init(getApplicationContext());
            AirMap.setAuthToken(getApplicationContext().getApplicationContext().getSharedPreferences(getString(R.string.auth_preference_file_key), Context.MODE_PRIVATE).getString("auth_token", ""));
        }

    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        outState.putString("condition", txtCondition.getText().toString());
        outState.putString("humidity", txtHumidity.getText().toString());
        outState.putString("precipitation", txtPrecip.getText().toString());
        outState.putString("temperature", txtTemperature.getText().toString());
        outState.putString("visibility", txtVisibility.getText().toString());
        outState.putString("wind", txtWind.getText().toString());

        outState.putString("AuthToken", AirMap.getAuthToken());

        super.onSaveInstanceState(outState);
    }

    private void initMenu() {
        Toolbar t = (Toolbar) findViewById(R.id.weather_toolbar);
        t.setTitle(getString(R.string.weather_menu_title));
        t.inflateMenu(R.menu.default_menu);
        t.setOnMenuItemClickListener(item -> {
            switch(item.getItemId()) {
                case R.id.menu_profile:
                    // GO TO PROFILE
                    Intent toProfile = new Intent(WeatherActivity.this, ProfileActivity.class);
                    startActivity(toProfile);
                    return true;
                default:
                    // Should not happen
                    return true;
            }
        });
    }

    private void setPlaneCount() {

        double lon1 = -71.06617418626098;
        double lat1 = 42.35534150531174;

        // Get coordinate from previous activity
        Bundle bundle = getIntent().getExtras();
        if(bundle != null && bundle.getDouble("lon") != 0) {
            try {
                lon1 = bundle.getDouble("lon");
                lat1 = bundle.getDouble("lat");
            } catch (Exception e) {
                // ...
            }
        }

        double finalLon = lon1;
        double finalLat = lat1;
        JsonRequest request = new JsonObjectRequest(Request.Method.GET, getString(R.string.opensky_url), null,
                (Response.Listener<JSONObject>) response -> {
                    try {

                        int count = 0;

                        // 3) the request was successful, lets try to parse the json data returned and get aircraft states
                        JSONArray states = response.getJSONArray("states");

                        // 4) create a list of aircraft states, each state is represented by a JSONArray
                        ArrayList<JSONArray> stateList = new ArrayList<JSONArray>();
                        for (int i = 0; i < states.length(); i++) {
                            stateList.add((JSONArray) states.get(i));
                        }

                        // check distance of each state
                        for (JSONArray s : stateList) {
                            double lon2 = s.getDouble(5);
                            double lat2 = s.getDouble(6);

                            double R = 6371.0;
                            double dLon = deg2rad(lon2 - finalLon);
                            double dLat = deg2rad(lat2 - finalLat);

                            double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) + Math.cos(deg2rad(finalLat)) * Math.cos(deg2rad(lat2)) + Math.sin(dLon / 2) * Math.sin(dLon / 2);
                            double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
                            double d = R * c;

                            if (d < 10) {
                                count++;
                            }
                        }

                        findViewById(R.id.icPlane).setVisibility(View.VISIBLE);
                        txtNumPlanes.setText(String.valueOf(count));


                    } catch (JSONException e) {

                        // there was an error parsing the json data
                        e.printStackTrace();
                    }
                },
                error -> {
                    // there was an error requesting the states
                }
        );
    }

    private double deg2rad(double deg) {
        return deg * (Math.PI/180);
    }

}

