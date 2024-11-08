package dronepathfinder.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.InputType;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.content.res.ResourcesCompat;

import com.example.dronepathfinder.R;
import dronepathfinder.algorithms.AStarAlgorithm;
import dronepathfinder.objects.AvoidancePoint;
import dronepathfinder.objects.Route;
import dronepathfinder.algorithms.DijkstraAlgorithm;

import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.Polygon;
import org.osmdroid.views.overlay.Polyline;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MapActivity extends AppCompatActivity
{
    private MapView map = null;
    private GestureDetector gestureDetector;
    private String selectedAlgorithm;
    private DijkstraAlgorithm dijkstraAlgorithm;
    private AStarAlgorithm aStarAlgorithm;
    private List<GeoPoint> points;
    private List<AvoidancePoint> avoidancePoints;
    private List<GeoPoint> shortestPath = null;
    private Map<GeoPoint, Marker> markersMap;
    private Polyline currentLine;
    private List<Polygon> avoidanceCircles = new ArrayList<>();

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

        map.setBuiltInZoomControls(false);
        map.setMultiTouchControls(true);

        selectedAlgorithm = loadSelectedAlgorithm();
        dijkstraAlgorithm = new DijkstraAlgorithm();
        aStarAlgorithm = new AStarAlgorithm();
        points = new ArrayList<>();
        avoidancePoints = new ArrayList<>();
        markersMap = new HashMap<>();
        currentLine = null;
        Context context = this;

        Drawable vectorDrawable = ResourcesCompat.getDrawable(getResources(), R.drawable.map_location_pin, null);
        Bitmap bitmap = Bitmap.createBitmap(vectorDrawable.getIntrinsicWidth(), vectorDrawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        vectorDrawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        vectorDrawable.draw(canvas);
        Drawable customIcon = new BitmapDrawable(getResources(), Bitmap.createScaledBitmap(bitmap, (int) (48.0f * getResources().getDisplayMetrics().density), (int) (48.0f * getResources().getDisplayMetrics().density), true));

        Button btnSaveRoute = findViewById(R.id.create_route);
        btnSaveRoute.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if (points.size() > 1)
                {
                    saveNewRoute(shortestPath, avoidancePoints);
                }
            }
        });

        gestureDetector = new GestureDetector(this, new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onSingleTapConfirmed(MotionEvent e)
            {
                GeoPoint tapPoint = (GeoPoint) map.getProjection().fromPixels((int) e.getX(), (int) e.getY());
                Marker foundMarker = null;

                for (Marker marker : markersMap.values()) {
                    if (tapPoint.distanceToAsDouble(marker.getPosition()) <= 2500 / map.getZoomLevelDouble()) {
                        foundMarker = marker;
                        break;
                    }
                }

                if (foundMarker != null)
                {
                    Log.d("MapActivity", "Deleting marker " + foundMarker.toString());

                    map.getOverlays().remove(foundMarker);
                    markersMap.remove(tapPoint);
                    points.remove(foundMarker.getPosition());
                }
                else
                {
                    Log.d("MapActivity", "Adding new marker");

                    foundMarker = new Marker(map);
                    foundMarker.setPosition(tapPoint);
                    foundMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
                    foundMarker.setIcon(customIcon);

                    map.getOverlays().add(foundMarker);
                    markersMap.put(tapPoint, foundMarker);
                    points.add(tapPoint);
                }

                if(points.size() > 1)
                    shortestPath = findShortestPath(points, avoidancePoints);

                updateMap(shortestPath, avoidancePoints);

                return true;
            }

            @Override
            public void onLongPress(MotionEvent e)
            {
                GeoPoint longPressPoint = (GeoPoint) map.getProjection().fromPixels((int) e.getX(), (int) e.getY());
                AvoidancePoint foundAvoidancePoint = null;

                for (AvoidancePoint avoidancePoint : avoidancePoints) {
                    if (longPressPoint.distanceToAsDouble(avoidancePoint.getCenter()) <= 10000.0 / map.getZoomLevelDouble()) {
                        foundAvoidancePoint = avoidancePoint;
                        break;
                    }
                }

                if (foundAvoidancePoint != null)
                {
                    avoidancePoints.remove(foundAvoidancePoint);
                }
                else
                {
                    AlertDialog.Builder builder = new AlertDialog.Builder(context);
                    builder.setTitle(R.string.alert_title_enter_radius);

                    final EditText input = new EditText(context);
                    input.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
                    builder.setView(input);

                    builder.setPositiveButton(R.string.alert_answer_add, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which)
                        {
                            double avoidanceRadius = Double.parseDouble(input.getText().toString());
                            avoidancePoints.add(new AvoidancePoint(longPressPoint, avoidanceRadius));
                            updateMap(shortestPath, avoidancePoints);
                        }
                    });

                    builder.setNegativeButton(R.string.alert_answer_cancel, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which)
                        {
                            dialog.cancel();
                        }
                    });

                    builder.show();;
                }

                if(points.size() > 1)
                    shortestPath = findShortestPath(points, avoidancePoints);

                updateMap(shortestPath, avoidancePoints);
            }
        });

        map.setOnTouchListener((v, event) -> {
            gestureDetector.onTouchEvent(event);
            return false;
        });
    }

    private List<GeoPoint> findShortestPath(List<GeoPoint> points, List<AvoidancePoint> avoidancePoints)
    {
        switch (selectedAlgorithm)
        {
            case "A*":
                return aStarAlgorithm.findShortestPath(points, avoidancePoints);
            case "Dijkstra":
            default:
                return dijkstraAlgorithm.findShortestPath(points, avoidancePoints);
        }
    }

    private void updateMap(List<GeoPoint> path, List<AvoidancePoint> avoidancePoints)
    {
        Log.d("MapActivity","Calling updateMap");

        for (Polygon circle : avoidanceCircles)
            map.getOverlays().remove(circle);

        avoidanceCircles.clear();

        if (currentLine != null)
            map.getOverlays().remove(currentLine);

        if (path != null)
        {
            currentLine = new Polyline();
            currentLine.setPoints(path);
            currentLine.setColor(Color.BLUE);
            currentLine.setWidth(10.0f);
            map.getOverlays().add(currentLine);
        }

        for (AvoidancePoint avoidancePoint : avoidancePoints)
        {
            GeoPoint point = avoidancePoint.getCenter();
            double radius = avoidancePoint.getRadius();

            Polygon circle = new Polygon();
            circle.setPoints(Polygon.pointsAsCircle(point, radius));
            circle.setStrokeColor(Color.RED);
            circle.setStrokeWidth(2.0f);
            circle.setFillColor(Color.argb(100, 255, 0, 0));
            map.getOverlays().add(circle);
            avoidanceCircles.add(circle);
        }

        map.invalidate();
    }

    private String loadSelectedAlgorithm()
    {
        SharedPreferences preferences = getApplication().getSharedPreferences("SettingsPrefs", Context.MODE_PRIVATE);
        return preferences.getString("selectedAlgorithm", "");
    }

    public void saveNewRoute(List<GeoPoint> points, List<AvoidancePoint> avoidancePoints)
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.alert_title_enter_route_name);

        final EditText input = new EditText(this);
        builder.setView(input);

        builder.setPositiveButton(R.string.alert_answer_save, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                String name = input.getText().toString();
                int weight = 0;
                Intent returnIntent = new Intent();
                returnIntent.putExtra("route", new Route(name, points, avoidancePoints, weight));
                setResult(Activity.RESULT_OK, returnIntent);
                finish();
            }
        });

        builder.setNegativeButton(R.string.alert_answer_cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                dialog.cancel();
            }
        });

        builder.show();
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
}
