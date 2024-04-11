package com.example.dronepathfinder;

import android.util.Log;

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

    public void initializeGraph(List<GeoPoint> points)
    {
        for (int i = 0; i < points.size() - 1; i++) {
            GeoPoint current = points.get(i);
            GeoPoint next = points.get(i + 1);

            double weight = current.distanceToAsDouble(next);

            graph.computeIfAbsent(current, k -> new HashMap<>()).put(next, weight);
            graph.computeIfAbsent(next, k -> new HashMap<>()).put(current, weight); // Для неорієнтованого графу
        }
    }
    public List<GeoPoint> findShortestPath(GeoPoint start, GeoPoint end)
    {
        Log.d("DijkstraAlgorithm", "Graph contains start: " + graph.containsKey(start) + ", end: " + graph.containsKey(end));

        Map<GeoPoint, Double> distances = new HashMap<>();
        PriorityQueue<GeoPoint> queue = new PriorityQueue<>((v1, v2) -> distances.get(v1).compareTo(distances.get(v2)));

        for (GeoPoint vertex : graph.keySet())
        {
            distances.put(vertex, Double.MAX_VALUE);
        }

        distances.put(start, 0.0);
        queue.add(start);

        while (!queue.isEmpty())
        {
            GeoPoint current = queue.poll();

            Log.d("DijkstraAlgorithm", "Current vertex: " + current.getLatitude() + ", " + current.getLongitude());

            Map<GeoPoint, Double> edges = graph.get(current);
            if (edges == null)
            {
                continue;
            }

            if (current.equals(end))
            {
                return reconstructPath(end);
            }

            for (Map.Entry<GeoPoint, Double> edge : graph.get(current).entrySet())
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
        List<GeoPoint> path = new ArrayList<>();
        GeoPoint step = end;

        if (predecessors.get(step) == null)
        {
            return path;
        }

        path.add(step);
        while (predecessors.get(step) != null)
        {
            step = predecessors.get(step);
            path.add(0, step);
        }

        return path;
    }
}
