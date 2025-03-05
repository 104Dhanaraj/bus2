package com.example.bus.user;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.bus.R;
import com.example.bus.model.Bus;
import com.example.bus.model.Route;
import com.example.bus.model.Stop;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class BusStopsActivity extends AppCompatActivity {
    private TextView txtSelectedBus, txtFare, txtTotalTime;
    private ListView stopListView;
    private List<String> stopNames;
    private String selectedSource, selectedDestination;
    private Bus selectedBus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bus_stops);

        txtSelectedBus = findViewById(R.id.txt_selected_bus);
        txtFare = findViewById(R.id.txt_fare);
        txtTotalTime = findViewById(R.id.txt_total_time);
        stopListView = findViewById(R.id.list_stops);

        // Get data from Intent
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

        // Debugging logs
        Log.d("DEBUG", "Selected Bus: " + selectedBus.getBusName());
        Log.d("DEBUG", "Source: " + selectedSource + ", Destination: " + selectedDestination);

        // Load stops and update UI
        loadStopsForSelectedRoute();
    }

    private void loadStopsForSelectedRoute() {
        List<Route> routes = loadRoutesFromStorage();
        stopNames = new ArrayList<>();

        if (routes == null || routes.isEmpty()) {
            Toast.makeText(this, "No route data available!", Toast.LENGTH_SHORT).show();
            return;
        }

        for (Route route : routes) {
            if (route.getRouteName().equals(selectedBus.getAssignedRoute())) {
                List<Stop> stops = route.getStops();

                int sourceIndex = -1, destinationIndex = -1;
                for (int i = 0; i < stops.size(); i++) {
                    if (stops.get(i).getName().equals(selectedSource)) {
                        sourceIndex = i;
                    }
                    if (stops.get(i).getName().equals(selectedDestination)) {
                        destinationIndex = i;
                    }
                }

                if (sourceIndex != -1 && destinationIndex != -1 && sourceIndex < destinationIndex) {
                    for (int i = sourceIndex; i <= destinationIndex; i++) {
                        stopNames.add(stops.get(i).getName());
                    }

                    // Calculate fare and time dynamically
                    int totalStops = stops.size() - 1;
                    int travelStops = destinationIndex - sourceIndex;
                    double adjustedFare = (selectedBus.getFare() / totalStops) * travelStops;
                    int adjustedTime = (selectedBus.getTotalTime() / totalStops) * travelStops;

                    // Update UI with dynamic values
                    txtSelectedBus.setText("Bus: " + selectedBus.getBusName());
                    txtFare.setText("Fare: ₹" + String.format("%.2f", adjustedFare));
                    txtTotalTime.setText("Time: " + adjustedTime + " min");

                    Log.d("DEBUG", "Adjusted Fare: ₹" + adjustedFare + ", Adjusted Time: " + adjustedTime + " min");
                } else {
                    Toast.makeText(this, "Invalid route selection!", Toast.LENGTH_SHORT).show();
                    return;
                }
            }
        }

        // Set up ListView adapter
        StopListAdapter adapter = new StopListAdapter(this, stopNames);
        stopListView.setAdapter(adapter);
    }

    private List<Route> loadRoutesFromStorage() {
        Gson gson = new Gson();
        String json = getSharedPreferences("BusStops", MODE_PRIVATE).getString("routes", "[]");
        Type routeListType = new TypeToken<ArrayList<Route>>() {}.getType();
        return gson.fromJson(json, routeListType);
    }
}
