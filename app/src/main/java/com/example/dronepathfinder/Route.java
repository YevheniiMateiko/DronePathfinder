package com.example.dronepathfinder;

import android.util.Pair;

import org.json.JSONObject;
import org.osmdroid.util.GeoPoint;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

public class Route implements Serializable {

    public enum Status
    {
        GOOD,
        WARNING

    }

    private String name;
    private String drone = "Mavic 3";
    private List<GeoPoint> points;
    private int num_of_points;
    private double length;
    private Status status;

    public Route(String name, List<GeoPoint> points)
    {
        this.name = name;
        this.points = points;
        this.num_of_points = points.size();
        this.length = calculateTotalLength(points);
        this.status = Status.GOOD;
    }


    public List<GeoPoint> getPoints()
    {
        return points;
    }
    public String getName()
    {
        return name;
    }
    public String getDrone()
    {
        return drone;
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
    public Status getStatus() {
        return status;
    }

    public void setDrone(String drone)
    {
        this.drone = drone;
    }
    public void setStatus(Status status)
    {
        this.status = status;
    }

    private double calculateTotalLength(List<GeoPoint> points)
    {
        double totalLength = 0.0;

        for (int i = 0; i < points.size() - 1; i++)
            totalLength += points.get(i).distanceToAsDouble(points.get(i + 1));

        return totalLength;
    }
}
