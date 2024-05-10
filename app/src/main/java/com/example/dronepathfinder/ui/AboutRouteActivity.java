package com.example.dronepathfinder.ui;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.dronepathfinder.R;
import com.example.dronepathfinder.objects.Route;

public class AboutRouteActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about_route);

        Route route = (Route) getIntent().getSerializableExtra("route");

        TextView tv_route_name = findViewById(R.id.tv_route_name_value);
        TextView tv_route_start = findViewById(R.id.tv_route_start_value);
        TextView tv_route_end = findViewById(R.id.tv_route_end_value);
        TextView tv_route_points = findViewById(R.id.tv_route_points_value);
        TextView tv_route_avoidpoints = findViewById(R.id.tv_route_avoidpoints_value);
        TextView tv_route_length = findViewById(R.id.tv_route_length_value);
        TextView tv_route_time = findViewById(R.id.tv_route_time_value);
        TextView tv_route_drone = findViewById(R.id.tv_route_drone_value);

        if (route != null)
        {
            tv_route_name.setText(route.getName());
            tv_route_start.setText(String.format("%.3f, %.3f", route.getStart().first, route.getStart().second));
            tv_route_end.setText(String.format("%.3f, %.3f", route.getEnd().first, route.getEnd().second));
            tv_route_points.setText(String.valueOf(route.getNumberOfPoints()));
            tv_route_avoidpoints.setText(String.valueOf(route.getNumberOfAvoidancePoints()));
            tv_route_length.setText(String.format("%.3f %s", route.getLength()/1_000, getString(R.string.menu_route_km)));
            tv_route_time.setText(getFormatedTime(route.getTimeToComplete()));
            tv_route_drone.setText(route.getDroneName());
        }
    }

    private String getFormatedTime(int totalSeconds)
    {
        int hours = totalSeconds / 3600;
        int minutes = (totalSeconds % 3600) / 60;
        int seconds = totalSeconds % 60;

        return String.format("%d%s %d%s %d%s",
                hours, getString(R.string.menu_route_hours),
                minutes, getString(R.string.menu_route_minutes),
                seconds, getString(R.string.menu_route_seconds));
    }
}