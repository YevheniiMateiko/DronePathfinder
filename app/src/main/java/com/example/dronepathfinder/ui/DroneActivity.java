package com.example.dronepathfinder.ui;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.example.dronepathfinder.R;
import com.example.dronepathfinder.objects.Drone;
import com.example.dronepathfinder.objects.Route;

public class DroneActivity extends AppCompatActivity
{
    private static final int DRONE_CREATION_MODE = 0;
    private static final int DRONE_ABOUT_MODE = 1;
    private int mode;
    private Drone drone;
    private int dronePosition;
    private Button deleteButton;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        setContentView(R.layout.activity_drone);

        mode = (int) getIntent().getIntExtra("mode", 0);
        deleteButton = findViewById(R.id.delete_drone);

        if (mode == DRONE_ABOUT_MODE)
            aboutModeBehavior();
        else
            creationModeBehavior();

    }

    private void aboutModeBehavior()
    {
        drone = (Drone) getIntent().getSerializableExtra("drone");
        dronePosition = (int) getIntent().getIntExtra("dronePosition", -1);
        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDeleteConfirmationDialog();
            }
        });

        updateSpecsUI(drone);
    }

    private void creationModeBehavior()
    {
        deleteButton.setVisibility(View.GONE);
    }

    private boolean updateSpecsUI(Drone drone)
    {
        TextView
                tv_drone_name = findViewById(R.id.tv_drone_name_value),
                tv_drone_flightdistance = findViewById(R.id.tv_drone_flightdistance_value),
                tv_drone_speed = findViewById(R.id.tv_drone_speed_value),
                tv_drone_payload = findViewById(R.id.tv_drone_payload_value);

        if (drone != null)
        {
            tv_drone_name.setText(drone.getName());
            tv_drone_flightdistance.setText(String.format("%.3f %s", drone.getFlightDistance()/1_000, getString(R.string.menu_km)));
            tv_drone_speed.setText(String.format("%.1f, %s", drone.getSpeed(), getString(R.string.menu_mps)));
            tv_drone_payload.setText(String.format("%.d, %s", drone.getPayload(), getString(R.string.menu_kg)));

            return true;
        }

        return false;
    }

    public void showDeleteConfirmationDialog()
    {
        new AlertDialog.Builder(this)
                .setTitle(drone.getName())
                .setMessage(getString(R.string.alert_msg_delete_drone))
                .setPositiveButton(R.string.alert_answer_positive, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        deleteDrone();
                    }
                })
                .setNegativeButton(R.string.alert_answer_negative, null)
                .setIcon(R.drawable.ic_delete)
                .show();
    }

    private void deleteDrone()
    {
        Intent returnIntent = new Intent();
        returnIntent.putExtra("mode", DRONE_ABOUT_MODE);
        returnIntent.putExtra("droneDeleted", true);
        returnIntent.putExtra("dronePosition", dronePosition);
        setResult(Activity.RESULT_OK, returnIntent);

        finish();
    }
}