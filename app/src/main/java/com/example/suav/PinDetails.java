package com.example.suav;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.w3c.dom.Text;

public class PinDetails extends AppCompatActivity {


    private FirebaseDatabase rootNode;
    private DatabaseReference reference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.pin_details);

        Button btnSubPin = (Button) findViewById(R.id.btnSubPin);
        EditText edtPinName = (EditText) findViewById(R.id.edtPinName);             //not null
        EditText edtPinRating = (EditText) findViewById(R.id.edtPinRating);         //not null
        EditText edtComment = (EditText) findViewById(R.id.edtComment);             //not null
        TextView txtLat = (TextView) findViewById(R.id.txtLat);
        TextView txtLong = (TextView) findViewById(R.id.txtLong);
        CheckBox checkPublic = (CheckBox) findViewById(R.id.checkPublic);



        txtLat.setText(getIntent().getExtras().getString("lat"));
        txtLong.setText(getIntent().getExtras().getString("lon"));

        btnSubPin.setOnClickListener (new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //raymond, this is where you want to upload pin data to database
                rootNode = FirebaseDatabase.getInstance();
                reference = rootNode.getReference("Pins");

                //Convert data to string for easier database storage
                String pinName = edtPinName.getText().toString();
                String pinRating = "Rating: " + edtPinRating.getText().toString();
                String pinComment = "Comments: " + edtComment.getText().toString();
                String takeOffCoordinateString = txtLat.getText().toString() + ", "  + txtLong.getText().toString();

                writeDatabaseHelper writeHelper = new writeDatabaseHelper(pinRating, pinRating, takeOffCoordinateString, pinComment;
                reference.child(pinName).setValue(writeHelper);

                Intent intent = new Intent(PinDetails.this, MainMapActivity.class);
                startActivity(intent);
            }
        });



    }

}
