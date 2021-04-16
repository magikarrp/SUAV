package com.example.suav;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.airmap.airmapsdk.networking.services.AirMap;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class MainActivity extends AppCompatActivity {

    private FirebaseDatabase rootNode;
    private DatabaseReference reference;
    private Button btn;
    private TextView text;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        text = findViewById(R.id.text);
        btn = findViewById(R.id.btn);

        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                // FireBase Setup
                // Retrieve a getInstance from database so we can write to it. Then we get a reference from out tree in database.
                rootNode = FirebaseDatabase.getInstance();
                reference = rootNode.getReference("Pins");

                // Get data and place it in the helper class
                String testUserID = "111101";
                String testLocation = "17.36.3723";
                String testDate = "12.06.2020";
                String testName = text.getText().toString();
                writeDatabaseHelper writeHelper = new writeDatabaseHelper(testUserID, testName, testLocation, testDate);
                // Write to database
                reference.child(testUserID).setValue(writeHelper);

                // Read from database


            }
        });
    }
}



