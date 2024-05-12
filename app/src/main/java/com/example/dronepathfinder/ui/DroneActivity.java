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
            if (et_drone_name.getText().toString().trim().isEmpty()
                    || et_drone_flightdistance.getText().toString().trim().isEmpty()
                    || et_drone_speed.getText().toString().trim().isEmpty()
                    || et_drone_payload.getText().toString().trim().isEmpty())
            {
                showEmptyFieldsAlert();
            }
            else
            {
                String
                        name = et_drone_name.getText().toString(),
                        str_flightdistance = et_drone_flightdistance.getText().toString().replace(',', '.'),
                        str_speed = et_drone_speed.getText().toString().replace(',', '.');
                double
                        flightDistance,
                        speed;
                int payload;

                try
                {
                    flightDistance = 1_000 * Double.parseDouble(str_flightdistance);
                    speed = Double.parseDouble(str_speed);
                    payload = Integer.parseInt(et_drone_payload.getText().toString());
                }
                catch (NumberFormatException e)
                {
                    name = "Smth went wrong";
                    flightDistance = 0.0;
                    speed = 0.0;
                    payload = 0;
                }

                Intent returnIntent = new Intent();

                if (mode == DRONE_ABOUT_MODE)
                {
                    drone.setName(name);
                    drone.setFlightDistance(flightDistance);
                    drone.setSpeed(speed);
                    drone.setPayload(payload);

                    returnIntent.putExtra("droneUpdated", true);
                    returnIntent.putExtra("dronePosition", dronePosition);
                }
                else
                {
                    drone = new Drone (name, flightDistance, speed, payload);
                    returnIntent.putExtra("droneCreated", true);
                }

                returnIntent.putExtra("drone", drone);
                setResult(Activity.RESULT_OK, returnIntent);
                finish();
            }

        });
    }

    private boolean updateSpecsUI(Drone drone)
    {
        if (drone != null)
        {
            et_drone_name.setText(drone.getName());
            et_drone_flightdistance.setText(String.format("%.3f", drone.getFlightDistance()/1_000));
            et_drone_speed.setText(String.format("%.1f", drone.getSpeed()));
            et_drone_payload.setText(String.format("%d", drone.getPayload()));

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
        returnIntent.putExtra("droneDeleted", true);
        returnIntent.putExtra("dronePosition", dronePosition);
        setResult(Activity.RESULT_OK, returnIntent);

        finish();
    }
}