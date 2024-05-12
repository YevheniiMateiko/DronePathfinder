package com.example.dronepathfinder.ui;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.example.dronepathfinder.R;
import com.example.dronepathfinder.objects.Drone;

public class DroneActivity extends AppCompatActivity
{
    private static final int
            DRONE_CREATION_MODE = 0,
            DRONE_ABOUT_MODE = 1;
    private int mode;
    private Drone drone;
    private int dronePosition;
    private Button
            deleteButton,
            saveButton;
    private EditText
            et_drone_name,
            et_drone_flightdistance,
            et_drone_speed,
            et_drone_payload;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        setContentView(R.layout.activity_drone);

        mode = (int) getIntent().getIntExtra("mode", 0);

        deleteButton = findViewById(R.id.delete_drone);
        saveButton = findViewById(R.id.save_drone);

        handleSaveButtonBehavior();

        et_drone_name = findViewById(R.id.et_drone_name_value);
        et_drone_flightdistance = findViewById(R.id.et_drone_flightdistance_value);
        et_drone_speed = findViewById(R.id.et_drone_speed_value);
        et_drone_payload = findViewById(R.id.et_drone_payload_value);

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

    private void handleSaveButtonBehavior()
    {
        saveButton.setOnClickListener(view -> {
            String
                    str_drone_name = et_drone_name.getText().toString().trim(),
                    str_drone_flightdistance = et_drone_flightdistance.getText().toString().trim(),
                    str_drone_speed = et_drone_speed.getText().toString().trim(),
                    str_drone_payload = et_drone_payload.getText().toString().trim();

            if (str_drone_name.isEmpty()
                    || str_drone_flightdistance.isEmpty()
                    || str_drone_speed.isEmpty()
                    || str_drone_payload.isEmpty())
            {
                showEmptyFieldsAlert();
            }
            else
            {
                double flightDistance,
                        speed;
                int payload;

                try
                {
                    flightDistance = Double.parseDouble(et_drone_flightdistance.getText().toString());
                    speed = Double.parseDouble(et_drone_speed.getText().toString());
                    payload = Integer.parseInt(et_drone_payload.getText().toString());
                }
                catch (NumberFormatException e)
                {
                    flightDistance = 0.0;
                    speed = 0.0;
                    payload = 0;
                }

                if (mode == DRONE_ABOUT_MODE)
                {
                    drone.setName(str_drone_name);
                    drone.setFlightDistance(flightDistance);
                    drone.setSpeed(speed);
                    drone.setPayload(payload);

                    Intent returnIntent = new Intent();
                    returnIntent.putExtra("mode", DRONE_ABOUT_MODE);
                    setResult(Activity.RESULT_OK, returnIntent);
                    finish();
                }
                else
                {
                    drone = new Drone (
                            et_drone_name.getText().toString(),
                            flightDistance, speed, payload);

                    Intent returnIntent = new Intent();
                    returnIntent.putExtra("mode", DRONE_CREATION_MODE);
                    returnIntent.putExtra("drone", drone);
                    setResult(Activity.RESULT_OK, returnIntent);
                    finish();
                }
            }

        });
    }

    private boolean updateSpecsUI(Drone drone)
    {
        if (drone != null)
        {
            et_drone_name.setText(drone.getName());
            et_drone_flightdistance.setText(String.format("%.3f %s", drone.getFlightDistance()/1_000, getString(R.string.menu_km)));
            et_drone_speed.setText(String.format("%.1f, %s", drone.getSpeed(), getString(R.string.menu_mps)));
            et_drone_payload.setText(String.format("%.d, %s", drone.getPayload(), getString(R.string.menu_kg)));

            return true;
        }

        return false;
    }

    private void showDeleteConfirmationDialog()
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

    private void showEmptyFieldsAlert()
    {
        new AlertDialog.Builder(this)
                .setTitle(getString(R.string.alert_not_enough_data_label))
                .setMessage(getString(R.string.alert_msg_not_enough_data))
                .setPositiveButton(R.string.alert_answer_ok, null)
                .setNegativeButton(R.string.alert_answer_cancel, null)
                .setIcon(R.drawable.ic_warning)
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