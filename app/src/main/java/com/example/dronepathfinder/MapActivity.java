package com.example.dronepathfinder;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.content.res.ResourcesCompat;

import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.Polyline;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MapActivity extends AppCompatActivity
{
    private MapView map = null;
    private GestureDetector gestureDetector;
    private DijkstraAlgorithm dijkstra;
    private List<GeoPoint> points;
    private List<GeoPoint> shortestPath = null;
    private Map<GeoPoint, Marker> markersMap;
    private Polyline currentLine;

    @Override
    public void onCreate(Bundle savedInstanceState) {
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
        markersMap = new HashMap<>();
        currentLine = null;

        Drawable vectorDrawable = ResourcesCompat.getDrawable(getResources(), R.drawable.map_location_pin, null);
        Bitmap bitmap = Bitmap.createBitmap(vectorDrawable.getIntrinsicWidth(), vectorDrawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        vectorDrawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        vectorDrawable.draw(canvas);
        Drawable customIcon = new BitmapDrawable(getResources(), Bitmap.createScaledBitmap(bitmap, (int) (48.0f * getResources().getDisplayMetrics().density), (int) (48.0f * getResources().getDisplayMetrics().density), true));

        Button btnSaveRoute = findViewById(R.id.save_route);
        btnSaveRoute.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if (points.size() > 1)
                {
                    saveNewRoute("New route", shortestPath);
                }
            }
        });

        gestureDetector = new GestureDetector(this, new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onSingleTapConfirmed(MotionEvent e)
            {
                GeoPoint tapPoint = (GeoPoint) map.getProjection().fromPixels((int) e.getX(), (int) e.getY());
                boolean isMarkerTapped = false;

                Log.d("Tap", "GeoPoint: " + tapPoint);

                for (Marker marker : markersMap.values())
                {
                    Log.d("Tap", "Marker position: " + marker.getPosition());

                    if (marker.hitTest(e, map))
                    {
                        map.getOverlays().remove(marker);
                        markersMap.remove(marker.getPosition());
                        points.remove(marker.getPosition());
                        isMarkerTapped = true;
                        break;
                    }
                }

                if (!isMarkerTapped)
                {
                    Marker marker = new Marker(map);
                    marker.setPosition(tapPoint);
                    marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);

                    marker.setIcon(customIcon);

                    map.getOverlays().add(marker);
                    markersMap.put(tapPoint, marker);
                    points.add(tapPoint);

                    Log.d("Marker", "Marker created at Lat: " + tapPoint.getLatitude() + ", Lon: " + tapPoint.getLongitude());

                    /*
                    Polygon boundsPolygon = new Polygon(map);
                    BoundingBox boundingBox = marker.getBounds();
                    List<GeoPoint> boundsPoints = new ArrayList<>();
                    boundsPoints.add(new GeoPoint(boundingBox.getLatNorth(), boundingBox.getLonWest()));
                    boundsPoints.add(new GeoPoint(boundingBox.getLatNorth(), boundingBox.getLonEast()));
                    boundsPoints.add(new GeoPoint(boundingBox.getLatSouth(), boundingBox.getLonEast()));
                    boundsPoints.add(new GeoPoint(boundingBox.getLatSouth(), boundingBox.getLonWest()));
                    boundsPolygon.setPoints(boundsPoints);

                    boundsPolygon.getFillPaint().setColor(Color.TRANSPARENT);
                    boundsPolygon.getOutlinePaint().setColor(Color.RED);
                    boundsPolygon.getOutlinePaint().setStrokeWidth(2);

                    map.getOverlays().add(boundsPolygon);

                    Log.d("Bounds", "Bounds created for Lat: " + boundingBox.getLatNorth() + ", Lon: " + boundingBox.getLonWest()
                            + " to Lat: " + boundingBox.getLatSouth() + ", Lon: " + boundingBox.getLonEast());
                     */
                }

                if (points.size() > 1)
                {
                    dijkstra.initializeGraph(points);
                    shortestPath = dijkstra.findShortestPath(points.get(0), points.get(points.size() - 1));
                    displayShortestPath(shortestPath);
                }

                map.invalidate();
                return true;
            }
        });

        map.setOnTouchListener((v, event) -> {
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
        if (currentLine != null)
        {
            map.getOverlays().remove(currentLine);
        }

        Log.d("MapActivity", "Displaying shortest path with size: " + path.size());
        for (GeoPoint point : path) {
            Log.d("MapActivity", "Point: " + point.getLatitude() + ", " + point.getLongitude());
        }

        currentLine = new Polyline();
        currentLine.setPoints(path);
        currentLine.setColor(Color.BLUE);
        currentLine.setWidth(10.0f);

        map.getOverlays().add(currentLine);
        map.invalidate();
    }

    public void saveNewRoute(String name, List<GeoPoint> points)
    {
        Intent returnIntent = new Intent();
        returnIntent.putExtra("route", new Route(name, points));
        setResult(Activity.RESULT_OK, returnIntent);
        finish();
    }
}
