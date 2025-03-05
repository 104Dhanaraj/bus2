package com.example.bus.model;

import java.util.List;
import java.util.Map;

public class Route {
    private String routeName;
    private List<String> stops;
    private Map<String, double[]> stopLocations; // Stores GPS coordinates for each stop

    public Route(String routeName, List<String> stops, Map<String, double[]> stopLocations) {
        this.routeName = routeName;
        this.stops = stops;
        this.stopLocations = stopLocations; // ✅ Fixed initialization issue
    }

    public String getRouteName() {
        return routeName;
    }

    public List<String> getStops() {
        return stops;
    }

    public Map<String, double[]> getStopLocations() {
        return stopLocations;
    }

    public double[] getStopLocation(String stopName) {
        return stopLocations != null ? stopLocations.get(stopName) : null; // ✅ Prevents NullPointerException
    }
}
