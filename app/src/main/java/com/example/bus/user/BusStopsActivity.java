package com.example.bus.user;

import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.example.bus.R;
import com.example.bus.model.Bus;
import com.example.bus.model.Route;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class BusStopsActivity extends AppCompatActivity {
    private TextView txtSelectedBus, txtFare, txtTotalTime;
    private ListView stopListView;
    private List<String> stopNames;
    private List<double[]> stopCoordinates;
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
        String busJson = intent.getStringExtra("buses");

        Type busListType = new TypeToken<ArrayList<Bus>>() {}.getType();
        List<Bus> busList = new Gson().fromJson(busJson, busListType);

        if (busList == null || busList.isEmpty()) {
            Toast.makeText(this, "No buses found!", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        selectedBus = busList.get(0); // Default to the first bus in the list

        txtSelectedBus.setText("Bus: " + selectedBus.getBusName());
        txtFare.setText("Fare: $" + selectedBus.getFare());
        txtTotalTime.setText("Time: " + selectedBus.getTotalTime() + " min");

        loadStopsForSelectedRoute();
    }

    private void loadStopsForSelectedRoute() {
        List<Route> routes = loadRoutesFromStorage();
        stopNames = new ArrayList<>();
        stopCoordinates = new ArrayList<>();

        for (Route route : routes) {
            if (route.getRouteName().equals(selectedBus.getAssignedRoute())) {
                List<String> stops = route.getStops();
                Map<String, double[]> stopLocations = route.getStopLocations();

                boolean startAdding = false;
                for (String stop : stops) {
                    if (stop.equals(selectedSource)) {
                        startAdding = true;
                    }
                    if (startAdding) {
                        stopNames.add(stop);
                        stopCoordinates.add(stopLocations.get(stop));
                    }
                    if (stop.equals(selectedDestination)) {
                        break;
                    }
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
