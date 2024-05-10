package com.example.dronepathfinder.objects;

import android.util.Log;

import java.io.Serializable;

public class Drone extends Object implements Serializable
{
    private static final long serialVersionUID = 1L;
    private double flightDistance; //meters
    private double speed; //meters per seconds

    public Drone (String name, double flightDistance, double speed)
    {
        super(name);

        this.flightDistance = flightDistance;
        this.speed = speed;

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
}
