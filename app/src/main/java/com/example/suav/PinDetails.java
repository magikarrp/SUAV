package com.example.suav;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import org.w3c.dom.Text;

public class PinDetails extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.pin_details);

        Button btnSubPin = (Button) findViewById(R.id.btnSubPin);
        EditText edtPinName = (EditText) findViewById(R.id.edtPinName);
        EditText edtPinRating = (EditText) findViewById(R.id.edtPinRating);
        EditText edtComment = (EditText) findViewById(R.id.edtComment);
        TextView txtLat = (TextView) findViewById(R.id.txtLat);
        TextView txtLong = (TextView) findViewById(R.id.txtLong);


        txtLat.setText(getIntent().getExtras().getString("lat"));
        txtLong.setText(getIntent().getExtras().getString("lon"));

        btnSubPin.setOnClickListener (new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //raymond, this is where you want to upload pin data to database

                Intent intent = new Intent(PinDetails.this, MainMapActivity.class);
                startActivity(intent);
            }
        });



    }

}
