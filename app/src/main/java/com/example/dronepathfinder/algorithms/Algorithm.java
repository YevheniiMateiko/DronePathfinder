package com.example.dronepathfinder.algorithms;

import com.example.dronepathfinder.objects.AvoidancePoint;

import org.osmdroid.util.GeoPoint;

import java.util.List;

interface Algorithm
{
    List<GeoPoint> findShortestPath(List<GeoPoint> points, List<AvoidancePoint> avoidancePoints);
}
