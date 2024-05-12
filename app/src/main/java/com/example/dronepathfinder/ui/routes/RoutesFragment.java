package com.example.dronepathfinder.ui.routes;

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
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.dronepathfinder.ui.AboutRouteActivity;
import com.example.dronepathfinder.ui.MapActivity;
import com.example.dronepathfinder.R;
import com.example.dronepathfinder.objects.Route;
import com.example.dronepathfinder.databinding.FragmentRoutesBinding;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class RoutesFragment extends Fragment
{
    private ActivityResultLauncher<Intent> aboutRouteActivityLauncher;
    private ActivityResultLauncher<Intent> mapActivityLauncher;
    private View root;
    private FragmentRoutesBinding binding;
    private ArrayAdapter<Route> adapter;
    private List<Route> routeList;
    private TextView textView;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        loadRoutesFromSharedPreferences();
    }

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState)
    {
        //RoutesViewModel routesViewModel = new ViewModelProvider(this).get(RoutesViewModel.class);

        binding = FragmentRoutesBinding.inflate(inflater, container, false);
        root = binding.getRoot();

        textView = binding.textRoutes;
        updateRoutesDesc();

        handleActivityResults();
        handleListView();

        Button btnNavigateToMapActivity = root.findViewById(R.id.add_route);
        btnNavigateToMapActivity.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                launchMapActivity();
            }
        });

        textView.setText(getString(R.string.frgt_routes_desc));

        return root;
    }

    private void launchAboutRouteActivity(Route route, int position)
    {
        Intent intent = new Intent(getActivity(), AboutRouteActivity.class);
        intent.putExtra("route", route);
        intent.putExtra("routePosition", position);
        aboutRouteActivityLauncher.launch(intent);
    }

    private void launchMapActivity()
    {
        Intent intent = new Intent(getActivity(), MapActivity.class);
        mapActivityLauncher.launch(intent);
    }

    private void handleActivityResults()
    {
        aboutRouteActivityLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        boolean routeDeleted = result.getData().getBooleanExtra("routeDeleted", false);
                        int routePosition = result.getData().getIntExtra("routePosition", -1);

                        if (routeDeleted && routePosition != -1)
                        {
                            routeList.remove(routePosition);
                            adapter.notifyDataSetChanged();
                            saveRoutesToSharedPreferences();
                        }
                    }
                }
        );

        mapActivityLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null)
                    {
                        Route route = (Route) result.getData().getSerializableExtra("route");
                        updateRouteList(route);
                        updateRoutesDesc();
                    }
                }
        );
    }

    private void handleListView()
    {
        adapter = new ArrayAdapter<Route>(getActivity(), R.layout.route_list_item, routeList)
        {
            @NonNull
            @Override
            public View getView(int position, View convertView, @NonNull ViewGroup parent)
            {
                if (convertView == null)
                    convertView = LayoutInflater.from(getContext()).inflate(R.layout.route_list_item, parent, false);

                TextView
                        lv_item_name = convertView.findViewById(R.id.listview_route_name_value),
                        lv_item_drone = convertView.findViewById(R.id.listview_route_drone_name_value),
                        lv_item_start = convertView.findViewById(R.id.listview_route_start_value),
                        lv_item_end = convertView.findViewById(R.id.listview_route_end_value),
                        lv_item_length = convertView.findViewById(R.id.listview_route_length_value),
                        lv_item_time = convertView.findViewById(R.id.listview_route_time_value);
                ImageView lv_item_status = convertView.findViewById(R.id.listview_route_status_value);

                Route route = getItem(position);
                if (route != null)
                {
                    lv_item_name.setText(route.getName());
                    lv_item_drone.setText(route.getDroneName());
                    lv_item_start.setText(String.format("%.3f, %.3f", route.getStart().getLongitude(), route.getStart().getLatitude()));
                    lv_item_end.setText(String.format("%.3f, %.3f", route.getEnd().getLongitude(), route.getEnd().getLatitude()));
                    lv_item_length.setText(String.format("%.3f %s", route.getLength()/1_000, getString(R.string.menu_km)));
                    lv_item_time.setText(getFormatedTime(route.getTimeToComplete()));
                    switch (route.getStatus())
                    {
                        case GOOD:
                            lv_item_status.setImageResource(R.drawable.ic_checkmark);
                            break;
                        case ROUTE_TOO_LONG:
                        case OVERWEIGHT:
                            lv_item_status.setImageResource(R.drawable.ic_cancel);
                            break;
                        case NO_DRONE:
                            lv_item_status.setImageResource(R.drawable.ic_warning);
                            break;
                        default:
                            break;
                    }
                }

                return convertView;
            }
        };
        binding.lvRoutes.setAdapter(adapter);

        binding.lvRoutes.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id)
            {
                Route selectedRoute = adapter.getItem(position);
                if (selectedRoute != null)
                {
                    launchAboutRouteActivity(selectedRoute, position);
                }
            }
        });

        /*binding.listViewRoutes.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener()
        {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id)
            {
            }
        });*/
    }

    private void updateRouteList(Route newRoute)
    {
        if (routeList != null)
        {
            routeList.add(newRoute);
            adapter.notifyDataSetChanged();
        }
    }

    private void updateRoutesDesc()
    {
        if (!routeList.isEmpty())
            textView.setVisibility(View.GONE);
        else
            textView.setVisibility(View.VISIBLE);
    }

    private void saveRoutesToSharedPreferences()
    {
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("RoutesPref", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        Gson gson = new Gson();
        String jsonRoutes = gson.toJson(routeList);
        editor.putString("SavedRoutes", jsonRoutes);
        editor.apply();
    }

    private void loadRoutesFromSharedPreferences()
    {
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("RoutesPref", Context.MODE_PRIVATE);
        Gson gson = new Gson();
        String jsonRoutes = sharedPreferences.getString("SavedRoutes", null);
        Type type = new TypeToken<ArrayList<Route>>() {}.getType();
        routeList = gson.fromJson(jsonRoutes, type);

        if (routeList == null)
            routeList = new ArrayList<>();
    }

    private String getFormatedTime(int totalSeconds)
    {
        int hours = totalSeconds / 3_600;
        int minutes = (totalSeconds % 3_600) / 60;
        int seconds = totalSeconds % 60;

        return String.format("%d%s %d%s %d%s",
                hours, getString(R.string.menu_hours),
                minutes, getString(R.string.menu_minutes),
                seconds, getString(R.string.menu_seconds));
    }

    @Override
    public void onPause()
    {
        super.onPause();
        saveRoutesToSharedPreferences();
    }

    @Override
    public void onDestroyView()
    {
        super.onDestroyView();
        binding = null;
    }
}