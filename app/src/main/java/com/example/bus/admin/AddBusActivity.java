package com.example.bus.admin;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.bus.R;
import com.example.bus.model.Bus;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class AddBusActivity extends AppCompatActivity {
    private Spinner busDropdown, routeDropdown;
    private EditText edtFare, edtTime;
    private Button btnSaveBus;
    private List<Bus> busList = new ArrayList<>();

    private static final List<String> buses = Arrays.asList("Bus A", "Bus B", "Bus C", "Bus D", "Bus E");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_bus);

        busDropdown = findViewById(R.id.spinner_bus);
        routeDropdown = findViewById(R.id.spinner_route);
        edtFare = findViewById(R.id.edt_fare);
        edtTime = findViewById(R.id.edt_time);
        btnSaveBus = findViewById(R.id.btn_save_bus);

        SharedPreferences prefs = getSharedPreferences("BusStops", MODE_PRIVATE);
        loadBusList(prefs);

        busDropdown.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, buses));
        routeDropdown.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, AddRouteActivity.getRoutes(prefs)));

        btnSaveBus.setOnClickListener(v -> {
            String selectedBus = busDropdown.getSelectedItem().toString();
            String selectedRoute = routeDropdown.getSelectedItem().toString();
            String fareStr = edtFare.getText().toString().trim();
            String timeStr = edtTime.getText().toString().trim();

            if (fareStr.isEmpty() || timeStr.isEmpty()) {
                Toast.makeText(this, "Please enter all details", Toast.LENGTH_SHORT).show();
            } else {
                double fare = Double.parseDouble(fareStr);
                int totalTime = Integer.parseInt(timeStr);

                busList.add(new Bus(selectedBus, selectedRoute, fare, totalTime)); // Store the Bus
                saveBusList(prefs); // Persist the updated list

                Toast.makeText(this, "Bus Assigned Successfully", Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    private void loadBusList(SharedPreferences prefs) {
        Gson gson = new Gson();
        String busesJson = prefs.getString("buses", "[]");

        Type busListType = new TypeToken<ArrayList<Bus>>() {}.getType();
        busList = gson.fromJson(busesJson, busListType);

        if (busList == null) busList = new ArrayList<>();

        Log.d("DEBUG", "Loaded Buses: " + busesJson);
    }

    private void saveBusList(SharedPreferences prefs) {
        SharedPreferences.Editor editor = prefs.edit();
        String busesJson = new Gson().toJson(busList);
        editor.putString("buses", busesJson);
        editor.apply();
    }

    public static List<String> getBusesForRoute(SharedPreferences prefs, String routeName, String sourceStop, String destinationStop) {
        List<String> busesForRoute = new ArrayList<>();
        Gson gson = new Gson();

        String busesJson = prefs.getString("buses", "[]");
        Type busListType = new TypeToken<ArrayList<Bus>>() {}.getType();
        List<Bus> busList = gson.fromJson(busesJson, busListType);

        if (busList == null) busList = new ArrayList<>();

        List<AddRouteActivity.Stop> stopObjects = AddRouteActivity.getStopsForRoute(prefs, routeName);
        List<String> stops = new ArrayList<>();

        if (stopObjects == null || stopObjects.isEmpty()) {
            Log.d("DEBUG", "No stops found for route: " + routeName);
            return busesForRoute;
        }

        for (AddRouteActivity.Stop stop : stopObjects) {
            stops.add(stop.getName());
        }

        for (Bus bus : busList) {
            if (bus.getAssignedRoute().equals(routeName)) {
                int sourceIndex = stops.indexOf(sourceStop);
                int destinationIndex = stops.indexOf(destinationStop);

                if (sourceIndex != -1 && destinationIndex != -1 && sourceIndex < destinationIndex) {
                    int totalStops = stops.size() - 1;
                    int travelStops = destinationIndex - sourceIndex;

                    double adjustedFare = (bus.getFare() / totalStops) * travelStops;
                    int adjustedTime = (bus.getTotalTime() / totalStops) * travelStops;

                    busesForRoute.add(bus.getBusName() + " | Fare: ₹" + String.format("%.2f", adjustedFare) +
                            " | Time: " + adjustedTime + " mins");
                }
            }
        }

        Log.d("DEBUG", "Buses found for route " + routeName + ": " + busesForRoute);
        return busesForRoute;
    }
}
