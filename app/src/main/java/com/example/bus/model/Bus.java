package com.example.bus.model;

public class Bus {
    private String busName;
    private String assignedRoute;
    private double fare;
    private int totalTime; // in minutes

    public Bus(String busName, String assignedRoute, double fare, int totalTime) {
        this.busName = busName;
        this.assignedRoute = assignedRoute;
        this.fare = fare;
        this.totalTime = totalTime;
    }

    public String getBusName() {
        return busName;
    }

    public String getAssignedRoute() {
        return assignedRoute;
    }

    public double getFare() {
        return fare;
    }

    public int getTotalTime() {
        return totalTime;
    }
}
