package com.example.dronepathfinder.algorithms;

import android.util.Log;

import com.example.dronepathfinder.objects.AvoidancePoint;

import org.osmdroid.util.GeoPoint;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;

public class DijkstraAlgorithm
{
    private Map<GeoPoint, Map<GeoPoint, Double>> graph = new HashMap<>();
    private Map<GeoPoint, GeoPoint> predecessors = new HashMap<>();

    private void initializeGraph(List<GeoPoint> points, List<AvoidancePoint> avoidancePoints)
    {
        Log.d("DijkstraAlgorithm", "Calling initializeGraph");

        graph.clear();
        predecessors.clear();

        for (int i = 0; i < points.size() - 1; i++)
        {
            GeoPoint current = points.get(i);
            GeoPoint next = points.get(i + 1);

            double weight = current.distanceToAsDouble(next);

            graph.computeIfAbsent(current, k -> new HashMap<>()).put(next, weight);
            graph.computeIfAbsent(next, k -> new HashMap<>()).put(current, weight);
        }

        for (AvoidancePoint avoidancePoint : avoidancePoints)
        {
            GeoPoint point = avoidancePoint.getCenter();
            double radius = avoidancePoint.getRadius();

            for (GeoPoint existingPoint : graph.keySet())
            {
                double distance = existingPoint.distanceToAsDouble(point);

                if (distance <= radius)
                    graph.get(existingPoint).remove(point);
            }
        }
    }


    public List<GeoPoint> findShortestPath(List<GeoPoint> points, List<AvoidancePoint> avoidancePoints)
    {
        Log.d("DijkstraAlgorithm","Calling findShortestPath");

        initializeGraph(points, avoidancePoints);

        GeoPoint start = points.get(0);
        GeoPoint end = points.get(points.size() - 1);

        Log.d("DijkstraAlgorithm", "findShortestPath: points.size(): "
                + points.size());

        Map<GeoPoint, Double> distances = new HashMap<>();
        PriorityQueue<GeoPoint> queue = new PriorityQueue<>((v1, v2)
                -> distances.get(v1).compareTo(distances.get(v2)));

        for (GeoPoint vertex : graph.keySet())
            distances.put(vertex, Double.MAX_VALUE);
        distances.put(start, 0.0);

        queue.add(start);

        while (!queue.isEmpty())
        {
            GeoPoint current = queue.poll();

            Log.d("DijkstraAlgorithm", "findShortestPath: Current vertex: "
                    + current.getLatitude() + ", " + current.getLongitude());

            Map<GeoPoint, Double> edges = graph.get(current);
            if (edges == null)
                continue;

            if (current.equals(end))
                return reconstructPath(end);

            for (Map.Entry<GeoPoint, Double> edge : edges.entrySet())
            {
                GeoPoint neighbor = edge.getKey();
                Double weight = edge.getValue();
                Double distanceThroughCurrent = distances.get(current) + weight;

                if (distanceThroughCurrent < distances.get(neighbor))
                {
                    distances.put(neighbor, distanceThroughCurrent);
                    predecessors.put(neighbor, current);
                    queue.add(neighbor);
                }
            }
        }

        return new ArrayList<>();
    }

    private List<GeoPoint> reconstructPath(GeoPoint end)
    {
        Log.d("DijkstraAlgorithm", "Сalling reconstructPath");

        List<GeoPoint> path = new ArrayList<>();
        GeoPoint step = end;

        if (predecessors.get(step) == null)
        {
            Log.d("DijkstraAlgorithm", "reconstructPath: End point has no predecessor");
            return path;
        }

        path.add(step);

        while (predecessors.get(step) != null)
        {
            step = predecessors.get(step);

            if (path.contains(step))
            {
                Log.d("DijkstraAlgorithm", "reconstructPath: Cycle detected. Exiting loop.");
                break;
            }

            path.add(0, step);
            Log.d("DijkstraAlgorithm", "reconstructPath: Added step: " + step);
        }

        Log.d("DijkstraAlgorithm", "reconstructPath: Path reconstruction complete");
        return path;
    }
}
