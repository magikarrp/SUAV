package com.example.suav;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.w3c.dom.Text;

public class PinDetails extends AppCompatActivity {


    private FirebaseDatabase rootNode;
    private DatabaseReference reference;
    private double lon, lat;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.pin_details);

        Button btnSubPin = (Button) findViewById(R.id.btnSubPin);
        EditText edtPinName = (EditText) findViewById(R.id.edtPinName);             //not null
        EditText edtPinRating = (EditText) findViewById(R.id.edtPinRating);         //not null
        EditText edtComment = (EditText) findViewById(R.id.edtComment);             //not null
        TextView txtAdd = (TextView) findViewById(R.id.txtAdd);
        CheckBox checkPublic = (CheckBox) findViewById(R.id.checkPublic);
        TextView txtDesc = (TextView) findViewById(R.id.txtDesc);

        initMenu();

        txtAdd.setText(getIntent().getExtras().getString("address"));

        this.lat = getIntent().getExtras().getDouble("lat");
        this.lon = getIntent().getExtras().getDouble("lon");

        btnSubPin.setOnClickListener (new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //Write to database
                rootNode = FirebaseDatabase.getInstance();
                reference = rootNode.getReference("Pins").child("Pins");

                //Convert data to string for easier database storage
                String pinName = edtPinName.getText().toString();
                String pinRating = "Rating: " + edtPinRating.getText().toString();
                String pinComment = "Comments: " + edtComment.getText().toString();


                writeDatabaseHelper writeHelper = new writeDatabaseHelper(pinRating, pinComment, lat, lon);
                reference.child(pinName).setValue(writeHelper);

                Intent intent = new Intent(PinDetails.this, MainMapActivity.class);
                startActivity(intent);
            }
        });



    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public void setLon(double lon) {
        this.lon = lon;
    }

    private void initMenu() {
        Toolbar t = (Toolbar) findViewById(R.id.pd_toolbar);
        t.setTitle(getString(R.string.pd_menu_title));
        t.inflateMenu(R.menu.default_menu);
        t.setOnMenuItemClickListener(item -> {
            switch(item.getItemId()) {
                case R.id.menu_profile:
                    // GO TO PROFILE
                    Intent toProfile = new Intent(PinDetails.this, ProfileActivity.class);
                    startActivity(toProfile);
                    return true;
                default:
                    // Should not happen
                    return true;
            }
        });
    }
}
