package com.ex.dguide;

import static com.mapbox.android.core.location.LocationEngineProvider.getBestLocationEngine;
import static com.mapbox.maps.ViewAnnotationAnchor.BOTTOM_LEFT;
import static com.mapbox.maps.ViewAnnotationAnchor.BOTTOM_RIGHT;
import static com.mapbox.maps.ViewAnnotationAnchor.TOP_RIGHT;
import static com.mapbox.maps.plugin.animation.CameraAnimationsUtils.getCamera;
import static com.mapbox.maps.plugin.gestures.GesturesUtils.getGestures;
import static com.mapbox.maps.plugin.locationcomponent.LocationComponentUtils.getLocationComponent;
import static com.mapbox.navigation.base.extensions.RouteOptionsExtensions.applyDefaultNavigationOptions;
import static java.util.Arrays.asList;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.ex.dguide.databinding.ActivityMainBinding;
import com.ex.dguide.search.CustomDivider;
import com.ex.dguide.search.PlaceAdapter;
import com.ex.dguide.search.SearchManager;
import com.ex.dguide.sqlitehelper.DbHelper;
import com.ex.dguide.sqlitehelper.SearchPlace;
import com.ex.dguide.utils.AddMarkertoNewPlace;
import com.ex.dguide.utils.BitmapUtils;
import com.ex.dguide.utils.MapMarkers;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.mapbox.android.core.location.LocationEngine;
import com.mapbox.android.core.location.LocationEngineCallback;
import com.mapbox.android.core.location.LocationEngineResult;
import com.mapbox.android.gestures.MoveGestureDetector;
import com.mapbox.api.directions.v5.DirectionsCriteria;
import com.mapbox.api.directions.v5.models.Bearing;
import com.mapbox.api.directions.v5.models.DirectionsRoute;
import com.mapbox.api.directions.v5.models.RouteOptions;
import com.mapbox.api.directions.v5.models.VoiceInstructions;
import com.mapbox.bindgen.Expected;
import com.mapbox.core.constants.Constants;
import com.mapbox.geojson.LineString;
import com.mapbox.geojson.Point;
import com.mapbox.maps.CameraOptions;
import com.mapbox.maps.EdgeInsets;
import com.mapbox.maps.MapView;
import com.mapbox.maps.MapboxMap;
import com.mapbox.maps.ScreenCoordinate;
import com.mapbox.maps.Style;
import com.mapbox.maps.ViewAnnotationAnchor;
import com.mapbox.maps.ViewAnnotationOptions;
import com.mapbox.maps.extension.style.layers.properties.generated.IconPitchAlignment;
import com.mapbox.maps.plugin.LocationPuck2D;
import com.mapbox.maps.plugin.animation.MapAnimationOptions;
import com.mapbox.maps.plugin.gestures.OnMapClickListener;
import com.mapbox.maps.plugin.gestures.OnMoveListener;
import com.mapbox.maps.plugin.locationcomponent.LocationComponentConstants;
import com.mapbox.maps.plugin.locationcomponent.LocationComponentPlugin;
import com.mapbox.maps.plugin.locationcomponent.OnIndicatorPositionChangedListener;
import com.mapbox.maps.viewannotation.OnViewAnnotationUpdatedListener;
import com.mapbox.maps.viewannotation.ViewAnnotationManager;
import com.mapbox.navigation.base.formatter.DistanceFormatterOptions;
import com.mapbox.navigation.base.options.NavigationOptions;
import com.mapbox.navigation.base.route.NavigationRoute;
import com.mapbox.navigation.base.route.NavigationRouterCallback;
import com.mapbox.navigation.base.route.RouterFailure;
import com.mapbox.navigation.base.route.RouterOrigin;
import com.mapbox.navigation.base.trip.model.RouteLegProgress;
import com.mapbox.navigation.base.trip.model.RouteProgress;
import com.mapbox.navigation.core.MapboxNavigation;
import com.mapbox.navigation.core.arrival.ArrivalObserver;
import com.mapbox.navigation.core.directions.session.RoutesObserver;
import com.mapbox.navigation.core.directions.session.RoutesUpdatedResult;
import com.mapbox.navigation.core.formatter.MapboxDistanceFormatter;
import com.mapbox.navigation.core.lifecycle.MapboxNavigationApp;
import com.mapbox.navigation.core.trip.session.LocationMatcherResult;
import com.mapbox.navigation.core.trip.session.LocationObserver;
import com.mapbox.navigation.core.trip.session.RouteProgressObserver;
import com.mapbox.navigation.core.trip.session.VoiceInstructionsObserver;
import com.mapbox.navigation.ui.base.util.MapboxNavigationConsumer;
import com.mapbox.navigation.ui.maneuver.api.MapboxManeuverApi;
import com.mapbox.navigation.ui.maneuver.view.MapboxManeuverView;
import com.mapbox.navigation.ui.maps.location.NavigationLocationProvider;
import com.mapbox.navigation.ui.maps.route.arrow.api.MapboxRouteArrowApi;
import com.mapbox.navigation.ui.maps.route.arrow.api.MapboxRouteArrowView;
import com.mapbox.navigation.ui.maps.route.arrow.model.RouteArrowOptions;
import com.mapbox.navigation.ui.maps.route.line.api.MapboxRouteLineApi;
import com.mapbox.navigation.ui.maps.route.line.api.MapboxRouteLineView;
import com.mapbox.navigation.ui.maps.route.line.model.MapboxRouteLineOptions;
import com.mapbox.navigation.ui.maps.route.line.model.RouteLineError;
import com.mapbox.navigation.ui.maps.route.line.model.RouteLineResources;
import com.mapbox.navigation.ui.maps.route.line.model.RouteLineUpdateValue;
import com.mapbox.navigation.ui.tripprogress.api.MapboxTripProgressApi;
import com.mapbox.navigation.ui.tripprogress.model.DistanceRemainingFormatter;
import com.mapbox.navigation.ui.tripprogress.model.EstimatedTimeToArrivalFormatter;
import com.mapbox.navigation.ui.tripprogress.model.TimeRemainingFormatter;
import com.mapbox.navigation.ui.tripprogress.model.TripProgressUpdateFormatter;
import com.mapbox.navigation.ui.tripprogress.view.MapboxTripProgressView;
import com.mapbox.navigation.ui.voice.api.MapboxSpeechApi;
import com.mapbox.navigation.ui.voice.api.MapboxVoiceInstructionsPlayer;
import com.mapbox.navigation.ui.voice.model.SpeechAnnouncement;
import com.mapbox.navigation.ui.voice.model.SpeechError;
import com.mapbox.navigation.ui.voice.model.SpeechValue;
import com.mapbox.navigation.ui.voice.model.SpeechVolume;
import com.mapbox.navigation.ui.voice.view.MapboxSoundButton;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import kotlin.Unit;

public class MainActivity extends AppCompatActivity {
    private final NavigationLocationProvider navigationLocationProvider = new NavigationLocationProvider();
    private final LocationPuck2D locationPuck2D = new LocationPuck2D();
    private final ActivityResultLauncher<String> activityResultLauncher = registerForActivityResult(new ActivityResultContracts.RequestPermission(), result -> {
        if (result) {
            enableLocation();
        } else {
            // Permission denied, handle accordingly
            Toast.makeText(MainActivity.this, "Permission denied! exitng the app", Toast.LENGTH_SHORT).show();
            finish();
        }
    });

    private final MapboxRouteArrowApi routeArrowApi = new MapboxRouteArrowApi();
    public MapboxNavigation mapboxNavigation;
    MapView mapView;
    private final BroadcastReceiver newPlaceAddedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            AddMarkertoNewPlace newPlace = new AddMarkertoNewPlace(mapView);
            DbHelper db = new DbHelper(MainActivity.this, "placesData.db", null, 1);
            newPlace.addMarkersFromDatabase(db);
        }
    };
    MaterialButton setRoute;
    FloatingActionButton focusLocationBtn, refresh, addPlacebtn;
    TextInputLayout searchLayout;
    View progress;
    boolean focusLocation = true;
    public final LocationObserver locationObserver = new LocationObserver() {
        @Override
        public void onNewRawLocation(@NonNull Location location) {

        }

        @Override
        public void onNewLocationMatcherResult(@NonNull LocationMatcherResult locationMatcherResult) {
            Location location = locationMatcherResult.getEnhancedLocation();
            navigationLocationProvider.changePosition(location, locationMatcherResult.getKeyPoints(), null, null);
            if (focusLocation) {
                updateCamera(Point.fromLngLat(location.getLongitude(), location.getLatitude()), (double) location.getBearing());
            }
        }
    };
    private final OnMoveListener onMoveListener = new OnMoveListener() {
        @Override
        public void onMoveBegin(@NonNull MoveGestureDetector moveGestureDetector) {
            focusLocation = false;
            getGestures(mapView).removeOnMoveListener(this);
            focusLocationBtn.show();
        }

        @Override
        public boolean onMove(@NonNull MoveGestureDetector moveGestureDetector) {
            return false;
        }

        @Override
        public void onMoveEnd(@NonNull MoveGestureDetector moveGestureDetector) {
        }
    };
    boolean showNoResults = false;
    private MapboxRouteLineView routeLineView;
    private MapboxRouteLineApi routeLineApi;
    public final RoutesObserver routesObserver = new RoutesObserver() {
        @Override
        public void onRoutesChanged(@NonNull RoutesUpdatedResult routesUpdatedResult) {
            routeLineApi.setNavigationRoutes(routesUpdatedResult.getNavigationRoutes(), routeLineErrorRouteSetValueExpected -> {
                Style style = mapView.getMapboxMap().getStyle();
                if (style != null) {
                    routeLineView.renderRouteDrawData(style, routeLineErrorRouteSetValueExpected);
                }
            });
        }
    };
    private final OnIndicatorPositionChangedListener onPositionChangedListener = new OnIndicatorPositionChangedListener() {
        @Override
        public void onIndicatorPositionChanged(@NonNull Point point) {
            Expected<RouteLineError, RouteLineUpdateValue> result = routeLineApi.updateTraveledRouteLine(point);
            Style style = mapView.getMapboxMap().getStyle();
            if (style != null) {
                routeLineView.renderRouteLineUpdate(style, result);
            }
        }
    };
    private LocationComponentPlugin locationComponent;
    private MapboxSpeechApi speechApi;
    private final MapboxNavigationConsumer<SpeechAnnouncement> voiceInstructionsPlayerCallback = new MapboxNavigationConsumer<SpeechAnnouncement>() {
        @Override
        public void accept(SpeechAnnouncement speechAnnouncement) {
            speechApi.clean(speechAnnouncement);
        }
    };
    private MapboxVoiceInstructionsPlayer mapboxVoiceInstructionsPlayer;
    private final MapboxNavigationConsumer<Expected<SpeechError, SpeechValue>> speechCallback = new MapboxNavigationConsumer<Expected<SpeechError, SpeechValue>>() {
        @Override
        public void accept(Expected<SpeechError, SpeechValue> speechErrorSpeechValueExpected) {
            speechErrorSpeechValueExpected.fold(input -> {
                mapboxVoiceInstructionsPlayer.play(input.getFallback(), voiceInstructionsPlayerCallback);
                return Unit.INSTANCE;
            }, input -> {
                mapboxVoiceInstructionsPlayer.play(input.getAnnouncement(), voiceInstructionsPlayerCallback);
                return Unit.INSTANCE;
            });
        }
    };
    VoiceInstructionsObserver voiceInstructionsObserver = new VoiceInstructionsObserver() {
        @Override
        public void onNewVoiceInstructions(@NonNull VoiceInstructions voiceInstructions) {
            speechApi.generate(voiceInstructions, speechCallback);
        }
    };
    private boolean isVoiceInstructionsMuted = false;
    private TextInputEditText searchET;
    private MapboxTripProgressView tripProgress;
    private MapboxTripProgressApi tripProgressApi;
    private MapboxManeuverView mapboxManeuverView;
    private final ArrivalObserver arrivalObserver = new ArrivalObserver() {


        @Override
        public void onNextRouteLegStart(@NonNull RouteLegProgress routeLegProgress) {

        }

        @Override
        public void onWaypointArrival(@NonNull RouteProgress routeProgress) {


        }

        @Override
        public void onFinalDestinationArrival(@NonNull RouteProgress routeProgress) {
            mapboxVoiceInstructionsPlayer.play(new SpeechAnnouncement.Builder(getString(R.string.arrival)).build(), voiceInstructionsPlayerCallback);
            mapboxNavigation.stopTripSession();
            mapboxNavigation.setNavigationRoutes(Collections.emptyList());
            mapboxManeuverView.setVisibility(View.GONE);
            tripProgress.setVisibility(View.GONE);

        }
    };
    private MapboxManeuverApi maneuverApi;
    private MapboxRouteArrowView routeArrowView;
    private final RouteProgressObserver routeProgressObserver = new RouteProgressObserver() {
        @Override
        public void onRouteProgressChanged(@NonNull RouteProgress routeProgress) {
            Style style = mapView.getMapboxMap().getStyle();
            if (style != null) {
                routeArrowView.renderManeuverUpdate(style, routeArrowApi.addUpcomingManeuverArrow(routeProgress));
                tripProgress.render(tripProgressApi.getTripProgress(routeProgress));
            }
            maneuverApi.getManeuvers(routeProgress).fold(input -> new Object(), input -> {
                mapboxManeuverView.setVisibility(View.VISIBLE);
                tripProgress.setVisibility(View.VISIBLE);
                tripProgress.render(tripProgressApi.getTripProgress(routeProgress));
                mapboxManeuverView.renderManeuvers(maneuverApi.getManeuvers(routeProgress));
                return new Object();
            });
        }
    };
    private MapMarkers mapMarkers;
    private OnMapClickListener mapClickListener;
    private ViewAnnotationManager viewAnnotationManager;
    private View etaView, eta_altView;
    private PlaceAdapter adapter;
    private SearchManager searchManager;
    private RecyclerView recyclerView;
    private CustomDivider customDivider;

    private void updateCamera(@NonNull Point point, Double bearing) {
        MapAnimationOptions animationOptions = new MapAnimationOptions.Builder().duration(2000L).build();
        CameraOptions cameraOptions = new CameraOptions.Builder().center(point).zoom(13.0).bearing(bearing)
                .padding(new EdgeInsets(0.0, 0.0, 0.0, 0.0)).build();

        getCamera(mapView).easeTo(cameraOptions, animationOptions);
    }

    private void enableLocation() {
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (locationManager != null && !locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            // If GPS is not enabled, prompt the user to enable it
            new AlertDialog.Builder(MainActivity.this)
                    .setTitle("Enable Location")
                    .setMessage("Location services are required for this app. Do you want to enable them?")
                    .setPositiveButton("Yes", (dialog, which) -> {
                        Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        startActivity(intent);
                    })
                    .setNegativeButton("No", (dialog, which) -> dialog.dismiss())
                    .show();
        }
    }



    @SuppressLint("MissingPermission")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityMainBinding binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        LocalBroadcastManager.getInstance(this).registerReceiver(
                newPlaceAddedReceiver,
                new IntentFilter("NEW_PLACE_ADDED")
        );

        progress = findViewById(R.id.loadingLayout);
        progress.setVisibility(View.VISIBLE);

        try (DbHelper dbHelper = new DbHelper(MainActivity.this, "placesData.db", null, 1)) {
            List<SearchPlace> placesList = new ArrayList<>();
            adapter = new PlaceAdapter(placesList);
            recyclerView = findViewById(R.id.recyclerView);
            customDivider = new CustomDivider(this, R.drawable.borderline);
            searchManager = new SearchManager(dbHelper, placesList, adapter, recyclerView);
            dbHelper.insertFirebaseDataToSQLite();
        }
        initsearch();

        new Handler().postDelayed(() -> {
            mapView = findViewById(R.id.mapView);
            focusLocationBtn = findViewById(R.id.focusLocation);
            setRoute = findViewById(R.id.setRoute);
            setRoute.setEnabled(false);
            refresh = findViewById(R.id.refreshMap);
            addPlacebtn = findViewById(R.id.addPlace);
            searchLayout = findViewById(R.id.searchLayout);
            mapboxManeuverView = findViewById(R.id.maneuverView);
            tripProgress = findViewById(R.id.tripProgressView);
            mapboxManeuverView = findViewById(R.id.maneuverView);
            tripProgress = findViewById(R.id.tripProgressView);
            initMapboxNavigation();
            initSpeech();


            MapboxRouteLineOptions options = new MapboxRouteLineOptions.Builder(MainActivity.this)
                    .withRouteLineResources(new RouteLineResources.Builder()
                            .build())
                    .withRouteLineBelowLayerId(LocationComponentConstants.LOCATION_INDICATOR_LAYER)
                    .withRouteLineBelowLayerId("road-label")
                    .withVanishingRouteLineEnabled(true)
                    .iconPitchAlignment(IconPitchAlignment.AUTO)
                    .displaySoftGradientForTraffic(true)
                    .displayRestrictedRoadSections(true)
                    .withTolerance(0.5)
                    .build();
            routeLineView = new MapboxRouteLineView(options);
            routeLineApi = new MapboxRouteLineApi(options);


            MapboxSoundButton soundButton = findViewById(R.id.soundButton);
            soundButton.setVisibility(View.GONE);
            soundButton.unmute();
            soundButton.setOnClickListener(view -> {
                isVoiceInstructionsMuted = !isVoiceInstructionsMuted;
                if (isVoiceInstructionsMuted) {
                    soundButton.muteAndExtend(1500L);
                    mapboxVoiceInstructionsPlayer.volume(new SpeechVolume(0f));
                } else {
                    soundButton.unmuteAndExtend(1500L);
                    mapboxVoiceInstructionsPlayer.volume(new SpeechVolume(1f));
                }
            });

            try (DbHelper db = new DbHelper(MainActivity.this, "placesData.db", null, 1)) {
                DatabaseReference mainTableReference = FirebaseDatabase.getInstance().getReference("places");
                DatabaseReference searchTableReference = FirebaseDatabase.getInstance().getReference("searchPlaces");
                db.uploadPlacesToFirebase(mainTableReference, searchTableReference);
                db.syncWithFirebase(mainTableReference);
                if (!db.isSearchTableExists()) {
                    db.initializeSearchTable();
                }

                mapMarkers = new MapMarkers(mapView);
                mapClickListener = point -> {
                    mapMarkers.clearMarkers();
                    mapMarkers.addMarker(point);
                    MainActivity.this.updateCamera(point, null);
                    setRoute.setEnabled(true);
                    setRoute.setOnClickListener(view -> MainActivity.this.fetchRoute(point));

                   //double latitude = point.latitude();
                     //double longitude = point.longitude();

                     // addPlacebtn.setVisibility(View.VISIBLE);
                     //addPlacebtn.setOnClickListener(view -> {
                     // Intent intent = new Intent(MainActivity.this, AddPlaceActivity.class);
                     //  intent.putExtra("latitude", latitude);
                     //  intent.putExtra("longitude", longitude);
                      // MainActivity.this.startActivity(intent);

                     // });
                    return true;
                };

                AddMarkertoNewPlace newPlace = new AddMarkertoNewPlace(mapView);
                LocationEngine locationEngine = getBestLocationEngine(this);
                mapView.getMapboxMap().loadStyleUri(getMapStyleUri(), style -> {
                    locationComponent = getLocationComponent(mapView);
                    newPlace.addMarkersFromDatabase(db);
                    getLocationComponent(mapView).addOnIndicatorPositionChangedListener(onPositionChangedListener);
                    locationEngine.getLastLocation(new LocationEngineCallback<LocationEngineResult>() {
                        @Override
                        public void onSuccess(LocationEngineResult result) {
                            Location location = result.getLastLocation();
                            if (location != null) {
                                double latitude = location.getLatitude();
                                double longitude = location.getLongitude();

                                addPlacebtn.setVisibility(View.VISIBLE);
                                addPlacebtn.setOnClickListener(view -> {
                                    Intent intent = new Intent(MainActivity.this, AddPlaceActivity.class);
                                    intent.putExtra("latitude", latitude);
                                    intent.putExtra("longitude", longitude);
                                    startActivity(intent);

                                });

                                Point point = Point.fromLngLat(location.getLongitude(), location.getLatitude());
                                moveCamera(point);
                                getGestures(mapView).addOnMoveListener(onMoveListener);

                            }
                            initLocationComponent();

                            if (location == null) {

                                mapView.getMapboxMap().setCamera(
                                        new CameraOptions.Builder().zoom(13.0).center(Point.fromLngLat(125.35722, 6.74972))
                                                .padding(new EdgeInsets(300.0, 0.0, 0.0, 0.0)).build());
                            }


                            addOnMapClickListener(mapView.getMapboxMap(), mapClickListener);

                            focusLocationBtn.setOnClickListener(view -> {
                                focusLocation = true;
                                getGestures(mapView).addOnMoveListener(onMoveListener);
                                focusLocationBtn.hide();

                                Toast.makeText(MainActivity.this, "GPS not enabled", Toast.LENGTH_SHORT).show();
                                if (location == null) {
                                    enableLocation();
                                }else {
                                    focusLocation = true;
                                }
                            });

                            refresh.setOnClickListener(view -> {
                                setRoute.setText(R.string.find_path);
                                setRoute.setVisibility(View.VISIBLE);
                                setRoute.setEnabled(false);
                                soundButton.setVisibility(View.GONE);
                                mapMarkers.clearMarkers();
                                viewAnnotationManager.removeAllViewAnnotations();
                                mapboxNavigation.stopTripSession();
                                if (location != null) {
                                    moveCamera(Point.fromLngLat(location.getLongitude(), location.getLatitude()));
                                }
                                mapboxNavigation.setNavigationRoutes(Collections.emptyList());
                                searchLayout.setVisibility(View.VISIBLE);
                                tripProgress.setVisibility(View.GONE);
                                mapboxManeuverView.setVisibility(View.GONE);
                                addOnMapClickListener(mapView.getMapboxMap(), mapClickListener);
                                refresh.hide();
                                addPlacebtn.show();
                            });
                            progress.setVisibility(View.GONE);
                            searchLayout.setVisibility(View.VISIBLE);
                        }

                        @Override
                        public void onFailure(@NonNull Exception exception) {

                        }
                    });

                });
            } catch (Exception e) {
                e.printStackTrace();
            }

        }, 5000);

        if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            activityResultLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION);
            activityResultLauncher.launch(Manifest.permission.ACCESS_COARSE_LOCATION);
        }

    }

    private void initsearch() {
        searchET = findViewById(R.id.searchET);
        searchManager.initializeSearch(searchET, false);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
        recyclerView.addItemDecoration(customDivider);

        adapter.setOnItemClickListener((position, latitude, longitude) -> {
            mapMarkers.clearMarkers();
            mapMarkers.addMarker(Point.fromLngLat(longitude, latitude));
            recyclerView.setVisibility(View.GONE);
            moveCamera(Point.fromLngLat(longitude, latitude));
            setRoute.setEnabled(true);
            setRoute.setOnClickListener(view -> fetchRoute(Point.fromLngLat(longitude, latitude)));
           searchET.setText("");
           searchET.clearFocus(); // remove focus from search

        });

    }

    public void onBackPressed() {
        if (searchET.getVisibility() == View.VISIBLE) {
            searchET.setText("");
            searchET.clearFocus();
            mapMarkers.clearMarkers();
            setRoute.setEnabled(false);
        } else {
            super.onBackPressed();
        }
    }


    private void moveCamera(@NonNull Point point) {
        MapAnimationOptions animationOptions = new MapAnimationOptions.Builder().duration(2500L).build();
        CameraOptions cameraOptions = new CameraOptions.Builder().center(point).zoom(13.0).build();
        getCamera(mapView).easeTo(cameraOptions, animationOptions);
        getGestures(mapView).addOnMoveListener(onMoveListener);

    }


    private String getMapStyleUri() {
        int darkMode = getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
        switch (darkMode) {
            case Configuration.UI_MODE_NIGHT_YES:
                return Style.TRAFFIC_NIGHT;
            case Configuration.UI_MODE_NIGHT_NO:
            case Configuration.UI_MODE_NIGHT_UNDEFINED:
                return Style.MAPBOX_STREETS;
            default:
                throw new RuntimeException("Unknown mode: " + darkMode);

        }
    }


    private void addOnMapClickListener(MapboxMap mapboxMap, OnMapClickListener listener) {
        getGestures(mapView).addOnMapClickListener(listener);
    }


    private void removeOnMapClickListener(MapboxMap mapboxMap, OnMapClickListener listener) {
        getGestures(mapView).removeOnMapClickListener(listener);
    }

    private void initLocationComponent() {
        locationComponent.setEnabled(true);
        locationPuck2D.setTopImage(AppCompatResources.getDrawable(MainActivity.this, R.drawable.user_icon));
        locationPuck2D.setShadowImage(AppCompatResources.getDrawable(MainActivity.this, R.drawable.shadow));
        locationComponent.setLocationPuck(locationPuck2D);
        getGestures(mapView).addOnMoveListener(onMoveListener);
        getLocationComponent(mapView).addOnIndicatorPositionChangedListener(onPositionChangedListener);
        locationComponent.updateSettings(locationComponentSettings -> {
            locationComponentSettings.setPulsingMaxRadius(15);
            locationComponentSettings.setEnabled(true);
            locationComponentSettings.setPulsingEnabled(true);
            return null;


        });
    }

    private void initSpeech() {
        speechApi = new MapboxSpeechApi(MainActivity.this, getString(R.string.mapbox_access_token), Locale.US.toLanguageTag());
        mapboxVoiceInstructionsPlayer = new MapboxVoiceInstructionsPlayer(MainActivity.this, Locale.US.toLanguageTag());
    }


    private void initManueverView() {
        maneuverApi = new MapboxManeuverApi(new MapboxDistanceFormatter(new DistanceFormatterOptions.Builder(MainActivity.this)
                .locale(Locale.US)
                .build()));
        routeArrowView = new MapboxRouteArrowView(new RouteArrowOptions.Builder(MainActivity.this).build());
    }

    private void initMapboxNavigation() {
        NavigationOptions navigationOptions = new NavigationOptions.Builder(this).accessToken(getString(R.string.mapbox_access_token)).build();
        MapboxNavigationApp.setup(navigationOptions);
        mapboxNavigation = new MapboxNavigation(navigationOptions);
        mapboxNavigation.registerRoutesObserver(routesObserver);
        mapboxNavigation.registerRouteProgressObserver(routeProgressObserver);
        mapboxNavigation.registerLocationObserver(locationObserver);
        mapboxNavigation.registerVoiceInstructionsObserver(voiceInstructionsObserver);
        mapboxNavigation.registerArrivalObserver(arrivalObserver);
        initManueverView();
        initTripProgress();
    }

    private void initTripProgress() {
        DistanceFormatterOptions Doptions = new DistanceFormatterOptions.Builder(this)
                .build();
        TripProgressUpdateFormatter tripProgressFormatter = new TripProgressUpdateFormatter.Builder(this)
                .distanceRemainingFormatter(new DistanceRemainingFormatter(Doptions))
                .timeRemainingFormatter(new TimeRemainingFormatter(this, Locale.US))
                .estimatedTimeToArrivalFormatter(new EstimatedTimeToArrivalFormatter(this, 12))
                .build();

        tripProgressApi = new MapboxTripProgressApi(tripProgressFormatter);

    }


    @SuppressLint("MissingPermission")
    private void fetchRoute(Point destination) {
        LocationEngine locationEngine = getBestLocationEngine(MainActivity.this);
        locationEngine.getLastLocation(new LocationEngineCallback<LocationEngineResult>() {
            @Override
            public void onSuccess(LocationEngineResult result) {
                Location location = result.getLastLocation();
                setRoute.setEnabled(false);
                setRoute.setText(R.string.fetching_route);
                refresh.show();
                addPlacebtn.hide();
                RouteOptions.Builder builder = RouteOptions.builder();
                Point origin = Point.fromLngLat(Objects.requireNonNull(location).getLongitude(), location.getLatitude());
                builder.coordinatesList(asList(origin, destination));
                builder.profile(DirectionsCriteria.PROFILE_DRIVING);
                builder.voiceUnits(DirectionsCriteria.METRIC);
                builder.alternatives(true);
                builder.bannerInstructions(true);
                builder.annotations(DirectionsCriteria.ANNOTATION_DURATION);
                builder.annotations(DirectionsCriteria.ANNOTATION_DISTANCE);
                builder.overview(DirectionsCriteria.OVERVIEW_FULL);
                builder.bearingsList(asList(Bearing.builder().angle(location.getBearing()).degrees(45).build(), null));
                applyDefaultNavigationOptions(builder);

                mapboxNavigation.requestRoutes(builder.build(), new NavigationRouterCallback() {
                    @Override
                    public void onRoutesReady(@NonNull List<NavigationRoute> list, @NonNull RouterOrigin routerOrigin) {

                        if (list.size() >= 1) {
                            mapboxNavigation.setNavigationRoutes(list);

                            DirectionsRoute route = list.get(0).getDirectionsRoute();

                            DirectionsRoute alternativeRouteInfo = null;
                            if (list.size() >= 2) {
                                alternativeRouteInfo = list.get(1).getDirectionsRoute();


                            } else {
                                Toast.makeText(MainActivity.this, "No Alternative routes found", Toast.LENGTH_SHORT).show();
                            }
                            viewAnnotationManager = mapView.getViewAnnotationManager();
                            addannotation(route, alternativeRouteInfo);

                        }
                        removeOnMapClickListener(mapView.getMapboxMap(), mapClickListener);
                        setRoute.setEnabled(true);
                        setRoute.setText(R.string.navigate);
                        setRoute.setOnClickListener(view -> {
                            focusLocationBtn.performClick();
                            searchLayout.setVisibility(View.GONE);
                            MapboxSoundButton soundButton = findViewById(R.id.soundButton);
                            soundButton.setVisibility(View.VISIBLE);
                            tripProgress.setVisibility(View.VISIBLE);
                            MapboxManeuverView maneuverView = findViewById(R.id.maneuverView);
                            maneuverView.setVisibility(View.VISIBLE);
                            refresh.show();
                            mapboxNavigation.startTripSession();
                            getLocationComponent(mapView).addOnIndicatorPositionChangedListener(onPositionChangedListener);
                            removeOnMapClickListener(mapView.getMapboxMap(), mapClickListener);
                            setRoute.setVisibility(View.GONE);

                        });

                    }


                    @Override
                    public void onFailure(@NonNull List<RouterFailure> list, @NonNull RouteOptions routeOptions) {
                        setRoute.setEnabled(true);
                        setRoute.setText(R.string.find_path);
                        Toast.makeText(MainActivity.this, "Route request failed", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onCanceled(@NonNull RouteOptions routeOptions, @NonNull RouterOrigin routerOrigin) {

                    }
                });
            }

            @Override
            public void onFailure(@NonNull Exception exception) {

            }
        });

    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapboxNavigation.onDestroy();
        mapboxNavigation.unregisterRoutesObserver(routesObserver);
        mapboxNavigation.unregisterRouteProgressObserver(routeProgressObserver);
        mapboxNavigation.unregisterLocationObserver(locationObserver);
        mapboxNavigation.unregisterVoiceInstructionsObserver(voiceInstructionsObserver);
        mapboxNavigation.unregisterArrivalObserver(arrivalObserver);
    }


    @SuppressLint("SetTextI18n")
    private void addannotation(DirectionsRoute route, DirectionsRoute alternativeRouteInfo) {
        if (route != null && route.geometry() != null) {
            List<Point> routeCoordinates = LineString.fromPolyline(route.geometry(), Constants.PRECISION_6).coordinates();
            if (routeCoordinates.size() >= 1) {
                int midIndex = routeCoordinates.size() / 2;
                Point midPoint = routeCoordinates.get(midIndex);
                double routeDuration = route.duration();
                double routeDistance = route.distance();
                String routeDurationInMinutes = String.format(Locale.getDefault(), "%.2f", routeDuration / 60.0);
                String routeDistanceInKm = String.format(Locale.getDefault(), "%.2f", routeDistance / 1000.0);
                ViewAnnotationOptions options;

                etaView = viewAnnotationManager.addViewAnnotation(R.layout.item_dva_eta, options = new ViewAnnotationOptions.Builder()
                        .anchor(TOP_RIGHT)
                        .anchor(ViewAnnotationAnchor.TOP_LEFT)
                        .anchor(BOTTOM_RIGHT)
                        .anchor(BOTTOM_LEFT)
                        .geometry(midPoint)
                        .build());
                TextView etaText = etaView.findViewById(R.id.eta);
                etaText.setText(routeDurationInMinutes + " min,\n" + routeDistanceInKm + " km");


                if (alternativeRouteInfo != null && alternativeRouteInfo.geometry() != null) {
                    List<Point> alternativeRouteCoordinates = LineString.fromPolyline(alternativeRouteInfo.geometry(), Constants.PRECISION_6).coordinates();
                    if (alternativeRouteCoordinates.size() >= 1) {
                        int altMidIndex = alternativeRouteCoordinates.size() / 2;
                        Point alternativeMidPoint = alternativeRouteCoordinates.get(altMidIndex);
                        double altRouteDuration = alternativeRouteInfo.duration();
                        double altRouteDistance = alternativeRouteInfo.distance();
                        String alternativerouteDurationInMinutes = String.format(Locale.getDefault(), "%.2f", altRouteDuration / 60.0);
                        String altRouteDistanceInKm = String.format(Locale.getDefault(), "%.2f", altRouteDistance / 1000.0);

                        eta_altView = viewAnnotationManager.addViewAnnotation(R.layout.item_dva_alt_eta, options = new ViewAnnotationOptions.Builder()
                                .anchor(TOP_RIGHT)
                                .anchor(ViewAnnotationAnchor.TOP_LEFT)
                                .anchor(BOTTOM_RIGHT)
                                .anchor(BOTTOM_LEFT)
                                .geometry(alternativeMidPoint)
                                .build());

                        TextView etaAltText = eta_altView.findViewById(R.id.alt_eta);
                        etaAltText.setText(alternativerouteDurationInMinutes + " min,\n" + altRouteDistanceInKm + " km");
                    }

                }


                ViewAnnotationOptions finalOptions = options;
                viewAnnotationManager.addOnViewAnnotationUpdatedListener(new OnViewAnnotationUpdatedListener() {
                    @Override
                    public void onViewAnnotationVisibilityUpdated(@NonNull View view, boolean b) {


                    }

                    @Override
                    public void onViewAnnotationPositionUpdated(@NonNull View view, @NonNull ScreenCoordinate screenCoordinate, int i, int i1) {

                        if (view == etaView) {
                            assert finalOptions.getAnchor() != null;
                            view.setBackground(getBackground(finalOptions.getAnchor(), ContextCompat.getColor(MainActivity.this, R.color.blue)));
                            view.setTranslationX((float) screenCoordinate.getX());
                            view.setTranslationY((float) screenCoordinate.getY());
                        }  if (view == eta_altView) {
                            assert finalOptions.getAnchor() != null;
                            view.setBackground(getBackground(finalOptions.getAnchor(), ContextCompat.getColor(MainActivity.this, R.color.blue)));
                            view.setTranslationX((float) screenCoordinate.getX());
                            view.setTranslationY((float) screenCoordinate.getY());
                        }

                    }

                    private Drawable getBackground(ViewAnnotationAnchor anchorConfig, int color) {
                        boolean flipX = false;
                        boolean flipY = false;

                        switch (anchorConfig) {
                            case BOTTOM_RIGHT:
                                flipX = true;
                                flipY = true;
                                break;

                            case TOP_RIGHT:
                                flipX = true;
                                break;

                            case BOTTOM_LEFT:
                                flipY = true;
                                break;

                            default:
                                // no-op
                                break;
                        }

                        Resources resources = getResources();
                        Drawable originalDrawable = ContextCompat.getDrawable(MainActivity.this, R.drawable.bg_dva_eta);

                        // Log information for debugging
                        Log.d("DrawableTest", "Drawable resource: " + originalDrawable);
                        Log.d("DrawableTest", "FlipX: " + flipX + ", FlipY: " + flipY);

                        Bitmap bitmap = BitmapUtils.drawableToBitmap(
                                originalDrawable, flipX, flipY, null
                        );


                        // Log information for debugging
                        Log.d("DrawableTest", "Bitmap: " + bitmap);

                        return new BitmapDrawable(resources, bitmap);
                    }
                });
            }
        }
    }

}











