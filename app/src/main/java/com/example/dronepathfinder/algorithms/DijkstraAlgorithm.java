package com.example.dronepathfinder.algorithms;

import android.util.Log;
import android.util.Pair;

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

    public Map<GeoPoint, Map<GeoPoint, Double>> getGraph()
    {
        return graph;
    }
    public Map<GeoPoint, GeoPoint> getPredecessors()
    {
        return predecessors;
    }

    public void initializeGraph(List<GeoPoint> points, List<Pair<GeoPoint, Double>> avoidancePoints)
    {
        for (int i = 0; i < points.size() - 1; i++)
        {
            GeoPoint current = points.get(i);
            GeoPoint next = points.get(i + 1);

            double weight = current.distanceToAsDouble(next);

            graph.computeIfAbsent(current, k -> new HashMap<>()).put(next, weight);
            graph.computeIfAbsent(next, k -> new HashMap<>()).put(current, weight);
        }

        for (Pair<GeoPoint, Double> avoidancePoint : avoidancePoints) {
            GeoPoint point = avoidancePoint.first;
            double radius = avoidancePoint.second;

            for (GeoPoint existingPoint : graph.keySet())
            {
                double distance = existingPoint.distanceToAsDouble(point);
                if (distance <= radius) {
                    graph.get(existingPoint).remove(point);
                }
            }
        }
    }
    public List<GeoPoint> findShortestPath(List<GeoPoint> points, List<Pair<GeoPoint, Double>> avoidancePoints)
    {
        // Ініціалізація графа зі списку точок
        initializeGraph(points, avoidancePoints);

        // Визначення початкової та кінцевої точок як першої та останньої у списку
        GeoPoint start = points.get(0);
        GeoPoint end = points.get(points.size() - 1);

        // Перевірка наявності початкової та кінцевої точок у графі
        /*Log.d("DijkstraAlgorithm", "Graph contains start: "
                + graph.containsKey(start) + ", end: " + graph.containsKey(end));*/

        // Ініціалізація карти відстаней та пріоритетної черги
        Map<GeoPoint, Double> distances = new HashMap<>();
        PriorityQueue<GeoPoint> queue = new PriorityQueue<>((v1, v2)
                -> distances.get(v1).compareTo(distances.get(v2)));

        // Встановлення відстані до початкової точки як нуль
        // та до всіх інших точок як нескінченність
        for (GeoPoint vertex : graph.keySet())
            distances.put(vertex, Double.MAX_VALUE);
        distances.put(start, 0.0);

        // Додавання початкової точки до черги для подальшої обробки
        queue.add(start);

        while (!queue.isEmpty())
        {
            GeoPoint current = queue.poll();

            /*Log.d("DijkstraAlgorithm", "Current vertex: "
                    + current.getLatitude() + ", " + current.getLongitude());*/

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

    public List<GeoPoint> reconstructPath(GeoPoint end)
    {
        List<GeoPoint> path = new ArrayList<>();
        GeoPoint step = end;

        if (predecessors.get(step) == null)
            return path;

        path.add(step);

        while (predecessors.get(step) != null)
        {
            step = predecessors.get(step);
            path.add(0, step);
        }

        return path;
    }
}
