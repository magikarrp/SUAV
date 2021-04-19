package com.example.suav;

import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.mapbox.geojson.Feature;
import com.mapbox.geojson.FeatureCollection;
import com.mapbox.geojson.Point;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.maps.Style;
import com.mapbox.mapboxsdk.plugins.annotation.SymbolManager;
import com.mapbox.mapboxsdk.style.layers.SymbolLayer;
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource;

import java.util.ArrayList;
import java.util.List;

import static com.mapbox.mapboxsdk.style.layers.Property.ICON_ROTATION_ALIGNMENT_VIEWPORT;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconAllowOverlap;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconIgnorePlacement;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconImage;

/**
 * Display {@link SymbolLayer} icons on the map.
 */
public class MainMapActivity extends AppCompatActivity implements
        OnMapReadyCallback {

    private static final String SOURCE_ID = "SOURCE_ID";
    private static final String ICON_ID = "ICON_ID";
    private static final String LAYER_ID = "LAYER_ID";
    private MapView mapView;
    List<Feature> symbolLayerIconFeatureList = new ArrayList<>();
    private FirebaseDatabase rootNode;
    private DatabaseReference reference;
    private String pinName, pinRating, pinComment;
    private double pinLong, pinLat;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


// Mapbox access token is configured here. This needs to be called either in your application
// object or in the same activity which contains the mapview.
        Mapbox.getInstance(this, getString(R.string.mapbox_access_token));

// This contains the MapView in XML and needs to be called after the access token is configured.
        setContentView(R.layout.main_map_activity);

        mapView = findViewById(R.id.mapView);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);

        //Read from database
        rootNode = FirebaseDatabase.getInstance();
        reference = rootNode.getReference().child("Pins").child("pinz");

        // Read from the database
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                pinName = dataSnapshot.getValue().toString();
                Log.d("TEST", "Value is: " + pinName);
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.w("FAILED", "Failed to read value.", error.toException());
            }
        });
    };
//
//    pinName = ss.getValue(String.class);
//    pinRating = ss.child("pinRating").getValue(String.class);
//    pinComment = ss.child("pinComment").getValue(String.class);
//    pinLat = ss.child("latitude").getValue(double.class);
//    pinLong = ss.child("longitude").getValue(double.class);
//
//                    Log.i("testSuccess ", pinLat + " " + pinLong);
//
//                   symbolLayerIconFeatureList.add(Feature.fromGeometry(
//                           Point.fromLngLat(-57.225365, -33.213144)));
//
//}

    @Override
    public void onMapReady(@NonNull final MapboxMap mapboxMap) {

        Button btnPin = (Button) findViewById(R.id.btnPin);
        btnPin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainMapActivity.this, PinPickerActivity.class);
                startActivity(intent);



            }
        });

//          test cases
//        symbolLayerIconFeatureList.add(Feature.fromGeometry(
//                Point.fromLngLat(-57.225365, -33.213144)));
//        symbolLayerIconFeatureList.add(Feature.fromGeometry(
//                Point.fromLngLat(-54.14164, -33.981818)));
//        symbolLayerIconFeatureList.add(Feature.fromGeometry(
//                Point.fromLngLat(-56.990533, -30.583266)));

        mapboxMap.setStyle(new Style.Builder().fromUri("mapbox://styles/mapbox/cjf4m44iw0uza2spb3q0a7s41")

/////////////////////////////Generate an on-touch event that will autogenerate pins on user touch////////////////////////////////////////
/////create a new list for coordinates generated by an on-touch event -> save for a specific flight plan////////////////////////////
///////add to a list of saved coordinates 'mypins'////////////////////////////////////////////////////////

// Add the SymbolLayer icon image to the map style
                .withImage(ICON_ID, BitmapFactory.decodeResource(
                        MainMapActivity.this.getResources(), R.drawable.mapbox_marker_icon_default))

// Adding a GeoJson source for the SymbolLayer icons.
                .withSource(new GeoJsonSource(SOURCE_ID,
                        FeatureCollection.fromFeatures(symbolLayerIconFeatureList)))

// Adding the actual SymbolLayer to the map style. An offset is added that the bottom of the red
// marker icon gets fixed to the coordinate, rather than the middle of the icon being fixed to
// the coordinate point. This is offset is not always needed and is dependent on the image
// that you use for the SymbolLayer icon.
                .withLayer(new SymbolLayer(LAYER_ID, SOURCE_ID)
                        .withProperties(
                                iconImage(ICON_ID),
                                iconAllowOverlap(true),
                                iconIgnorePlacement(true)
                        )
                ), new Style.OnStyleLoaded() {
            @Override
            public void onStyleLoaded(@NonNull Style style) {

// Map is set up and the style has loaded. Now you can add additional data or make other map adjustments.
//////////////////////////////////////////////display user location on map load/////////////////////////////////////////////
                // create symbol manager object
                SymbolManager symbolManager = new SymbolManager(mapView, mapboxMap, style);


// set non-data-driven properties, such as:
                symbolManager.setIconAllowOverlap(true);
                symbolManager.setIconTranslate(new Float[]{-4f,5f});
                symbolManager.setIconRotationAlignment(ICON_ROTATION_ALIGNMENT_VIEWPORT);

            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    protected void onStart() {
        super.onStart();
        mapView.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mapView.onStop();
    }

    @Override
    public void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }
}
