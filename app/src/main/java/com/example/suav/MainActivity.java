package com.example.suav;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

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
                // Retrieve a getInstance from database so we can write to it. Then we get a reference from out tree in database.
                rootNode = FirebaseDatabase.getInstance();
                reference = rootNode.getReference("Pins");

                // Get data and place it in the helper class
                String hello = text.getText().toString();
                writeDatabaseHelper writeHelper = new writeDatabaseHelper(hello);
                // Write to database
                reference.setValue(writeHelper);
            }
        });

    }



}