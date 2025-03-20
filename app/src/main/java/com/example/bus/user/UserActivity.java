package com.example.bus.user;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Spinner;
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

public class UserActivity extends AppCompatActivity {
    private Spinner sourceSpinner, destinationSpinner;
    private Button btnFindBuses;
    private ListView listViewBuses;
    private TextView txtAvailableBuses;
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
        listViewBuses = findViewById(R.id.list_buses);
        txtAvailableBuses = findViewById(R.id.txt_available_buses);

        listViewBuses.setVisibility(View.GONE); // Hide ListView Initially
        txtAvailableBuses.setVisibility(View.GONE);

        // Load stored routes and buses
        loadRoutesAndBuses();

        btnFindBuses.setOnClickListener(v -> {
            if (selectedSource == null || selectedDestination == null) {
                Toast.makeText(this, "Select Source and Destination", Toast.LENGTH_SHORT).show();
            } else {
                findAvailableBuses();
            }
        });
    }

    private void loadRoutesAndBuses() {
        Gson gson = new Gson();
        SharedPreferences prefs = getSharedPreferences("BusStops", MODE_PRIVATE);

        String routesJson = prefs.getString("routes", "[]");
        String busesJson = prefs.getString("buses", "[]");

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
            for (Stop stop : route.getStops()) {
                if (!allStops.contains(stop.getName())) {
                    allStops.add(stop.getName());
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

    private void findAvailableBuses() {
        List<Bus> matchingBuses = new ArrayList<>();

        for (Bus bus : buses) {
            for (Route route : routes) {
                if (bus.getAssignedRoute().equals(route.getRouteName())) {
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
                        int totalStops = stops.size() - 1;
                        int travelStops = destinationIndex - sourceIndex;

                        double adjustedFare = (bus.getFare() / totalStops) * travelStops;
                        int adjustedTime = (bus.getTotalTime() / totalStops) * travelStops;

                        bus.setAdjustedTime(adjustedTime);
                        bus.setAdjustedFare(adjustedFare);

                        matchingBuses.add(bus);
                    }
                }
            }
        }

        if (matchingBuses.isEmpty()) {
            Toast.makeText(this, "No buses found for selected route", Toast.LENGTH_SHORT).show();
            return;
        }

        // Sort buses by TotalTime (ascending order)
        matchingBuses.sort((b1, b2) -> Integer.compare(b1.getAdjustedTime(), b2.getAdjustedTime()));

        // Prepare list for display
        List<String> busInfoList = new ArrayList<>();
        for (Bus bus : matchingBuses) {
            String busInfo = bus.getBusName() + " | Fare: ₹" + String.format("%.2f", bus.getAdjustedFare()) +
                    " | Time: " + bus.getAdjustedTime() + " mins";
            busInfoList.add(busInfo);
        }

        showAvailableBuses(busInfoList, matchingBuses);
    }

    private void showAvailableBuses(List<String> busInfoList, List<Bus> matchingBuses) {
        txtAvailableBuses.setVisibility(View.VISIBLE);
        listViewBuses.setVisibility(View.VISIBLE);
//
////        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, busInfoList);
////        listViewBuses.setAdapter(adapter);
            BusListAdapter adapter = new BusListAdapter(this, matchingBuses);
            listViewBuses.setAdapter(adapter);


        listViewBuses.setOnItemClickListener((parent, view, position, id) -> {
            Bus selectedBus = matchingBuses.get(position);
            Intent intent = new Intent(UserActivity.this, BusStopsActivity.class);
            intent.putExtra("source", selectedSource);
            intent.putExtra("destination", selectedDestination);
            intent.putExtra("selectedBus", new Gson().toJson(selectedBus));
            startActivity(intent);
        });
    }
}
