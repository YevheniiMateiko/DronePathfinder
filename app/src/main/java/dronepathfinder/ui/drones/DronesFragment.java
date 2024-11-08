package dronepathfinder.ui.drones;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.dronepathfinder.R;
import com.example.dronepathfinder.databinding.FragmentDronesBinding;
import dronepathfinder.objects.Drone;
import dronepathfinder.ui.DroneActivity;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class DronesFragment extends Fragment
{
    private static final int DRONE_CREATION_MODE = 0;
    private static final int DRONE_ABOUT_MODE = 1;
    private ActivityResultLauncher<Intent> droneActivityLauncher;
    private View root;
    private FragmentDronesBinding binding;
    private ArrayAdapter<Drone> adapter;
    private List<Drone> dronesList;
    private TextView textView;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        loadDronesFromSharedPreferences();
    }

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState)
    {
        DronesViewModel dronesViewModel =
                new ViewModelProvider(this).get(DronesViewModel.class);

        binding = FragmentDronesBinding.inflate(inflater, container, false);
        root = binding.getRoot();

        textView = binding.textDrones;
        updateDronesDesc();

        handleActivityResults();
        handleListView();

        Button btnNavigateToMapActivity = root.findViewById(R.id.add_drone);
        btnNavigateToMapActivity.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                launchDroneActivity();
            }
        });

        textView.setText(getString(R.string.frgt_drones_desc));

        return root;
    }

    private void launchDroneActivity()
    {
        Intent intent = new Intent(getActivity(), DroneActivity.class);
        intent.putExtra("mode", DRONE_CREATION_MODE);
        droneActivityLauncher.launch(intent);
    }

    private void launchDroneActivity(Drone drone, int position)
    {
        Intent intent = new Intent(getActivity(), DroneActivity.class);
        intent.putExtra("mode", DRONE_ABOUT_MODE);
        intent.putExtra("drone", drone);
        intent.putExtra("dronePosition", position);
        droneActivityLauncher.launch(intent);
    }

    private void handleActivityResults()
    {
        droneActivityLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        boolean
                            droneDeleted = result.getData().getBooleanExtra("droneDeleted", false),
                            droneUpdated = result.getData().getBooleanExtra("droneUpdated", false),
                            droneCreated = result.getData().getBooleanExtra("droneCreated", false);
                        int dronePosition = result.getData().getIntExtra("dronePosition", -1);

                        if (droneDeleted && dronePosition != -1)
                            dronesList.remove(dronePosition);
                        if (droneUpdated && dronePosition != -1)
                        {
                            Drone updatedDrone = (Drone) result.getData().getSerializableExtra("drone");

                            dronesList.set(dronePosition, updatedDrone);
                        }
                        if (droneCreated)
                        {
                            Drone drone = (Drone) result.getData().getSerializableExtra("drone");
                            addDroneToList(drone);
                            updateDronesDesc();
                        }

                        adapter.notifyDataSetChanged();
                        saveDronesToSharedPreferences();
                    }
                }
        );
    }

    private void handleListView()
    {
        adapter = new ArrayAdapter<Drone>(getActivity(), R.layout.drone_list_item, dronesList)
        {
            @NonNull
            @Override
            public View getView(int position, View convertView, @NonNull ViewGroup parent)
            {
                if (convertView == null)
                    convertView = LayoutInflater.from(getContext()).inflate(R.layout.drone_list_item, parent, false);

                TextView
                        lv_item_name = convertView.findViewById(R.id.lv_drone_name_value),
                        lv_item_flightdistance = convertView.findViewById(R.id.lv_drone_flightdistance_value),
                        lv_item_speed = convertView.findViewById(R.id.lv_drone_speed_value),
                        lv_item_payload = convertView.findViewById(R.id.lv_drone_payload_value);

                Drone drone = getItem(position);
                if (drone != null)
                {
                    lv_item_name.setText(drone.getName());
                    lv_item_flightdistance.setText(String.format("%.3f %s", drone.getFlightDistance()/1_000, getString(R.string.menu_km)));
                    lv_item_speed.setText(String.format("%.1f %s", drone.getSpeed(), getString(R.string.menu_mps)));
                    lv_item_payload.setText(String.format("%d %s", drone.getPayload(), getString(R.string.menu_kg)));
                }

                return convertView;
            }
        };
        binding.lvDrones.setAdapter(adapter);

        binding.lvDrones.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id)
            {
                Drone selectedDrone = adapter.getItem(position);

                if (selectedDrone != null)
                    launchDroneActivity(selectedDrone, position);
            }
        });

        /*binding.lvDrones.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener()
        {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id)
            {
            }
        });*/
    }

    private void addDroneToList(Drone newDrone)
    {
        if (dronesList != null)
        {
            dronesList.add(newDrone);
            adapter.notifyDataSetChanged();
        }
    }

    private void updateDronesDesc()
    {
        if (!dronesList.isEmpty())
            textView.setVisibility(View.GONE);
        else
            textView.setVisibility(View.VISIBLE);
    }

    private void saveDronesToSharedPreferences()
    {
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("DronesPref", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        Gson gson = new Gson();
        String jsonDrones = gson.toJson(dronesList);
        editor.putString("SavedDrones", jsonDrones);
        editor.apply();
    }

    private void loadDronesFromSharedPreferences()
    {
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("DronesPref", Context.MODE_PRIVATE);
        Gson gson = new Gson();
        String jsonDrones = sharedPreferences.getString("SavedDrones", null);
        Type type = new TypeToken<ArrayList<Drone>>() {}.getType();
        dronesList = gson.fromJson(jsonDrones, type);

        if (dronesList == null)
            dronesList = new ArrayList<>();
    }

    @Override
    public void onPause()
    {
        super.onPause();
        saveDronesToSharedPreferences();
    }

    @Override
    public void onDestroyView()
    {
        super.onDestroyView();
        binding = null;
    }
}