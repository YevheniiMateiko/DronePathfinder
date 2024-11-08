package dronepathfinder.algorithms;

import dronepathfinder.objects.AvoidancePoint;

import org.osmdroid.util.GeoPoint;

import java.util.List;

interface Algorithm
{
    List<GeoPoint> findShortestPath(List<GeoPoint> points, List<AvoidancePoint> avoidancePoints);
}
