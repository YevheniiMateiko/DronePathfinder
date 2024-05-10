package com.example.dronepathfinder.objects;

import android.util.Log;
import android.util.Pair;

import org.osmdroid.util.GeoPoint;

import java.io.Serializable;
import java.util.List;

public class Route extends Object implements Serializable
{
    public enum Status
    {
        GOOD,
        WARNING,
        BAD
    }

    private static final long serialVersionUID = 1L;
    private List<GeoPoint> points;
    private List<AvoidancePoint> avoidancePoints;
    private int num_of_points;
    private int num_of_avoidance_points;
    private double length; //meters
    private int timeToComplete; //seconds
    private Drone drone;
    private Status status;

    public Route(String name, List<GeoPoint> points, List<AvoidancePoint> avoidancePoints)
    {
        super(name);

        this.points = points;
        this.avoidancePoints = avoidancePoints;
        this.num_of_points = points.size();
        this.num_of_avoidance_points = avoidancePoints.size();
        this.length = calculateTotalLength(points);
        this.timeToComplete = 0;
        this.drone = null;
        this.status = updateStatus();

        Log.d("New route", "Created new route: " + name);
    }

    public List<GeoPoint> getPoints()
    {
        return points;
    }

    public List<AvoidancePoint> getAvoidancePoints()
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

    public int getTimeToComplete()
    {
        return timeToComplete;
    }

    public String getDroneName()
    {
        if (this.drone == null)
            return "";
        else
            return drone.getName();
    }

    public Status getStatus()
    {
        return status;
    }

    public void setDrone(Drone drone)
    {
        this.drone = drone;
        this.status = updateStatus();
        this.timeToComplete = calculateTimeToComplete();
    }

    private Status updateStatus()
    {
        if (this.drone == null)
            return Status.WARNING;
        else if (this.drone.getFlightDistance() < this.length)
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

    private int calculateTimeToComplete()
    {
        return (int) (this.length/this.drone.getSpeed());
    }
}
