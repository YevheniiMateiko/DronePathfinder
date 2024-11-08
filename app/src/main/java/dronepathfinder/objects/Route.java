package dronepathfinder.objects;

import android.util.Log;

import org.osmdroid.util.GeoPoint;

import java.io.Serializable;
import java.util.List;

public class Route extends Object implements Serializable
{
    public enum Status
    {
        GOOD,
        NO_DRONE,
        ROUTE_TOO_LONG,
        OVERWEIGHT
    }

    private static final long serialVersionUID = 2L;
    private List<GeoPoint> points;
    private List<AvoidancePoint> avoidancePoints;
    private double areaOfAvoidancePoints; //meters^2
    private double length; //meters
    private int timeToComplete; //seconds
    private int weight; //kilograms
    private Drone drone;
    private Status status;

    public Route(String name, List<GeoPoint> points, List<AvoidancePoint> avoidancePoints, int weight)
    {
        super(name);

        this.drone = null;
        this.points = points;
        this.avoidancePoints = avoidancePoints;
        this.areaOfAvoidancePoints = calculateAreaOfAvoidancePoints(avoidancePoints);
        this.weight = weight;
        this.length = calculateTotalLength(points);
        this.timeToComplete = calculateTimeToComplete(drone);
        this.status = updateStatus();

        Log.d("New route", "Created new route: " + name);
    }

    public GeoPoint getStart()
    {
        return points.get(0);
    }

    public GeoPoint getEnd()
    {
        return points.get(points.size() - 1);
    }

    public double getAreaOfAvoidancePoints()
    {
        return areaOfAvoidancePoints;
    }

    public int getWeight()
    {
        return weight;
    }

    public double getLength()
    {
        return length;
    }

    public int getTimeToComplete()
    {
        return timeToComplete;
    }

    public int getNumberOfPoints()
    {
        if (points != null)
            return points.size();

        return 0;
    }

    public int getNumberOfAvoidancePoints()
    {
        if (avoidancePoints != null)
            return avoidancePoints.size();

        return 0;
    }

    public String getDroneName()
    {
        if (this.drone != null)
            return drone.getName();

        return "";
    }

    public Status getStatus()
    {
        return status;
    }

    public void setDrone(Drone drone)
    {
        Log.d("Route", "Successfully setted drone " + drone.getName());
        this.drone = drone;
        this.status = updateStatus();
        this.timeToComplete = calculateTimeToComplete(drone);
    }

    public void setWeight(int weight)
    {
        this.weight = weight;
        this.status = updateStatus();
    }

    private Status updateStatus()
    {
        if (drone == null)
        {
            return Status.NO_DRONE;
        }
        else if (drone.getFlightDistance() < this.length)
        {
            return Status.ROUTE_TOO_LONG;
        }
        else if (drone.getPayload() < this.weight)
        {
            return Status.OVERWEIGHT;
        }
        else
        {
            return Status.GOOD;
        }
    }

    private double calculateTotalLength(List<GeoPoint> points)
    {
        double totalLength = 0.0;

        for (int i = 0; i < points.size() - 1; i++)
            totalLength += points.get(i).distanceToAsDouble(points.get(i + 1));

        return totalLength;
    }

    private int calculateTimeToComplete(Drone drone)
    {
        if (drone != null)
            return (int) (this.length/drone.getSpeed());

        return 0;
    }

    private double calculateAreaOfAvoidancePoints(List<AvoidancePoint> avoidancePoints)
    {
        if(this.avoidancePoints != null)
        {
            double area = 0.0;

            for (AvoidancePoint avoidancePoint: avoidancePoints)
                area += avoidancePoint.getRadius() * avoidancePoint.getRadius() * 3.1415;

            return area;
        }

        return 0.0;
    }
}
