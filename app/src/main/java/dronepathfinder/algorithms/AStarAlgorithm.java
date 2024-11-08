package dronepathfinder.algorithms;

import android.util.Log;

import dronepathfinder.objects.AvoidancePoint;

import org.osmdroid.util.GeoPoint;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;

public class AStarAlgorithm implements Algorithm
{
    private final Map<GeoPoint, List<GeoPoint>> adjacencyList = new HashMap<>();
    private final Map<GeoPoint, GeoPoint> cameFrom = new HashMap<>();
    private final Map<GeoPoint, Double> gScore = new HashMap<>();
    private final Map<GeoPoint, Double> fScore = new HashMap<>();
    private final PriorityQueue<GeoPoint> openSet = new PriorityQueue<>(Comparator.comparingDouble(fScore::get));

    public List<GeoPoint> findShortestPath(List<GeoPoint> points, List<AvoidancePoint> avoidancePoints)
    {
        Log.d("AStarAlgorithm", "Calling findShortestPath");

        adjacencyList.clear();
        cameFrom.clear();
        gScore.clear();
        fScore.clear();
        openSet.clear();

        List<GeoPoint> filteredPoints = new ArrayList<>(points);
        filteredPoints.removeIf(point -> isWithinAvoidancePoint(point, avoidancePoints));

        for (int i = 0; i < filteredPoints.size() - 1; i++)
        {
            GeoPoint current = filteredPoints.get(i);
            GeoPoint next = filteredPoints.get(i + 1);
            adjacencyList.computeIfAbsent(current, k -> new ArrayList<>()).add(next);
            adjacencyList.computeIfAbsent(next, k -> new ArrayList<>()).add(current);
            gScore.put(current, Double.MAX_VALUE);
            fScore.put(current, Double.MAX_VALUE);
        }

        GeoPoint start = filteredPoints.get(0);
        GeoPoint goal = filteredPoints.get(filteredPoints.size() - 1);
        gScore.put(start, 0.0);
        fScore.put(start, heuristicCostEstimate(start, goal));

        openSet.add(start);

        while (!openSet.isEmpty())
        {
            GeoPoint current = openSet.poll();

            Log.d("AStarAlgorithm", "Current vertex: " + current.getLatitude() + ", " + current.getLongitude());

            if (current.equals(goal))
                return reconstructPath(cameFrom, current);

            for (GeoPoint neighbor : adjacencyList.getOrDefault(current, new ArrayList<>()))
            {
                double tentativeGScore = gScore.get(current) + current.distanceToAsDouble(neighbor);

                if (tentativeGScore < gScore.getOrDefault(neighbor, Double.MAX_VALUE))
                {
                    cameFrom.put(neighbor, current);
                    gScore.put(neighbor, tentativeGScore);
                    fScore.put(neighbor, tentativeGScore + heuristicCostEstimate(neighbor, goal));

                    if (!openSet.contains(neighbor))
                        openSet.add(neighbor);
                }
            }
        }

        return new ArrayList<>();
    }

    private boolean isWithinAvoidancePoint(GeoPoint point, List<AvoidancePoint> avoidancePoints)
    {
        for (AvoidancePoint avoidancePoint : avoidancePoints)
        {
            if (point.distanceToAsDouble(avoidancePoint.getCenter()) <= avoidancePoint.getRadius()) {
                return true;
            }
        }
        return false;
    }

    private List<GeoPoint> reconstructPath(Map<GeoPoint, GeoPoint> cameFrom, GeoPoint current)
    {
        Log.d("AStarAlgorithm", "Calling reconstructPath");

        List<GeoPoint> totalPath = new ArrayList<>();
        totalPath.add(current);

        while (cameFrom.containsKey(current))
        {
            current = cameFrom.get(current);
            totalPath.add(0, current);

            Log.d("AStarAlgorithm", "Added step: " + current);
        }

        return totalPath;
    }

    private double heuristicCostEstimate(GeoPoint start, GeoPoint goal)
    {
        return start.distanceToAsDouble(goal);
    }
}
