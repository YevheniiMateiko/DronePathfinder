package dronepathfinder.objects;

import org.osmdroid.util.GeoPoint;

import java.io.Serializable;

public class AvoidancePoint implements Serializable
{
    private static final long serialVersionUID = 1L;
    private GeoPoint center;
    private double radius;

    public AvoidancePoint(GeoPoint center, double radius)
    {
        this.center = center;
        this.radius = radius;
    }

    public GeoPoint getCenter()
    {
        return center;
    }

    public double getRadius()
    {
        return radius;
    }
}
