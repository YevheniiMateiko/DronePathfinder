package com.example.dronepathfinder.objects;

import android.util.Log;

public class Drone {

    private String name;
    private double maxFlightDistance;

    public Drone (String name, double maxFlightDistance)
    {
        Log.d("New drone", "Created new drone with name " + name);
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
