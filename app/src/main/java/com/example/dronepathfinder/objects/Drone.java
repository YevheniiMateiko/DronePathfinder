package com.example.dronepathfinder.objects;

import android.util.Log;

import java.io.Serializable;

public class Drone extends Object implements Serializable
{
    private static final long serialVersionUID = 2L;
    private double flightDistance; //meters
    private double speed; //meters per seconds
    private int payload; //kilograms

    public Drone (String name, double flightDistance, double speed, int payload)
    {
        super(name);

        this.flightDistance = flightDistance;
        this.speed = speed;
        this.payload = payload;

        Log.d("New drone", "Created new drone with name " + name);
    }

    public double getFlightDistance()
    {
        return flightDistance;
    }

    public double getSpeed()
    {
        return speed;
    }

    public int getPayload() {
        return payload;
    }
}
