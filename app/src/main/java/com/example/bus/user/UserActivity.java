package com.example.bus.user;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;
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

public class UserActivity extends AppCompatActivity {
    private Spinner sourceSpinner, destinationSpinner;
    private Button btnFindBuses;
    private List<Route> routes;
    private List<Bus> buses;
    private String selectedSource, selectedDestination;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user);

        sourceSpinner = findViewById(R.id.spinner_source);
        destinationSpinner = findViewById(R.id.spinner_destination);
        btnFindBuses = findViewById(R.id.btn_find_buses);

        // Load stored routes and buses
        loadRoutesAndBuses();

        btnFindBuses.setOnClickListener(v -> {
            if (selectedSource == null || selectedDestination == null) {
                Toast.makeText(this, "Select Source and Destination", Toast.LENGTH_SHORT).show();
            } else {
                openBusStopsScreen();
            }
        });
    }

    private void loadRoutesAndBuses() {
        Gson gson = new Gson();
        String routesJson = getSharedPreferences("BusStops", MODE_PRIVATE).getString("routes", "[]");
        String busesJson = getSharedPreferences("BusStops", MODE_PRIVATE).getString("buses", "[]");

        Type routeListType = new TypeToken<ArrayList<Route>>() {}.getType();
        Type busListType = new TypeToken<ArrayList<Bus>>() {}.getType();

        routes = gson.fromJson(routesJson, routeListType);
        buses = gson.fromJson(busesJson, busListType);

        if (routes == null) routes = new ArrayList<>();
        if (buses == null) buses = new ArrayList<>();

        setupSpinners();
    }

    private void setupSpinners() {
        List<String> allStops = new ArrayList<>();
        for (Route route : routes) {
            for (String stop : route.getStops()) {
                if (!allStops.contains(stop)) {
                    allStops.add(stop);
                }
            }
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, allStops);
        sourceSpinner.setAdapter(adapter);
        destinationSpinner.setAdapter(adapter);

        sourceSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedSource = allStops.get(position);
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        destinationSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedDestination = allStops.get(position);
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private void openBusStopsScreen() {
        List<Bus> matchingBuses = new ArrayList<>();

        for (Bus bus : buses) {
            for (Route route : routes) {
                if (bus.getAssignedRoute().equals(route.getRouteName())) {
                    List<String> stops = route.getStops();
                    if (stops.contains(selectedSource) && stops.contains(selectedDestination)) {
                        matchingBuses.add(bus);
                    }
                }
            }
        }

        if (matchingBuses.isEmpty()) {
            Toast.makeText(this, "No buses found for selected route", Toast.LENGTH_SHORT).show();
            return;
        }

        Intent intent = new Intent(UserActivity.this, BusStopsActivity.class);
        intent.putExtra("source", selectedSource);
        intent.putExtra("destination", selectedDestination);
        intent.putExtra("buses", new Gson().toJson(matchingBuses)); // Pass filtered buses
        startActivity(intent);
    }
}
