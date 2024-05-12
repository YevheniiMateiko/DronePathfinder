package com.example.dronepathfinder.ui;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.example.dronepathfinder.R;
import com.example.dronepathfinder.objects.Route;

public class AboutRouteActivity extends AppCompatActivity {

    private Route route;
    private int routePosition;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        setContentView(R.layout.activity_about_route);

        route = (Route) getIntent().getSerializableExtra("route");
        routePosition = (int) getIntent().getIntExtra("routePosition", -1);

        updateSpecsUI(route);
        updateStatusUI(route);

        Button deleteButton = findViewById(R.id.delete_route);
        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDeleteConfirmationDialog();
            }
        });

    }

    private String getFormatedTime(int totalSeconds)
    {
        int hours = totalSeconds / 3_600,
                minutes = (totalSeconds % 3_600) / 60,
                seconds = totalSeconds % 60;

        return String.format("%d%s %d%s %d%s",
                hours, getString(R.string.menu_hours),
                minutes, getString(R.string.menu_minutes),
                seconds, getString(R.string.menu_seconds));
    }

    private boolean updateSpecsUI(Route route)
    {
        TextView
                tv_route_name = findViewById(R.id.tv_route_name_value),
                tv_route_start = findViewById(R.id.tv_route_start_value),
                tv_route_end = findViewById(R.id.tv_route_end_value),
                tv_route_points = findViewById(R.id.tv_route_points_value),
                tv_route_avoidpoints = findViewById(R.id.tv_route_avoidpoints_value),
                tv_route_avoidpointsarea = findViewById(R.id.tv_route_avoidpointsarea_value),
                tv_route_weight = findViewById(R.id.tv_route_weight_value),
                tv_route_length = findViewById(R.id.tv_route_length_value),
                tv_route_time = findViewById(R.id.tv_route_time_value),
                tv_route_drone = findViewById(R.id.tv_route_drone_value);

        if (route != null)
        {
            tv_route_name.setText(route.getName());
            tv_route_start.setText(String.format("%.3f, %.3f", route.getStart().getLongitude(), route.getStart().getLatitude()));
            tv_route_end.setText(String.format("%.3f, %.3f", route.getEnd().getLongitude(), route.getEnd().getLatitude()));
            tv_route_points.setText(String.valueOf(route.getNumberOfPoints()));
            tv_route_avoidpoints.setText(String.valueOf(route.getNumberOfAvoidancePoints()));
            tv_route_avoidpointsarea.setText(String.format("%.3f %s", route.getAreaOfAvoidancePoints()/1_000_000, getString(R.string.menu_square_km)));
            tv_route_weight.setText(String.format("%d %s", route.getWeight(), getString(R.string.menu_kg)));
            tv_route_length.setText(String.format("%.3f %s", route.getLength()/1_000, getString(R.string.menu_km)));
            tv_route_time.setText(getFormatedTime(route.getTimeToComplete()));
            tv_route_drone.setText(route.getDroneName());

            return true;
        }

        return false;
    }

    private boolean updateStatusUI(Route route)
    {
        ImageView iv_status = findViewById(R.id.iv_status_value);
        TextView tv_status_msg = findViewById(R.id.tv_route_status_value);

        if (route != null)
        {
            switch (route.getStatus())
            {
                case GOOD:
                    iv_status.setImageResource(R.drawable.ic_checkmark);
                    tv_status_msg.setText(R.string.status_msg_fine);
                    break;
                case ROUTE_TOO_LONG:
                    iv_status.setImageResource(R.drawable.ic_cancel);
                    tv_status_msg.setText(R.string.status_mgs_route_too_long);
                    break;
                case OVERWEIGHT:
                    iv_status.setImageResource(R.drawable.ic_cancel);
                    tv_status_msg.setText(R.string.status_msg_overweight);
                    break;
                case NO_DRONE:
                    iv_status.setImageResource(R.drawable.ic_warning);
                    tv_status_msg.setText(R.string.status_msg_no_drone);
                    break;
                default:
                    break;
            }

            return true;
        }

        return false;
    }

    public void showDeleteConfirmationDialog() {
        new AlertDialog.Builder(this)
                .setTitle(route.getName())
                .setMessage(getString(R.string.alert_msg_delete_route))
                .setPositiveButton(R.string.alert_answer_positive, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        deleteRoute();
                    }
                })
                .setNegativeButton(R.string.alert_answer_negative, null)
                .setIcon(R.drawable.ic_delete)
                .show();
    }

    private void deleteRoute()
    {
        Intent returnIntent = new Intent();
        returnIntent.putExtra("routeDeleted", true);
        returnIntent.putExtra("routePosition", routePosition);
        setResult(Activity.RESULT_OK, returnIntent);

        finish();
    }
}