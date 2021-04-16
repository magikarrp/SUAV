package com.example.suav;

import androidx.appcompat.app.AppCompatActivity;
import com.airmap.airmapsdk.models.pilot.AirMapPilot;
import com.airmap.airmapsdk.models.shapes.AirMapPolygon;
import com.airmap.airmapsdk.networking.services.AirMap;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class LoginActivity extends AppCompatActivity {

    // airmap auth
    private AirMapPilot user;
    private RequestQueue rq;

    // views
    private EditText edtAuthEmail;
    private EditText edtAuthPassword;
    private TextView txtAuthInstructions;
    private TextView txtAuthResult;
    private Button btnAuthLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // set up volley
        rq = Volley.newRequestQueue(this);

        // set up airmap user
        user = null;

        // initialize views
        edtAuthEmail = (EditText) findViewById(R.id.edtAuthEmail);
        edtAuthPassword = (EditText) findViewById(R.id.edtAuthPassword);
        txtAuthInstructions = (TextView) findViewById(R.id.txtAuthInstruction);
        txtAuthResult = (TextView) findViewById(R.id.txtAuthResult);
        btnAuthLogin = (Button) findViewById(R.id.btnAuthLogin);

        // set up login button handler
        btnAuthLogin.setOnClickListener(v -> login());

        // initialize airmap (see /assets/airmap.config.json for client id and api key)
        AirMap.init(LoginActivity.this);
    }

    // login with airmap (invoked by login button)
    private void login() {

        // clear auth response from previous attempt
        txtAuthResult.setText("");

        // create post request to airmap authentication api
        StringRequest request = new StringRequest(Request.Method.POST, getString(R.string.airmap_auth_url),

            // response handler
            response -> {
                // update ui
                txtAuthResult.setText("Login Successful");
                edtAuthEmail.setText("");
                edtAuthPassword.setText("");

                // try to parse response as json object
                try {
                    JSONObject res = new JSONObject(response);
                    String accessToken = res.getString("access_token");
                    Log.i("Response: ", res.toString());
                    // set auth/access token
                    AirMap.setAuthToken(accessToken);
                    Log.i("UserID: ", AirMap.getUserId());
                } catch (JSONException e) {
                    Log.e("JSON ERROR ===>", "START");
                    e.printStackTrace();
                    Log.e("JSON ERROR ===>", "END");
                }
                Log.e("AUTH SUCCESS ===>", response.toString());

                Intent goToPlanning = new Intent(getApplication(), FlightPlanning.class);
                goToPlanning.putExtra("AuthToken", AirMap.getAuthToken());
                startActivity(goToPlanning);
            },

            // error handler
            error -> {
                txtAuthResult.setText("Uh oh, something went wrong.");
                edtAuthEmail.setText("");
                edtAuthPassword.setText("");
                Log.e("AUTH ERROR ===>", "START");
                error.printStackTrace();
                Log.e("AUTH ERROR ===>", "END");
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

    // refresh the auth token so the users session does not expire
    private void refresh() {
        // TODO
    }

}