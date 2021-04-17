package com.example.suav;

import androidx.appcompat.app.AppCompatActivity;
import com.airmap.airmapsdk.models.pilot.AirMapPilot;
import com.airmap.airmapsdk.models.shapes.AirMapPolygon;
import com.airmap.airmapsdk.networking.services.AirMap;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;


import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class LoginActivity extends AppCompatActivity {



    // airmap auth
    private RequestQueue rq;
    private String authToken;
    private String refreshToken;

    // views
    private EditText edtAuthEmail;
    private EditText edtAuthPassword;
    private TextView txtAuthInstructions;
    private TextView txtAuthResult;
    private Button btnAuthLogin;



    // START OF LIFECYCLE FUNCTIONS >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // when the activity is created just set the tokens to empty strings
        authToken = "";
        refreshToken = "";

        // uncomment this line to run the app as if there was nothing in persistent storage (clears persistent storage during onCreate)
        // saveData();

        // set up volley request queue
        rq = Volley.newRequestQueue(this);

        // initialize views
        edtAuthEmail = (EditText) findViewById(R.id.edtAuthEmail);
        edtAuthPassword = (EditText) findViewById(R.id.edtAuthPassword);
        txtAuthInstructions = (TextView) findViewById(R.id.txtAuthInstruction);
        txtAuthResult = (TextView) findViewById(R.id.txtAuthResult);
        btnAuthLogin = (Button) findViewById(R.id.btnAuthLogin);

        // disabled ui by default until we know user auth token cannot be refreshed (see onResume)
        setEnabled(false);

        // set up login button handler
        btnAuthLogin.setOnClickListener(v -> login());

        // initialize airmap (see /assets/airmap.config.json for client id and api key), auth token will be set later
        AirMap.init(LoginActivity.this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.e("AUTH ON PAUSE ===>", "===== ===== ===== ===== =====");
        saveData();
    }

    @Override
    protected void onResume() {
        super.onResume();

        // disabled ui by default until we know user auth token cannot be refreshed
        setEnabled(false);

        // read auth token and refresh token from persistent storage
        authToken = readData("auth_token");
        refreshToken = readData("refresh_token");

        Log.e("AUTH ON RESUME ===>", "AT=" + authToken + ", RT=" + refreshToken);

        // if we have tokens then refresh, otherwise must wait for user to login
        if (!authToken.equals("") && !refreshToken.equals("")) {
            refresh();
        } else {
            setEnabled(true);
        }
    }

    // END OF LIFECYCLE FUNCTIONS <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<



    // START OF login() >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
    // login with airmap (invoked by login button)
    private void login() {

        // clear response from previous attempt
        txtAuthResult.setText("");

        // create post request to airmap authentication api
        StringRequest request = new StringRequest(Request.Method.POST, getString(R.string.airmap_auth_url),

            // response handler
            response -> {

                // update ui
                txtAuthResult.setText(getString(R.string.auth_success));
                edtAuthEmail.setText("");
                edtAuthPassword.setText("");

                // try to handle response
                try {

                    // parse out values
                    JSONObject res = new JSONObject(response);
                    authToken = res.getString("access_token");
                    refreshToken = res.getString("refresh_token");

                    // save tokens to persistent storage
                    saveData();

                    // set auth/access token for airmap
                    AirMap.setAuthToken(authToken);

                    // log event
                    Log.e("AUTH SUCCESS ===>", response.toString());

                    // disabled ui since user is logged in
                    setEnabled(false);

                    // start main
                    Intent goToPlanning = new Intent(getApplication(), ProfileActivity.class);
                    goToPlanning.putExtra("AuthToken", AirMap.getAuthToken());
                    startActivity(goToPlanning);

                } catch (JSONException e) {
                    // error handler for json parsing

                    // update ui
                    txtAuthResult.setText(getString(R.string.auth_error));
                    edtAuthEmail.setText("");
                    edtAuthPassword.setText("");

                    // log error
                    Log.e("JSON ERROR ===>", "START");
                    e.printStackTrace();
                    Log.e("JSON ERROR ===>", "END");

                    // make sure ui is enabled for normal login
                    setEnabled(true);
                }

            },

            // error handler for volley request
            error -> {

                // update ui
                txtAuthResult.setText(getString(R.string.auth_error));
                edtAuthEmail.setText("");
                edtAuthPassword.setText("");

                // log error
                Log.e("AUTH LOGIN ERROR ===>", "START");
                error.printStackTrace();
                Log.e("AUTH LOGIN ERROR ===>", "END");

                // make sure ui is enabled for normal login
                setEnabled(true);
            }
        ) {
            // create post parameters required by airmap for authentication
            @Override public Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();
                params.put("grant_type", "password");
                params.put("client_id", getString(R.string.airmap_client_id));
                params.put("username", "jamespel@bu.edu");//edtAuthEmail.getText().toString());
                params.put("password", "CS501airmap*");//edtAuthPassword.getText().toString());
                params.put("scope", "openid");
                return params;
            }
        };

        // add request to queue so volley will send it
        rq.add(request);
    }
    // END OF login() <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<



    // START OF refresh() >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
    // refresh the auth token so the users session does not expire
    // invoked from onResume() if refresh token is in persistent storage
    private void refresh() {

        // create post request to airmap authentication api
        StringRequest request = new StringRequest(Request.Method.POST, getString(R.string.airmap_auth_url),

                // response handler for volley request
                response -> {

                    // try to handle response
                    try {

                        // parse out values
                        JSONObject res = new JSONObject(response);
                        authToken = res.getString("access_token");
                        refreshToken = res.getString("refresh_token");

                        saveData();

                        // set auth/access token
                        AirMap.setAuthToken(authToken);

                        // log
                        Log.e("AUTH REFRESH SUCCESS ===>", response);

                        // disabled ui since user is logged in
                        setEnabled(false);

                        // start main
                        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                        startActivity(intent);

                    } catch (JSONException e) {
                        // error handler for json parsing

                        // log error
                        Log.e("JSON ERROR ===>", "START");
                        e.printStackTrace();
                        Log.e("JSON ERROR ===>", "END");

                        // make sure ui is enabled for normal login
                        setEnabled(true);
                    }
                },

                // error handler for volley request
                error -> {

                    // log error
                    Log.e("AUTH REFRESH ERROR ===>", "START");
                    error.printStackTrace();
                    Log.e("AUTH REFRESH ERROR ===>", "END");

                    // enable ui for normal login
                    setEnabled(true);
                }

        ) {

            // create post parameters required by airmap for authentication
            @Override public Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();
                params.put("grant_type", "refresh_token");
                params.put("client_id", getString(R.string.airmap_client_id));
                params.put("refresh_token", refreshToken);
                return params;
            }

        };

        // add request to queue so volley will send it
        rq.add(request);
    }
    // END OF refresh() <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<



    // save tokens to persistent storage
    private void saveData() {
        SharedPreferences.Editor spe = getApplicationContext().getSharedPreferences(getString(R.string.auth_preference_file_key), Context.MODE_PRIVATE).edit();
        spe.putString("auth_token", authToken);
        spe.putString("refresh_token", refreshToken);
        spe.apply();
    }



    // read a value from persistent storage
    private String readData(String key) {
        SharedPreferences sp = getApplicationContext().getSharedPreferences(getString(R.string.auth_preference_file_key), Context.MODE_PRIVATE);
        return sp.getString(key, "");
    }



    // enable or disable ui
    private void setEnabled(boolean b) {
        edtAuthEmail.setVisibility(b ? View.VISIBLE : View.INVISIBLE);
        edtAuthEmail.setEnabled(b);
        edtAuthPassword.setVisibility(b ? View.VISIBLE : View.INVISIBLE);
        edtAuthPassword.setEnabled(b);
        txtAuthInstructions.setVisibility(b ? View.VISIBLE : View.INVISIBLE);
        txtAuthInstructions.setEnabled(b);
        txtAuthResult.setVisibility(b ? View.VISIBLE : View.INVISIBLE);
        txtAuthResult.setEnabled(b);
        btnAuthLogin.setVisibility(b ? View.VISIBLE : View.INVISIBLE);
        btnAuthLogin.setEnabled(b);
    }

}