package com.example.bus.user;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.example.bus.R;
import com.example.bus.model.Bus;
import com.example.bus.model.Route;
import com.example.bus.model.Stop;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class BusStopsActivity extends AppCompatActivity {
    private static final String TAG = "BusStopsActivity";
    private TextView txtSelectedBus, txtFare, txtTotalTime;
    private ListView stopListView;
    private List<Stop> stopList;
    private String selectedSource, selectedDestination;
    private Bus selectedBus;
    private StopListAdapter stopListAdapter; // Adapter instance

    private FusedLocationProviderClient locationClient;
    private LocationCallback locationCallback;
    private TextToSpeech textToSpeech;

    private static final float STOP_RADIUS = 150.0f;
    private Set<String> announcedStops = new HashSet<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bus_stops);

        txtSelectedBus = findViewById(R.id.txt_selected_bus);
//        txtFare = findViewById(R.id.txt_fare);
        txtTotalTime = findViewById(R.id.txt_total_time);
        stopListView = findViewById(R.id.list_stops);

        textToSpeech = new TextToSpeech(this, status -> {
            if (status == TextToSpeech.SUCCESS) {
                textToSpeech.setLanguage(Locale.US);
            }
        });

        Intent intent = getIntent();
        selectedSource = intent.getStringExtra("source");
        selectedDestination = intent.getStringExtra("destination");
        String busJson = intent.getStringExtra("selectedBus");

        selectedBus = new Gson().fromJson(busJson, Bus.class);

        if (selectedBus == null) {
            Toast.makeText(this, "Error: No bus data received!", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        txtSelectedBus.setText("Bus: " + selectedBus.getBusName());
//        txtFare.setText("Fare: ₹" + selectedBus.getFare());
        txtTotalTime.setText("Time: " + selectedBus.getTotalTime() + " min");

        loadStopsForSelectedRoute();
        updateStopListView();

        locationClient = LocationServices.getFusedLocationProviderClient(this);

        Log.d(TAG, "Starting location updates...");
        startLocationUpdates();
    }

    private void loadStopsForSelectedRoute() {
        List<Route> routes = loadRoutesFromStorage();
        stopList = new ArrayList<>();

        for (Route route : routes) {
            if (route.getRouteName().equals(selectedBus.getAssignedRoute())) {
                List<Stop> stops = route.getStops();

                boolean startAdding = false;
                for (Stop stop : stops) {
                    if (stop.getName().equals(selectedSource)) {
                        startAdding = true;
                    }
                    if (startAdding) {
                        stopList.add(stop);
                    }
                    if (stop.getName().equals(selectedDestination)) {
                        break;
                    }
                }
            }
        }
    }

    private void updateStopListView() {
        stopListAdapter = new StopListAdapter(this, stopList);
        stopListView.setAdapter(stopListAdapter);
    }

    private List<Route> loadRoutesFromStorage() {
        Gson gson = new Gson();
        String json = getSharedPreferences("BusStops", MODE_PRIVATE).getString("routes", "[]");
        Type routeListType = new TypeToken<ArrayList<Route>>() {}.getType();
        return gson.fromJson(json, routeListType);
    }

    private void startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            return;
        }

        LocationRequest locationRequest = LocationRequest.create()
                .setInterval(5000)
                .setFastestInterval(2000)
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) {
                    Log.d(TAG, "No location results");
                    return;
                }
                for (Location location : locationResult.getLocations()) {
                    Log.d(TAG, "User location: " + location.getLatitude() + ", " + location.getLongitude());
                    checkNearbyStop(location);
                }
            }
        };

        locationClient.requestLocationUpdates(locationRequest, locationCallback, null);
        Log.d(TAG, "Location updates started");
    }

    private void checkNearbyStop(Location userLocation) {
        Stop closestStop = null;
        float minDistance = Float.MAX_VALUE;

        for (Stop stop : stopList) {
            float[] distance = new float[1];
            Location.distanceBetween(
                    userLocation.getLatitude(), userLocation.getLongitude(),
                    stop.getLatitude(), stop.getLongitude(),
                    distance
            );

            Log.d(TAG, "Distance to stop " + stop.getName() + ": " + distance[0] + "m");

            if (distance[0] < minDistance && !announcedStops.contains(stop.getName())) {
                minDistance = distance[0];
                closestStop = stop;
            }
        }

        if (closestStop != null && minDistance < STOP_RADIUS) {
            Log.d(TAG, "Announcing stop: " + closestStop.getName());
            announceStop(closestStop.getName());
            announcedStops.add(closestStop.getName());

            // Update the dynamic Fare and Time
            updateFareAndTime(closestStop);
        }
    }

    private void announceStop(String stopName) {
        String announcement = "Approaching " + stopName;
        if (textToSpeech != null && !textToSpeech.isSpeaking()) {
            textToSpeech.speak(announcement, TextToSpeech.QUEUE_FLUSH, null, null);
        }

        // Update the UI to highlight the current stop
        runOnUiThread(() -> stopListAdapter.updateSelectedStop(stopName));
    }


    private void updateFareAndTime(Stop currentStop) {
        // Find the index of the current stop
        int stopIndex = stopList.indexOf(currentStop);
        if (stopIndex == -1) return; // Stop not found, exit

        // Assuming total time is distributed across all stops
        int totalTime = selectedBus.getTotalTime();

        // Calculate per-stop time
        int perStopTime = totalTime / stopList.size();

        // Calculate remaining time based on the stop index
        final int remainingTime = perStopTime * (stopList.size() - stopIndex);

        // If it's the last stop, set time to "1-2 mins"
        final int finalRemainingTime = (stopIndex == stopList.size() - 1) ? 1 : remainingTime;

        // Update the total time dynamically, decreasing as the user progresses through the stops
        runOnUiThread(() -> {
            // Dynamically update time based on stop progression
            txtTotalTime.setText("Time: " + finalRemainingTime + " min");
        });
    }



    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (locationClient != null && locationCallback != null) {
            locationClient.removeLocationUpdates(locationCallback);
        }
        if (textToSpeech != null) {
            textToSpeech.stop();
            textToSpeech.shutdown();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1 && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            startLocationUpdates();
        } else {
            Toast.makeText(this, "Location permission denied", Toast.LENGTH_SHORT).show();
        }
    }
}
