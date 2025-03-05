package com.example.bus.admin;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.bus.R;
import com.example.bus.model.Bus;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class AddBusActivity extends AppCompatActivity {
    private Spinner busDropdown, routeDropdown;
    private EditText edtFare, edtTime;
    private Button btnSaveBus;
    private static List<Bus> busList = new ArrayList<>();

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
                Toast.makeText(this, "Bus Assigned Successfully", Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    public static List<String> getBusesForRoute(SharedPreferences prefs, String routeName, String sourceStop, String destinationStop) {
        List<String> busesForRoute = new ArrayList<>();
        List<AddRouteActivity.Stop> stopObjects = AddRouteActivity.getStopsForRoute(prefs, routeName);
        List<String> stops = new ArrayList<>();

        // Extract stop names
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
        return busesForRoute;
    }
}
