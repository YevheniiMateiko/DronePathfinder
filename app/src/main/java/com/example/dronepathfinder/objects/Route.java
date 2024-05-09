package com.example.dronepathfinder.objects;

import android.util.Log;
import android.util.Pair;

import org.osmdroid.util.GeoPoint;

import java.io.Serializable;
import java.util.List;

public class Route implements Serializable {

    public enum Status
    {
        GOOD,
        WARNING,
        BAD

    }

    private String name;
    private List<GeoPoint> points;
    private List<Pair<GeoPoint, Double>> avoidancePoints;
    //private int num_of_points;
    private double length;
    private Drone drone;
    private Status status;
    private boolean pinned;

    public Route(String name, List<GeoPoint> points, List<Pair<GeoPoint, Double>> avoidancePoints)
    {
        Log.d("New route", "Created new route with name " + name);
        this.name = name;
        this.points = points;
        this.avoidancePoints = avoidancePoints;
        //this.num_of_points = points.size();
        this.length = calculateTotalLength(points);
        this.drone = null;
        this.status = updateStatus();
        this.pinned = false;
    }

    public String getName()
    {
        return name;
    }
    public List<GeoPoint> getPoints()
    {
        return points;
    }
    public List<Pair<GeoPoint, Double>> getAvoidancePoints()
    {
        return avoidancePoints;
    }
    public Pair<Double, Double> getStart()
    {
        return new Pair<Double, Double>(points.get(0).getLatitude(), points.get(0).getLongitude());
    }
    public Pair<Double, Double> getEnd()
    {
        return new Pair<Double, Double>(points.get(points.size() - 1).getLatitude(), points.get(points.size() - 1).getLongitude());
    }
    public double getLength()
    {
        return length;
    }
    public Drone getDrone()
    {
        return drone;
    }
    public Status getStatus() {
        return status;
    }

    public void setDrone(Drone drone)
    {
        this.drone = drone;
    }
    private Status updateStatus()
    {
        if (this.drone == null)
            return Status.WARNING;
        else if (this.drone.getMaxFlightDistance() < this.length)
        {
            return Status.BAD;
        }
        else
            return Status.GOOD;
    }

    private double calculateTotalLength(List<GeoPoint> points)
    {
        double totalLength = 0.0;

        for (int i = 0; i < points.size() - 1; i++)
            totalLength += points.get(i).distanceToAsDouble(points.get(i + 1));

        return totalLength;
    }
}
