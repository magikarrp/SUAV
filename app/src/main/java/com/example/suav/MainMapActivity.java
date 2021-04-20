package com.example.suav;

import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.mapbox.geojson.Feature;
import com.mapbox.geojson.FeatureCollection;
import com.mapbox.geojson.Point;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.location.LocationComponent;
import com.mapbox.mapboxsdk.location.LocationComponentActivationOptions;
import com.mapbox.mapboxsdk.location.modes.CameraMode;
import com.mapbox.mapboxsdk.location.modes.RenderMode;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.maps.Style;
import com.mapbox.mapboxsdk.plugins.annotation.OnSymbolClickListener;
import com.mapbox.mapboxsdk.plugins.annotation.OnSymbolDragListener;
import com.mapbox.mapboxsdk.plugins.annotation.Symbol;
import com.mapbox.mapboxsdk.plugins.annotation.SymbolManager;
import com.mapbox.mapboxsdk.style.layers.SymbolLayer;
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource;
import com.mapbox.android.core.permissions.PermissionsListener;
import com.mapbox.android.core.permissions.PermissionsManager;

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
        OnMapReadyCallback, PermissionsListener {

    private static final String SOURCE_ID = "SOURCE_ID";
    private static final String ICON_ID = "ICON_ID";
    private static final String LAYER_ID = "LAYER_ID";
    private MapView mapView;
    private MapboxMap mapboxMap;
    private PermissionsManager permissionsManager;
    List<Feature> symbolLayerIconFeatureList = new ArrayList<>();
    private FirebaseDatabase rootNode;
    private DatabaseReference reference;
    private String pinName, pinRating, pinComment;
    private double pinLong, pinLat;
    private Symbol symbol;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Mapbox access token is configured here. This needs to be called either in your application
        // object or in the same activity which contains the mapview.
        Mapbox.getInstance(this, getString(R.string.mapbox_access_token));

        // This contains the MapView in XML and needs to be called after the access token is configured.
        setContentView(R.layout.main_map_activity);

        initMenu();

        mapView = findViewById(R.id.mapView);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);

        //Read from database
        rootNode = FirebaseDatabase.getInstance();
        reference = rootNode.getReference().child("Pins").child("Pins");

        Button btnDropMark = (Button) findViewById(R.id.btnDropMark);
        btnDropMark.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainMapActivity.this, PinPickerActivity.class);
                startActivity(intent);
            }
        });

        Button btnEvents = (Button) findViewById(R.id.btnEvents);
        btnEvents.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainMapActivity.this, readDatabaseUsers.class);
                startActivity(intent);
            }
        });

        Button btnPlanFlight = (Button) findViewById(R.id.btnPlanFlight);
        btnPlanFlight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainMapActivity.this, FlightPathPicker.class);
                startActivity(intent);


            }
        });
    }

    @Override
    public void onMapReady(@NonNull final MapboxMap mapboxMap) {
        MainMapActivity.this.mapboxMap = mapboxMap;


        mapboxMap.setStyle(new Style.Builder().fromUri("mapbox://styles/mapbox/cjf4m44iw0uza2spb3q0a7s41")

// Add the SymbolLayer icon image to the map style
                .withImage(ICON_ID, BitmapFactory.decodeResource(
                        MainMapActivity.this.getResources(), R.drawable.red_marker))

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
                enableLocationComponent(style);

// Map is set up and the style has loaded. Now you can add additional data or make other map adjustments.

                // create symbol manager object
                SymbolManager symbolManager = new SymbolManager(mapView, mapboxMap, style);
                // set non-data-driven properties, such as:
                symbolManager.setIconAllowOverlap(true);
                symbolManager.setIconTranslate(new Float[]{-4f, 5f});
                symbolManager.setIconRotationAlignment(ICON_ROTATION_ALIGNMENT_VIEWPORT);




                reference.addValueEventListener(new ValueEventListener() {
                    @Override
                    // For loop grabs children of branch and then drops pin by reverse geo-caching w/ symbolLayerIconFeatureList
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        for (DataSnapshot ss : dataSnapshot.getChildren()) {
                            String name = reference.getKey();
                            pinRating = ss.child("pinRating").getValue(String.class);
                            pinComment = ss.child("pinComment").getValue(String.class);
                            pinLat = ss.child("latitude").getValue(double.class);
                            pinLong = ss.child("longitude").getValue(double.class);


                            symbolLayerIconFeatureList.add(Feature.fromGeometry(Point.fromLngLat(pinLong, pinLat)));


                            mapboxMap.setStyle(new Style.Builder().fromUri("mapbox://styles/mapbox/cjf4m44iw0uza2spb3q0a7s41")
                                    .withImage(ICON_ID, BitmapFactory.decodeResource(
                                            MainMapActivity.this.getResources(), R.drawable.red_marker))
                                    .withSource(new GeoJsonSource(SOURCE_ID,
                                            FeatureCollection.fromFeatures(symbolLayerIconFeatureList)))
                                    .withLayer(new SymbolLayer(LAYER_ID, SOURCE_ID)
                                            .withProperties(
                                                    iconImage(ICON_ID),
                                                    iconAllowOverlap(true),
                                                    iconIgnorePlacement(true)
                                            )
                                    ), new Style.OnStyleLoaded() {
                                @Override
                                public void onStyleLoaded(@NonNull Style style) {    ///reload map
                                    symbolManager.addClickListener(new OnSymbolClickListener() {
                                        @Override
                                        public boolean onAnnotationClick(Symbol symbol) {
                                            Toast.makeText(MainMapActivity.this,
                                                    "selected", Toast.LENGTH_SHORT).show();
                                            if(symbol.getIconImage()=="red_marker") {
                                                Toast.makeText(MainMapActivity.this,
                                                        "selected", Toast.LENGTH_SHORT).show();
                                                symbol.setIconImage("blue_marker");
                                                symbolManager.update(symbol);
                                            }
                                            else {
                                                Toast.makeText(MainMapActivity.this,
                                                        "unselected", Toast.LENGTH_SHORT).show();
                                                symbol.setIconImage("blue_marker");
                                                symbolManager.update(symbol);
                                            }
                                            return true;

                                        }
                                    });

                                }});
                        }

                    }

                    @Override
                    public void onCancelled(DatabaseError error) {
                        // Failed to read value
                        Log.w("FAILED", "Failed to read value.", error.toException());
                    }
                });




            }
        });
    }



    @SuppressWarnings( {"MissingPermission"})
    private void enableLocationComponent(@NonNull Style loadedMapStyle) {
// Check if permissions are enabled and if not request
        if (PermissionsManager.areLocationPermissionsGranted(this)) {

// Get an instance of the component
            LocationComponent locationComponent = mapboxMap.getLocationComponent();

// Activate with options
            locationComponent.activateLocationComponent(
                    LocationComponentActivationOptions.builder(this, loadedMapStyle).build());

// Enable to make component visible
            locationComponent.setLocationComponentEnabled(true);

// Set the component's camera mode
            locationComponent.setCameraMode(CameraMode.TRACKING);

// Set the component's render mode
            locationComponent.setRenderMode(RenderMode.COMPASS);
        } else {
            permissionsManager = new PermissionsManager(this);
            permissionsManager.requestLocationPermissions(this);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        permissionsManager.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    public void onExplanationNeeded(List<String> permissionsToExplain) {
        Toast.makeText(this, R.string.user_location_permission_explanation, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onPermissionResult(boolean granted) {
        if (granted) {
            mapboxMap.getStyle(new Style.OnStyleLoaded() {
                @Override
                public void onStyleLoaded(@NonNull Style style) {
                    enableLocationComponent(style);
                }
            });
        } else {
            Toast.makeText(this, R.string.user_location_permission_not_granted, Toast.LENGTH_LONG).show();
            finish();
        }
    }

    @Override
    @SuppressWarnings( {"MissingPermission"})
    protected void onStart() {
        super.onStart();
        mapView.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();
        mapView.onResume();
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

    private void initMenu() {
        Toolbar t = (Toolbar) findViewById(R.id.mm_toolbar);
        t.setTitle(getString(R.string.mm_menu_title));
        t.inflateMenu(R.menu.default_menu);
        t.setOnMenuItemClickListener(item -> {
            switch(item.getItemId()) {
                case R.id.menu_profile:
                    // GO TO PROFILE
                    Intent toProfile = new Intent(MainMapActivity.this, ProfileActivity.class);
                    startActivity(toProfile);
                    return true;
                default:
                    // Should not happen
                    return true;
            }
        });
    }
}
