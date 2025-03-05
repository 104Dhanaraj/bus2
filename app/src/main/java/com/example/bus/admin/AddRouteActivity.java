package com.example.bus.admin;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.bus.R;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class AddRouteActivity extends AppCompatActivity {
    private EditText edtRouteName, edtStopName;
    private Button btnAddStop, btnSaveRoute;
    private ListView stopListView;
    private ArrayAdapter<String> stopAdapter;
    private ArrayList<Stop> stopList = new ArrayList<>();
    private static final String PREFS_NAME = "BusStops";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_route);

        edtRouteName = findViewById(R.id.edt_route_name);
        edtStopName = findViewById(R.id.edt_stop_name);
        btnAddStop = findViewById(R.id.btn_add_stop);
        btnSaveRoute = findViewById(R.id.btn_save_route);
        stopListView = findViewById(R.id.list_stops);

        stopAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1);
        stopListView.setAdapter(stopAdapter);

        btnAddStop.setOnClickListener(v -> addStop(edtStopName.getText().toString().trim()));

        btnSaveRoute.setOnClickListener(v -> {
            String routeName = edtRouteName.getText().toString().trim();
            if (routeName.isEmpty() || stopList.isEmpty()) {
                Toast.makeText(this, "Enter route name and add stops!", Toast.LENGTH_SHORT).show();
            } else {
                saveRoute(routeName);
            }
        });
    }

    private void addStop(String stopName) {
        if (!stopName.isEmpty()) {
            fetchStopCoordinates(stopName);
        } else {
            Toast.makeText(this, "Enter a valid stop name", Toast.LENGTH_SHORT).show();
        }
    }

    private void fetchStopCoordinates(String stopName) {
        String apiKey = "AlzaSyjVUUoEcsK-6TW_d7rKKrlsX4lEf3Gh24S";
        String url = "https://maps.gomaps.pro/maps/api/place/findplacefromtext/json?input="
                + stopName + "&inputtype=textquery&fields=name,formatted_address,geometry/location&key=" + apiKey;

        RequestQueue queue = Volley.newRequestQueue(this);
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                response -> {
                    try {
                        JSONArray candidates = response.getJSONArray("candidates");
                        if (candidates.length() > 0) {
                            JSONObject firstResult = candidates.getJSONObject(0);
                            JSONObject location = firstResult.getJSONObject("geometry").getJSONObject("location");

                            double lat = location.getDouble("lat");
                            double lng = location.getDouble("lng");
                            String name = firstResult.getString("name");
                            String address = firstResult.getString("formatted_address");

                            saveStop(name, address, lat, lng);
                        } else {
                            // Use Geocoding API as fallback
                            fetchStopCoordinatesFallback(stopName);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(this, "Error parsing data!", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> Toast.makeText(this, "API Request Failed!", Toast.LENGTH_SHORT).show()
        );

        queue.add(request);
    }

    private void fetchStopCoordinatesFallback(String stopName) {
        String apiKey = "AlzaSyjVUUoEcsK-6TW_d7rKKrlsX4lEf3Gh24";
        String url = "https://maps.gomaps.pro/maps/api/geocode/json?address=" + stopName + "&key=" + apiKey;

        RequestQueue queue = Volley.newRequestQueue(this);
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                response -> {
                    try {
                        JSONArray results = response.getJSONArray("results");
                        if (results.length() > 0) {
                            JSONObject firstResult = results.getJSONObject(0);
                            JSONObject location = firstResult.getJSONObject("geometry").getJSONObject("location");

                            double lat = location.getDouble("lat");
                            double lng = location.getDouble("lng");
                            String address = firstResult.getString("formatted_address");

                            saveStop(stopName, address, lat, lng);
                        } else {
                            Toast.makeText(this, "Stop not found!", Toast.LENGTH_SHORT).show();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(this, "Error parsing fallback data!", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> Toast.makeText(this, "API Fallback Request Failed!", Toast.LENGTH_SHORT).show()
        );

        queue.add(request);
    }

    private void saveStop(String stopName, String address, double lat, double lng) {
        Stop stop = new Stop(stopName, address, lat, lng);
        stopList.add(stop);
        stopAdapter.add(stopName + " - " + address);
        stopAdapter.notifyDataSetChanged();
    }

    private void saveRoute(String routeName) {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        Gson gson = new Gson();
        String json = prefs.getString("routes", "[]");
        Type listType = new TypeToken<ArrayList<Route>>() {}.getType();
        ArrayList<Route> storedRoutes = gson.fromJson(json, listType);

        storedRoutes.add(new Route(routeName, new ArrayList<>(stopList)));

        String updatedJson = gson.toJson(storedRoutes);
        editor.putString("routes", updatedJson);
        editor.apply();

        Toast.makeText(this, "Route saved!", Toast.LENGTH_SHORT).show();
        finish();
    }

    public static List<String> getRoutes(SharedPreferences prefs) {
        Gson gson = new Gson();
        String json = prefs.getString("routes", "[]");
        Type listType = new TypeToken<ArrayList<Route>>() {}.getType();
        ArrayList<Route> storedRoutes = gson.fromJson(json, listType);

        List<String> routeNames = new ArrayList<>();
        for (Route route : storedRoutes) {
            routeNames.add(route.getRouteName());
        }
        return routeNames;
    }

    public static List<Stop> getStopsForRoute(SharedPreferences prefs, String routeName) {
        Gson gson = new Gson();
        String json = prefs.getString("routes", "[]");
        Type listType = new TypeToken<ArrayList<Route>>() {}.getType();
        ArrayList<Route> storedRoutes = gson.fromJson(json, listType);

        for (Route route : storedRoutes) {
            if (route.getRouteName().equals(routeName)) {
                return route.getStops();
            }
        }
        return new ArrayList<>();
    }

    static class Stop {
        private final String name;
        private final String address;
        private final double latitude;
        private final double longitude;

        Stop(String name, String address, double latitude, double longitude) {
            this.name = name;
            this.address = address;
            this.latitude = latitude;
            this.longitude = longitude;
        }

        public String getName() {
            return name;
        }

        public String getAddress() {
            return address;
        }

        public double getLatitude() {
            return latitude;
        }

        public double getLongitude() {
            return longitude;
        }
    }

    static class Route {
        private final String routeName;
        private final List<Stop> stops;

        Route(String routeName, List<Stop> stops) {
            this.routeName = routeName;
            this.stops = stops;
        }

        public String getRouteName() {
            return routeName;
        }

        public List<Stop> getStops() {
            return stops;
        }
    }
}