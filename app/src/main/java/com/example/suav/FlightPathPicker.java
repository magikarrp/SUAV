package com.example.suav;

import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;

import com.mapbox.android.core.permissions.PermissionsListener;
import com.mapbox.android.core.permissions.PermissionsManager;
import com.mapbox.api.geocoding.v5.GeocodingCriteria;
import com.mapbox.api.geocoding.v5.MapboxGeocoding;
import com.mapbox.api.geocoding.v5.models.CarmenFeature;
import com.mapbox.api.geocoding.v5.models.GeocodingResponse;
import com.mapbox.core.exceptions.ServicesException;
import com.mapbox.geojson.Feature;
import com.mapbox.geojson.FeatureCollection;
import com.mapbox.geojson.Point;
import com.mapbox.geojson.Polygon;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.annotations.MarkerOptions;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.location.LocationComponent;
import com.mapbox.mapboxsdk.location.LocationComponentActivationOptions;
import com.mapbox.mapboxsdk.location.modes.CameraMode;
import com.mapbox.mapboxsdk.location.modes.RenderMode;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.maps.Style;
import com.mapbox.mapboxsdk.plugins.annotation.FillManager;
import com.mapbox.mapboxsdk.plugins.annotation.FillOptions;
import com.mapbox.mapboxsdk.plugins.annotation.Symbol;
import com.mapbox.mapboxsdk.plugins.annotation.SymbolManager;
import com.mapbox.mapboxsdk.plugins.annotation.SymbolOptions;
import com.mapbox.mapboxsdk.style.layers.FillLayer;
import com.mapbox.mapboxsdk.style.layers.Layer;
import com.mapbox.mapboxsdk.style.layers.PropertyFactory;
import com.mapbox.mapboxsdk.style.layers.SymbolLayer;
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource;
import com.mapbox.mapboxsdk.utils.BitmapUtils;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import timber.log.Timber;

import static com.mapbox.mapboxsdk.style.layers.Property.ICON_ROTATION_ALIGNMENT_VIEWPORT;
import static com.mapbox.mapboxsdk.style.layers.Property.NONE;
import static com.mapbox.mapboxsdk.style.layers.Property.VISIBLE;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.fillColor;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconAllowOverlap;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconIgnorePlacement;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconImage;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.visibility;

//import com.mapbox.mapboxandroiddemo.R;

/**
 * Drop a marker at a specific location and then perform
 * reverse geocoding to retrieve and display the location's address
 */
public class FlightPathPicker extends AppCompatActivity implements PermissionsListener, OnMapReadyCallback {

    private static final String DROPPED_MARKER_LAYER_ID = "DROPPED_MARKER_LAYER_ID";
    private MapView mapView;
    private MapboxMap mapboxMap;
    private Button btnDropMark, btnMenu, btnConfirm, btnRemoveMark, btnEdtPath, btnFlightPlan;

    private PermissionsManager permissionsManager;
    private ImageView hoveringMarker;
    private ArrayList<LatLng> path;
    private List<Feature> symbolLayer;

    private List<List<Point>> POINTS;
    private List<Point> OUTER_POINTS;

    private static final String SOURCE_ID = "SOURCE_ID";
    private static final String ICON_ID = "ICON_ID";
    private static final String LAYER_ID = "LAYER_ID";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Mapbox access token is configured here. This needs to be called either in your application
        // object or in the same activity which contains the mapview.
        Mapbox.getInstance(this, getString(R.string.mapbox_access_token));

        // This contains the MapView in XML and needs to be called after the access token is configured.
        setContentView(R.layout.flight_path_picker);

        initMenu();

        // Initialize the mapboxMap view
        mapView = findViewById(R.id.mapView);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);


        btnDropMark = (Button) findViewById(R.id.btnDropMark);
        btnRemoveMark = (Button) findViewById(R.id.btnRemoveMark);
        btnRemoveMark.setVisibility(View.GONE);
        btnConfirm = (Button) findViewById(R.id.btnConfirm);
        btnConfirm.setVisibility(View.GONE);
        btnFlightPlan = (Button) findViewById(R.id.btnFlightPlan);
        btnFlightPlan.setVisibility(View.GONE);
        btnEdtPath = (Button) findViewById(R.id.btnEditPath);
        btnEdtPath.setVisibility(View.GONE);
        POINTS = new ArrayList<>();
        OUTER_POINTS = new ArrayList<>();

        path = new ArrayList<>();
        symbolLayer = new ArrayList<>();

        btnFlightPlan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // Use the map camera target's coordinates to make a reverse geocoding search
                //reverseGeocode(Point.fromLngLat(mapTargetLatLng.getLongitude(), mapTargetLatLng.getLatitude()));

                Intent intent = new Intent(FlightPathPicker.this, FlightPlanning.class);
                intent.putExtra("path", path);
                startActivity(intent);
            }
        });



        btnEdtPath.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                btnEdtPath.setVisibility(View.GONE);
                btnConfirm.setVisibility(View.VISIBLE);
                btnFlightPlan.setVisibility(View.GONE);
                btnDropMark.setVisibility(View.VISIBLE);
                btnRemoveMark.setVisibility(View.VISIBLE);
                hoveringMarker.setVisibility(View.VISIBLE);

                POINTS.remove(POINTS.size()-1);


                mapboxMap.setStyle(new Style.Builder().fromUri("mapbox://styles/mapbox/cjf4m44iw0uza2spb3q0a7s41")
                        .withImage(ICON_ID, BitmapFactory.decodeResource(
                                FlightPathPicker.this.getResources(), R.drawable.mapbox_marker_icon_default))
                        .withSource(new GeoJsonSource(SOURCE_ID,
                                FeatureCollection.fromFeatures(symbolLayer)))
                        .withLayer(new SymbolLayer(LAYER_ID, SOURCE_ID)
                                .withProperties(
                                        iconImage(ICON_ID),
                                        iconAllowOverlap(true),
                                        iconIgnorePlacement(true)
                                )
                        ), new Style.OnStyleLoaded() {
                    @Override
                    public void onStyleLoaded(@NonNull Style style) {    ///reload map
                    }});
            }
        });
    }

    @Override
    public void onMapReady(@NonNull final MapboxMap mapboxMap) {
        FlightPathPicker.this.mapboxMap = mapboxMap;
        mapboxMap.setStyle(Style.MAPBOX_STREETS, new Style.OnStyleLoaded() {
            @Override
            public void onStyleLoaded(@NonNull final Style style) {
                //view user location
                enableLocationPlugin(style);
                enableLocationComponent(style);

                //symbol manager to display multiple pins
                SymbolManager symbolManager = new SymbolManager(mapView, mapboxMap, style);
                symbolManager.setIconAllowOverlap(true);
                symbolManager.setIconTranslate(new Float[]{-4f,5f});
                symbolManager.setIconRotationAlignment(ICON_ROTATION_ALIGNMENT_VIEWPORT);

                // Toast instructing user to tap on the mapboxMap
                Toast.makeText(FlightPathPicker.this, getString(R.string.move_map_instruction), Toast.LENGTH_SHORT).show();

                //location selection market
                hoveringMarker = new ImageView(FlightPathPicker.this);
                hoveringMarker.setImageResource(R.drawable.red_marker);
                FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT, Gravity.CENTER);
                hoveringMarker.setLayoutParams(params);
                mapView.addView(hoveringMarker);

                //allows user to drop a mark aka flight path boundary, adds to a symbol layer and checks to see if minimum is met
                btnDropMark.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        // Use the map target's coordinates to make a reverse geocoding search
                        final LatLng mapTargetLatLng = mapboxMap.getCameraPosition().target;
                        symbolLayer.add(Feature.fromGeometry((Point.fromLngLat(mapTargetLatLng.getLongitude(), mapTargetLatLng.getLatitude()))));
                        OUTER_POINTS.add(Point.fromLngLat(mapTargetLatLng.getLongitude(), mapTargetLatLng.getLatitude()));
                        path.add(mapTargetLatLng);


                        mapboxMap.setStyle(new Style.Builder().fromUri("mapbox://styles/mapbox/cjf4m44iw0uza2spb3q0a7s41")
                                .withImage(ICON_ID, BitmapFactory.decodeResource(
                                        FlightPathPicker.this.getResources(), R.drawable.mapbox_marker_icon_default))
                                .withSource(new GeoJsonSource(SOURCE_ID,
                                        FeatureCollection.fromFeatures(symbolLayer)))
                                .withLayer(new SymbolLayer(LAYER_ID, SOURCE_ID)
                                        .withProperties(
                                                iconImage(ICON_ID),
                                                iconAllowOverlap(true),
                                                iconIgnorePlacement(true)
                                        )
                                ), new Style.OnStyleLoaded() {
                            @Override
                            public void onStyleLoaded(@NonNull Style style) {    ///reload map
                            }});

                        if (path.size() > 2) {
                            btnConfirm.setVisibility(View.VISIBLE);
                            btnConfirm.setClickable(true);
                        } else {
                            btnConfirm.setVisibility(View.GONE);
                            btnConfirm.setClickable(false);
                        }

                        if (path.size() > 0) {
                            btnRemoveMark.setVisibility(View.VISIBLE);
                            btnRemoveMark.setClickable(true);
                        } else {
                            btnRemoveMark.setVisibility(View.GONE);
                            btnRemoveMark.setClickable(false);
                        }
                    }
                });

                btnRemoveMark.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        symbolLayer.remove(symbolLayer.size()-1);
                        path.remove(path.size()-1);
                        OUTER_POINTS.remove(OUTER_POINTS.size()-1);


                        mapboxMap.setStyle(new Style.Builder().fromUri("mapbox://styles/mapbox/cjf4m44iw0uza2spb3q0a7s41")
                                .withImage(ICON_ID, BitmapFactory.decodeResource(
                                        FlightPathPicker.this.getResources(), R.drawable.mapbox_marker_icon_default))
                                .withSource(new GeoJsonSource(SOURCE_ID,
                                        FeatureCollection.fromFeatures(symbolLayer)))
                                .withLayer(new SymbolLayer(LAYER_ID, SOURCE_ID)
                                        .withProperties(
                                                iconImage(ICON_ID),
                                                iconAllowOverlap(true),
                                                iconIgnorePlacement(true)
                                        )
                                ), new Style.OnStyleLoaded() {
                            @Override
                            public void onStyleLoaded(@NonNull Style style) {    ///reload map
                            }});

                        if (path.size() > 2) {
                            btnConfirm.setVisibility(View.VISIBLE);
                            btnConfirm.setClickable(true);
                        } else {
                            btnConfirm.setVisibility(View.GONE);
                            btnConfirm.setClickable(false);
                        }

                        if (path.size() > 0) {
                            btnRemoveMark.setVisibility(View.VISIBLE);
                            btnRemoveMark.setClickable(true);
                        } else {
                            btnRemoveMark.setVisibility(View.GONE);
                            btnRemoveMark.setClickable(false);
                        }

                    }
                });

                btnConfirm.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        btnEdtPath.setVisibility(View.VISIBLE);
                        btnConfirm.setVisibility(View.GONE);
                        btnFlightPlan.setVisibility(View.VISIBLE);
                        btnDropMark.setVisibility(View.GONE);
                        btnRemoveMark.setVisibility(View.GONE);
                        hoveringMarker.setVisibility(View.GONE);

                        //display flight path outline

// create a fixed fill
                        //List<LatLng> innerLatLngs = new ArrayList<>();
                        //innerLatLngs.add(new LatLng(-10.733102, -3.363937));
                        //innerLatLngs.add(new LatLng(-19.716317, 1.754703));
                        //innerLatLngs.add(new LatLng(-21.085074, -15.747196));

                        //List<List<LatLng>> latLngs = new ArrayList<>();
                        //FillManager fillManager = new FillManager(mapView, mapboxMap, style);
                        //FillOptions fillOptions = new FillOptions()
                        //        .withLatLngs(latLngs)
                        //        .withFillColor(String.valueOf(Color.RED));
                        //fillManager.create(fillOptions);
                        POINTS.add(OUTER_POINTS);

                        mapboxMap.setStyle(Style.MAPBOX_STREETS, new Style.OnStyleLoaded() {
                            @Override
                            public void onStyleLoaded(@NonNull Style style) {
                                style.addSource(new GeoJsonSource("source-id", Polygon.fromLngLats(POINTS)));
                                style.addLayerBelow(new FillLayer("layer-id", "source-id").withProperties(
                                        fillColor(Color.parseColor("#3bb2d0"))), "settlement-label"
                                );
                            }
                            });
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

            // Activate with a built LocationComponentActivationOptions object
            locationComponent.activateLocationComponent(LocationComponentActivationOptions.builder(this, loadedMapStyle).build());

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
    public void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    @SuppressWarnings( {"MissingPermission"})
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
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
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
        if (granted && mapboxMap != null) {
            Style style = mapboxMap.getStyle();
            if (style != null) {
                enableLocationPlugin(style);
            }
        } else {
            Toast.makeText(this, R.string.user_location_permission_not_granted, Toast.LENGTH_LONG).show();
            finish();
        }
    }

    /**
     * This method is used to reverse geocode where the user has dropped the marker.
     *
     * @param point The location to use for the search
     */

    private void reverseGeocode(final Point point) {
        try {
            MapboxGeocoding client = MapboxGeocoding.builder()
                    .accessToken(getString(R.string.mapbox_access_token))
                    .query(Point.fromLngLat(point.longitude(), point.latitude()))
                    .geocodingTypes(GeocodingCriteria.TYPE_ADDRESS)
                    .build();

            client.enqueueCall(new Callback<GeocodingResponse>() {
                @Override
                public void onResponse(Call<GeocodingResponse> call, Response<GeocodingResponse> response) {

                    if (response.body() != null) {
                        List<CarmenFeature> results = response.body().features();
                        if (results.size() > 0) {
                            CarmenFeature feature = results.get(0);

                            // If the geocoder returns a result, we take the first in the list and show a Toast with the place name.
                            mapboxMap.getStyle(new Style.OnStyleLoaded() {
                                @Override
                                public void onStyleLoaded(@NonNull Style style) {
                                    if (style.getLayer(DROPPED_MARKER_LAYER_ID) != null) {
                                        Toast.makeText(FlightPathPicker.this,
                                                String.format(getString(R.string.location_picker_place_name_result),
                                                        feature.placeName()), Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });

                        } else {
                            Toast.makeText(FlightPathPicker.this,
                                    getString(R.string.location_picker_dropped_marker_snippet_no_results), Toast.LENGTH_SHORT).show();
                        }
                    }
                }

                @Override
                public void onFailure(Call<GeocodingResponse> call, Throwable throwable) {
                    Timber.e("Geocoding Failure: %s", throwable.getMessage());
                }
            });
        } catch (ServicesException servicesException) {
            Timber.e("Error geocoding: %s", servicesException.toString());
            servicesException.printStackTrace();
        }
    }

    @SuppressWarnings( {"MissingPermission"})
    private void enableLocationPlugin(@NonNull Style loadedMapStyle) {
        // Check if permissions are enabled and if not request
        if (PermissionsManager.areLocationPermissionsGranted(this)) {

            // Get an instance of the component. Adding in LocationComponentOptions is also an optional
            // parameter
            LocationComponent locationComponent = mapboxMap.getLocationComponent();
            locationComponent.activateLocationComponent(LocationComponentActivationOptions.builder(
                    this, loadedMapStyle).build());
            locationComponent.setLocationComponentEnabled(true);

            // Set the component's camera mode
            locationComponent.setCameraMode(CameraMode.TRACKING);
            locationComponent.setRenderMode(RenderMode.NORMAL);

        } else {
            permissionsManager = new PermissionsManager(this);
            permissionsManager.requestLocationPermissions(this);
        }
    }

    private void initMenu() {
        Toolbar t = (Toolbar) findViewById(R.id.fpp_toolbar);
        t.setTitle(getString(R.string.fp_menu_title));
        t.inflateMenu(R.menu.default_menu);
        t.setOnMenuItemClickListener(item -> {
            switch(item.getItemId()) {
                case R.id.menu_profile:
                    // GO TO PROFILE
                    Intent toProfile = new Intent(FlightPathPicker.this, ProfileActivity.class);
                    startActivity(toProfile);
                    return true;
                default:
                    // Should not happen
                    return true;
            }
        });
    }
}

