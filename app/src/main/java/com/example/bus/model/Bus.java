package com.example.bus.model;

public class Bus {
    private String busName;
    private String assignedRoute;
    private double fare;
    private int totalTime;

    // Temporary variables for adjusted time and fare (used in sorting)
    private transient int adjustedTime;
    private transient double adjustedFare;

    // ✅ Constructor to initialize a Bus object
    public Bus(String busName, String assignedRoute, double fare, int totalTime) {
        this.busName = busName;
        this.assignedRoute = assignedRoute;
        this.fare = fare;
        this.totalTime = totalTime;
    }

    // Getters and Setters
    public String getBusName() { return busName; }
    public String getAssignedRoute() { return assignedRoute; }
    public double getFare() { return fare; }
    public int getTotalTime() { return totalTime; }

    public int getAdjustedTime() { return adjustedTime; }
    public void setAdjustedTime(int adjustedTime) { this.adjustedTime = adjustedTime; }

    public double getAdjustedFare() { return adjustedFare; }
    public void setAdjustedFare(double adjustedFare) { this.adjustedFare = adjustedFare; }
}
