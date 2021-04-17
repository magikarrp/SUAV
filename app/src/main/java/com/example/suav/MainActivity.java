package com.example.suav;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;


import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class MainActivity extends AppCompatActivity {

    private FirebaseDatabase rootNode;
    private DatabaseReference reference;
    private Button btn;
    private Button pinPrivacy;
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
                reference = rootNode.getReference().child("Pins").child("111001");

                // Get data and place it in the helper class
                String testUserID = "111001";
                String testLocation = "17.36.3723";
                String testDate = "12.06.2020";
                String testName = text.getText().toString();

                // WRITE to database
                writeDatabaseHelper writeHelper = new writeDatabaseHelper(testUserID, testName, testLocation, testDate);
                reference.child(testUserID).setValue(writeHelper);

                // READ from database by using .get() for AddonCompleteListener method

                reference.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        // This method is called once with the initial value and again
                        // whenever data at this location is updated.
                        // We are reading data from our variable reference .child path
                        //String value = dataSnapshot.child("Pins").getValue().toString();
                        String loc = dataSnapshot.child("location").getValue().toString();
                        Log.d("firebase", "Value is: " + loc);
                        text.setText(loc);
                    }

                    @Override
                    public void onCancelled(DatabaseError error) {
                        // Failed to read value if we cannot grab value
                        Log.w("firebase", "Failed to read value.", error.toException());
                    }
                });

            }
        }


        );
    }
}



