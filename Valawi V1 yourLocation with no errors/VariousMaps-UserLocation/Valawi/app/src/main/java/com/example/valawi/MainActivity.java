package com.example.valawi;

import android.annotation.SuppressLint;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.LinearSnapHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.mapbox.android.core.permissions.PermissionsListener;
import com.mapbox.android.core.permissions.PermissionsManager;
import com.mapbox.api.directions.v5.DirectionsCriteria;
import com.mapbox.api.directions.v5.MapboxDirections;
import com.mapbox.api.directions.v5.models.DirectionsResponse;
import com.mapbox.api.directions.v5.models.DirectionsRoute;
import com.mapbox.geojson.Feature;
import com.mapbox.geojson.FeatureCollection;
import com.mapbox.geojson.LineString;
import com.mapbox.geojson.Point;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.location.LocationComponent;
import com.mapbox.mapboxsdk.location.LocationComponentActivationOptions;
import com.mapbox.mapboxsdk.location.modes.CameraMode;
import com.mapbox.mapboxsdk.location.modes.RenderMode;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.maps.Style;
import com.mapbox.mapboxsdk.style.layers.LineLayer;
import com.mapbox.mapboxsdk.style.layers.SymbolLayer;
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource;
import com.mapbox.mapboxsdk.utils.BitmapUtils;

import org.jetbrains.annotations.NotNull;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Random;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import timber.log.Timber;

import static android.view.View.VISIBLE;
import static com.mapbox.core.constants.Constants.PRECISION_6;
import static com.mapbox.mapboxsdk.style.layers.Property.LINE_JOIN_ROUND;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconAllowOverlap;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconIgnorePlacement;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconImage;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconOffset;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconSize;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.lineColor;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.lineJoin;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.lineWidth;
import static com.mapbox.mapboxsdk.utils.BitmapUtils.*;

/**
 * Use a variety of professionally designed styles with the Mapbox Android SDK.
 */
public class MainActivity extends AppCompatActivity implements
        OnMapReadyCallback, PermissionsListener {

    private static final String TAG = "MainActivity";
    private static final String ICON_PROPERTY = "ICON_PROPERTY";
    private static final String SOURCE_ID = "SOURCE_ID";
    private static final String ICON_ID = "ICON_ID";
    private static final String SYMBOL_ICON_ID = "SYMBOL_ICON_ID";
    private static final String PERSON_ICON_ID = "PERSON_ICON_ID";
    private static final String MARKER_SOURCE_ID = "MARKER_SOURCE_ID";
    private static final String PERSON_SOURCE_ID = "PERSON_SOURCE_ID";
    private static final String DASHED_DIRECTIONS_LINE_LAYER_SOURCE_ID = "DASHED_DIRECTIONS_LINE_LAYER_SOURCE_ID";
    private static final String LAYER_ID = "LAYER_ID";
    private static final String PERSON_LAYER_ID = "PERSON_LAYER_ID";
    private static final String DASHED_DIRECTIONS_LINE_LAYER_ID = "DASHED_DIRECTIONS_LINE_LAYER_ID";
    private static final Point directionsOriginPoint = Point.fromLngLat(100.48730850219725,
            13.737217333153827);
    private static final  LatLng[] possibleDestinations = new LatLng[]{
            new LatLng(13.773399508046145, 100.51116943359375),
            new LatLng(13.743387039520751, 100.45074462890625),
            new LatLng(13.732715012486663, 100.5523681640625),
            new LatLng(13.665336643848484, 100.45486450195312),
            new LatLng(13.7153719325982, 100.49263000488281),
            new LatLng(13.742053062720384, 100.51288604736328),
            new LatLng(13.77773432408578, 100.4806137084961),
            new LatLng(13.784736549340208, 100.55580139160156),
            new LatLng(13.71670606117596, 100.45520782470703)
    };
    private final List<DirectionsRoute> directionsRouteList = new ArrayList<>();
    private MapboxMap mapboxMap;
    private MapView mapView;
    private FeatureCollection dashedLineDirectionsFeatureCollection;
    private FeatureCollection featureCollection;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Mapbox access token is configured here. This needs to be called either in your application
        // object or in the same activity which contains the mapview.
        Mapbox.getInstance(this, getString(R.string.access_token));

        // This contains the MapView in XML and needs to be called after the access token is configured.
        setContentView(R.layout.activity_main);

        mapView = findViewById(R.id.mapView);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(@NonNull MapboxMap mapboxMap) {
                MainActivity.this.mapboxMap = mapboxMap;
                mapboxMap.setStyle(Style.LIGHT, MainActivity.this::enableLocationComponent);

            }
        });
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    @Override
    public void onMapReady(@NonNull MapboxMap mapboxMap) {
        List<Feature> symbolLayerIconFeatureList = new ArrayList<>();
        symbolLayerIconFeatureList.add(Feature.fromGeometry(
                Point.fromLngLat(-57.225365, -33.213144)));
        symbolLayerIconFeatureList.add(Feature.fromGeometry(
                Point.fromLngLat(-54.14164, -33.981818)));
        symbolLayerIconFeatureList.add(Feature.fromGeometry(
                Point.fromLngLat(-56.990533, -30.583266)));
        MainActivity.this.mapboxMap = mapboxMap;
        mapboxMap.setStyle(new Style.Builder().fromUri("mapbox://styles/mapbox/cj44mfrt20f082snokim4ungi")



// Set up the image, source, and layer for the person icon,
// which is where all of the routes will start from

                .withImage(PERSON_ICON_ID, Objects.requireNonNull(getBitmapFromDrawable(
                        getResources().getDrawable(R.drawable.ic_person))))
                .withSource(new GeoJsonSource(PERSON_SOURCE_ID,
                        Feature.fromGeometry(directionsOriginPoint)))
                .withLayer(new SymbolLayer(PERSON_LAYER_ID, PERSON_SOURCE_ID).withProperties(
                        iconImage(PERSON_ICON_ID),
                        iconSize(2f),
                        iconAllowOverlap(true),
                        iconIgnorePlacement(true)
                ))

// Set up the image, source, and layer for the potential destination markers
                .withImage(ICON_ID, BitmapFactory.decodeResource(
                        this.getResources(), R.drawable.red_marker))
                .withSource(new GeoJsonSource(MARKER_SOURCE_ID, initDestinationFeatureCollection()))
                .withLayer(new SymbolLayer(LAYER_ID, MARKER_SOURCE_ID).withProperties(
                        iconImage(SYMBOL_ICON_ID),
                        iconAllowOverlap(true),
                        iconIgnorePlacement(true),
                        iconOffset(new Float[]{0f, -4f})

                ))


// Set up the source and layer for the direction route LineLayer
                .withSource(new GeoJsonSource(DASHED_DIRECTIONS_LINE_LAYER_SOURCE_ID))
                .withLayerBelow(
                        new LineLayer(DASHED_DIRECTIONS_LINE_LAYER_ID, DASHED_DIRECTIONS_LINE_LAYER_SOURCE_ID)
                                .withProperties(
                                        lineWidth(7f),
                                        lineJoin(LINE_JOIN_ROUND),
                                        lineColor(Color.parseColor("#2096F3"))
                                ), PERSON_LAYER_ID), new Style.OnStyleLoaded() {
            @Override
            public void onStyleLoaded(@NonNull Style style) {
                getRoutesToAllPoints();
                style.addImage(PERSON_ICON_ID, BitmapFactory.decodeResource(
                        MainActivity.this.getResources(), R.drawable.red_marker));
                initDestinationFeatureCollection();
                initRecyclerView();
                initMarkerIcons(style);
                Toast.makeText(MainActivity.this,
                        R.string.toast_instruction, Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Loop through the possible destination list of LatLng locations and get
     * the route for each destination.
     */
    private void getRoutesToAllPoints() {
        for (LatLng singleLatLng : possibleDestinations) {
            getRoute(Point.fromLngLat(singleLatLng.getLongitude(), singleLatLng.getLatitude()));
        }
    }

    /**
     * Make a call to the Mapbox Directions API to get the route from the person location icon
     * to the marker's location and then add the route to the route list.
     *
     * @param destination the marker associated with the recyclerview card that was tapped on.
     */
    @SuppressWarnings({"MissingPermission"})
    private void getRoute(Point destination) {
        MapboxDirections client = MapboxDirections.builder()
                .origin(directionsOriginPoint)
                .destination(destination)
                .overview(DirectionsCriteria.OVERVIEW_FULL)
                .profile(DirectionsCriteria.PROFILE_DRIVING)
                .accessToken(getString(R.string.access_token))
                .build();
        client.enqueueCall(new Callback<DirectionsResponse>() {
            @Override
            public void onResponse(@NotNull Call<DirectionsResponse> call, @NotNull Response<DirectionsResponse> response) {
                if (Objects.requireNonNull(response).body() == null) {
                    Timber.d("No routes found, make sure you set the right user and access token.");
                    return;
                } else if (Objects.requireNonNull(response.body()).routes().size() < 1) {
                    Timber.d("No routes found");
                    return;
                }
// Add the route to the list.
                directionsRouteList.add(response.body().routes().get(0));
            }

            @SuppressLint("BinaryOperationInTimber")
            @Override
            public void onFailure(@NotNull Call<DirectionsResponse> call, @NotNull Throwable throwable) {
                Timber.d("Error: " + throwable.getMessage());
                if (!Objects.requireNonNull(throwable.getMessage()).equals("Coordinate is invalid: 0,0")) {
                    Toast.makeText(MainActivity.this,
                            "Error: " + throwable.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    /**
     * Update the GeoJSON data for the direction route LineLayer.
     *
     * @param route The route to be drawn in the map's LineLayer that was set up above.
     */
    private void drawNavigationPolylineRoute(final DirectionsRoute route) {
        if (mapboxMap != null) {
            mapboxMap.getStyle(new Style.OnStyleLoaded() {
                @Override
                public void onStyleLoaded(@NonNull Style style) {
                    List<Feature> directionsRouteFeatureList = new ArrayList<>();
                    LineString lineString = LineString.fromPolyline(route.geometry(), PRECISION_6);
                    List<Point> lineStringCoordinates = lineString.coordinates();
                    for (int i = 0; i < lineStringCoordinates.size(); i++) {
                        directionsRouteFeatureList.add(Feature.fromGeometry(
                                LineString.fromLngLats(lineStringCoordinates)));
                    }
                    dashedLineDirectionsFeatureCollection =
                            FeatureCollection.fromFeatures(directionsRouteFeatureList);
                    GeoJsonSource source = style.getSourceAs(DASHED_DIRECTIONS_LINE_LAYER_SOURCE_ID);
                    if (source != null) {
                        source.setGeoJson(dashedLineDirectionsFeatureCollection);
                    }
                }
            });
        }
    }

    /**
     * Create a FeatureCollection to display the possible destination markers.
     *
     * @return a {@link FeatureCollection}, which represents the possible destinations.
     */
    private FeatureCollection initDestinationFeatureCollection() {
        List<Feature> featureList = new ArrayList<>();
        for (LatLng latLng : possibleDestinations) {
            featureList.add(Feature.fromGeometry(
                    Point.fromLngLat(latLng.getLongitude(), latLng.getLatitude())));
        }
        return FeatureCollection.fromFeatures(featureList);
    }

    /**
     * Set up the RecyclerView.
     */
    private void initRecyclerView() {
        RecyclerView recyclerView = findViewById(R.id.rv_on_top_of_map);
        recyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext(),
                LinearLayoutManager.HORIZONTAL, true));
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(new LocationRecyclerViewAdapter(this,
                createRecyclerViewLocations(), mapboxMap));
        new LinearSnapHelper().attachToRecyclerView(recyclerView);
    }

    private void initMarkerIcons(@NonNull Style loadedMapStyle) {
        loadedMapStyle.addImage(SYMBOL_ICON_ID, BitmapFactory.decodeResource(
                this.getResources(), R.drawable.red_marker));
        loadedMapStyle.addSource(new GeoJsonSource(SOURCE_ID, featureCollection));
        loadedMapStyle.addLayer(new SymbolLayer(LAYER_ID, SOURCE_ID).withProperties(
                iconImage(SYMBOL_ICON_ID),
                iconAllowOverlap(true),
                iconOffset(new Float[] {0f, -4f})
        ));
    }

    /**
     * Create data fro the RecyclerView.
     *
     * @return a list of {@link SingleRecyclerViewLocation} objects for the RecyclerView.
     */
    @SuppressLint("StringFormatInvalid")
    private List<SingleRecyclerViewLocation> createRecyclerViewLocations() {
        ArrayList<SingleRecyclerViewLocation> locationList = new ArrayList<>();
        for (int x = 0; x < possibleDestinations.length; x++) {
            SingleRecyclerViewLocation singleLocation = new SingleRecyclerViewLocation();
            singleLocation.setName(String.format(getString(R.string.rv_directions_route_card_name), x));
            singleLocation.setAvailableTables(String.format(getString(
                    R.string.rv_directions_route_available_table_info),
                    new Random().nextInt(possibleDestinations.length)));
            locationList.add(singleLocation);
        }
        return locationList;
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
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_map_style, menu);
        return true;
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.menu_streets:
                mapboxMap.setStyle(Style.MAPBOX_STREETS);
                return true;
            case R.id.menu_dark:
                mapboxMap.setStyle(Style.DARK);
                return true;
            case R.id.menu_light:
                mapboxMap.setStyle(Style.LIGHT);
                return true;
            case R.id.menu_outdoors:
                mapboxMap.setStyle(Style.OUTDOORS);
                return true;
            case R.id.menu_satellite:
                mapboxMap.setStyle(Style.SATELLITE);
                return true;
            case R.id.menu_satellite_streets:
                mapboxMap.setStyle(Style.SATELLITE_STREETS);
                return true;
            case android.R.id.home:
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * POJO model class for a single location in the RecyclerView.
     */
    static class SingleRecyclerViewLocation {

        private String name;
        private String availableTables;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getAvailableTables() {
            return availableTables;
        }

        public void setAvailableTables(String availableTables) {
            this.availableTables = availableTables;
        }

    }

    static class LocationRecyclerViewAdapter extends
            RecyclerView.Adapter<LocationRecyclerViewAdapter.MyViewHolder> {

        private final List<SingleRecyclerViewLocation> locationList;

        private final MapboxMap map;
        private final WeakReference<MainActivity> weakReference;


        public LocationRecyclerViewAdapter(MainActivity activity,
                                           List<SingleRecyclerViewLocation> locationList,
                                           MapboxMap mapBoxMap) {
            this.locationList = locationList;
            this.map = mapBoxMap;
            this.weakReference = new WeakReference<>(activity);
        }

        @Override
        public @NotNull MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.rv_directions_card, parent, false);
            return new MyViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(MyViewHolder holder, int position) {
            SingleRecyclerViewLocation singleRecyclerViewLocation = locationList.get(position);
            holder.name.setText(singleRecyclerViewLocation.getName());
            holder.numOfAvailableTables.setText(singleRecyclerViewLocation.getAvailableTables());
            holder.setClickListener(new ItemClickListener() {
                private View view;
                private int position;

                @Override
                public void onClick(View view, int position) {
                    this.view = view;
                    this.position = position;
                    weakReference.get()
                            .drawNavigationPolylineRoute(weakReference.get().directionsRouteList.get(position));
                }
            });
        }

        @Override
        public int getItemCount() {
            return locationList.size();
        }

        static class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
            TextView name;
            TextView numOfAvailableTables;
            CardView singleCard;
            ItemClickListener clickListener;

            MyViewHolder(View view) {
                super(view);
                name = view.findViewById(R.id.location_title_tv);
                numOfAvailableTables = view.findViewById(R.id.location_num_of_beds_tv);
                singleCard = view.findViewById(R.id.single_location_cardview);
                singleCard.setOnClickListener(this);
            }

            public void setClickListener(ItemClickListener itemClickListener) {
                this.clickListener = itemClickListener;
            }

            @Override
            public void onClick(View view) {
                clickListener.onClick(view, getLayoutPosition());
            }
        }
    }

    public interface ItemClickListener {
        void onClick(View view, int position);
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
            PermissionsManager permissionsManager = new PermissionsManager(this);
            permissionsManager.requestLocationPermissions(this);
        }
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
}
