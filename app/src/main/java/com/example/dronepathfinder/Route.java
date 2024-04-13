package com.example.dronepathfinder;

import org.osmdroid.util.GeoPoint;

import java.io.Serializable;
import java.util.List;

public class Route implements Serializable {
    private String name;
    private List<GeoPoint> points;
    private double length;

    public Route(String name, List<GeoPoint> points, double length) {
        this.name = name;
        this.points = points;
        this.length = length;
    }

    public List<GeoPoint> getPoints() {
        return points;
    }

    public String getName() { return name; }
    public double getLength() {
        return length;
    }

    public GeoPoint getStart() {
        return points.get(0);
    }

    public GeoPoint getEnd() {
        return points.get(points.size() - 1);
    }
}
