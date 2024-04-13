package com.example.dronepathfinder;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.Polyline;

import java.util.ArrayList;
import java.util.List;

public class MapActivity extends AppCompatActivity
{
    private MapView map = null;
    private GestureDetector gestureDetector;
    private DijkstraAlgorithm dijkstra;
    private List<GeoPoint> points;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        Context ctx = getApplicationContext();
        Configuration.getInstance().load(ctx, PreferenceManager.getDefaultSharedPreferences(ctx));

        setContentView(R.layout.activity_map);

        map = (MapView) findViewById(R.id.map);
        map.setTileSource(TileSourceFactory.MAPNIK);

        map.setBuiltInZoomControls(true);
        map.setMultiTouchControls(true);

        dijkstra = new DijkstraAlgorithm();
        points = new ArrayList<>();
        gestureDetector = new GestureDetector(this, new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onSingleTapConfirmed(MotionEvent e) {
                GeoPoint point = (GeoPoint) map.getProjection().fromPixels((int) e.getX(), (int) e.getY());

                points.add(point);
                Marker marker = new Marker(map);
                marker.setPosition(point);
                marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
                map.getOverlays().add(marker);

                if (points.size() > 1)
                {
                    dijkstra.initializeGraph(points);
                    List<GeoPoint> shortestPath = dijkstra.findShortestPath(points.get(0), points.get(points.size() - 1));
                    displayShortestPath(shortestPath);
                }

                map.invalidate();
                return true;
            }

        });

        map.setOnTouchListener((v, event) ->
        {
            gestureDetector.onTouchEvent(event);
            return false;
        });
    }

    @Override
    public void onResume()
    {
        super.onResume();

        SharedPreferences prefs = getSharedPreferences("MapPrefs", MODE_PRIVATE);
        double lat = prefs.getFloat("Lat", 49.2352f);
        double lon = prefs.getFloat("Lon", 28.4692f);
        double zoom = prefs.getFloat("ZoomLevel", 13.5f);
        map.getController().setCenter(new GeoPoint(lat, lon));
        map.getController().setZoom(zoom);

        map.onResume();
    }

    @Override
    public void onPause()
    {
        super.onPause();

        SharedPreferences prefs = getSharedPreferences("MapPrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putFloat("Lat", (float) map.getMapCenter().getLatitude());
        editor.putFloat("Lon", (float) map.getMapCenter().getLongitude());
        editor.putFloat("ZoomLevel", (float) map.getZoomLevelDouble());
        editor.apply();

        map.onPause();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults)
    {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        int REQUEST_PERMISSIONS_REQUEST_CODE = 1;
        if (requestCode == REQUEST_PERMISSIONS_REQUEST_CODE)
        {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
            {
            } else
            {
            }
        }
    }

    private void displayShortestPath(List<GeoPoint> path)
    {
        Log.d("MapActivity", "Displaying shortest path with size: " + path.size());
        for (GeoPoint point : path)
        {
            Log.d("MapActivity", "Point: " + point.getLatitude() + ", " + point.getLongitude());
        }

        Polyline line = new Polyline();
        line.setPoints(path);
        line.setColor(Color.BLUE);
        line.setWidth(10.0f);

        map.getOverlays().add(line);
    }
}
