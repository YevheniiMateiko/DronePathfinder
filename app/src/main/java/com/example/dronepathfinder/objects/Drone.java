package com.example.dronepathfinder.objects;

public class Drone {

    private String name;
    private double maxFlightDistance;

    public Drone (String name, double maxFlightDistance)
    {
        this.name = name;
        this.maxFlightDistance = maxFlightDistance;
    }

    public String getName() {
        return name;
    }
    public double getMaxFlightDistance() {
        return maxFlightDistance;
    }
}
