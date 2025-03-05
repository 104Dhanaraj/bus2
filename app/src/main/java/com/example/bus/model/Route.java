package com.example.bus.model;

import java.util.List;

public class Route {
    private String routeName;
    private List<Stop> stops;

    public Route(String routeName, List<Stop> stops) {
        this.routeName = routeName;
        this.stops = stops;
    }

    public String getRouteName() {
        return routeName;
    }

    public List<Stop> getStops() {
        return stops;
    }

    public Stop getStop(String stopName) {
        for (Stop stop : stops) {
            if (stop.getName().equals(stopName)) {
                return stop;
            }
        }
        return null; // Stop not found
    }
}
