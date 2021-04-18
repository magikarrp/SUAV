package com.example.suav;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.airmap.airmapsdk.AirMapException;
import com.airmap.airmapsdk.models.AirMapWeather;
import com.airmap.airmapsdk.models.AirMapWeatherUpdate;
import com.airmap.airmapsdk.models.Coordinate;
import com.airmap.airmapsdk.networking.callbacks.AirMapCallback;
import com.airmap.airmapsdk.networking.services.AirMap;

import java.util.Date;

public class WeatherActivity extends Activity {

    TextView txtCondition, txtTemperature, txtPrecip, txtVisibility, txtWind, txtHumidity;
    ProgressBar pgrsWeatherLoad;

    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weather);

        txtCondition = (TextView) findViewById(R.id.txtCondition);
        txtTemperature = (TextView) findViewById(R.id.txtTemperature);
        txtPrecip = (TextView) findViewById(R.id.txtPrecip);
        txtVisibility = (TextView) findViewById(R.id.txtVisibility);
        txtWind = (TextView) findViewById(R.id.txtWind);
        txtHumidity = (TextView) findViewById(R.id.txtHumidity);
        pgrsWeatherLoad = (ProgressBar) findViewById(R.id.pgrsPlanLoad);
        pgrsWeatherLoad.setVisibility(View.GONE);

        // Middle of Boston Common
        Coordinate coordinate = new Coordinate(42.35534150531174,-71.06617418626098);

        // Get a start and end time for the user's flight
        Date startTime = new Date();
        Date endTime = new Date(startTime.getTime() + (4 * 60 * 60 * 1000));

        Bundle bundle = getIntent().getExtras();

        // We need to make sure the airmap object does not get destroyed and reinit it if it was
        if(!AirMap.hasBeenInitialized()) {
            AirMap.init(getApplicationContext());
            if(savedInstanceState == null)
                AirMap.setAuthToken(bundle.getString("AuthToken"));
            else
                AirMap.setAuthToken(savedInstanceState.getString("AuthToken"));
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
            AirMap.setAuthToken(savedInstanceState.getString("AuthToken"));
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
}

