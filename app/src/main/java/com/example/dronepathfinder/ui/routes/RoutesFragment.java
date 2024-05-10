package com.example.dronepathfinder.ui.routes;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
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
        updateRouteDesc();
        adapter = new ArrayAdapter<Route>(getActivity(), R.layout.custom_list_item, routeList)
        {
            @NonNull
            @Override
            public View getView(int position, View convertView, @NonNull ViewGroup parent)
            {
                if (convertView == null)
                    convertView = LayoutInflater.from(getContext()).inflate(R.layout.custom_list_item, parent, false);

                TextView lv_item_name = convertView.findViewById(R.id.listview_route_name_value);
                TextView lv_item_drone = convertView.findViewById(R.id.listview_route_drone_name_value);
                TextView lv_item_start = convertView.findViewById(R.id.listview_route_start_value);
                TextView lv_item_end = convertView.findViewById(R.id.listview_route_end_value);
                TextView lv_item_length = convertView.findViewById(R.id.listview_route_length_value);
                TextView lv_item_time = convertView.findViewById(R.id.listview_route_time_value);
                ImageView lv_item_status = convertView.findViewById(R.id.listview_route_status_value);

                Route route = getItem(position);
                if (route != null)
                {
                    lv_item_name.setText(route.getName());
                    lv_item_drone.setText(route.getDroneName());
                    lv_item_start.setText(String.format("%.3f, %.3f", route.getStart().first, route.getStart().second));
                    lv_item_end.setText(String.format("%.3f, %.3f", route.getEnd().first, route.getEnd().second));
                    lv_item_length.setText(String.format("%.3f %s", route.getLength()/1_000, getString(R.string.menu_route_km)));
                    lv_item_time.setText(getFormatedTime(route.getTimeToComplete()));
                    switch (route.getStatus()) {
                        case GOOD:
                            lv_item_status.setImageResource(R.drawable.ic_checkmark);
                            break;
                        case WARNING:
                            lv_item_status.setImageResource(R.drawable.ic_warning);
                            break;
                        default:
                            lv_item_status.setImageResource(R.drawable.ic_warning);
                            break;
                    }
                }

                return convertView;
            }
        };
        binding.listViewRoutes.setAdapter(adapter);

        ActivityResultLauncher<Intent> mapActivityResultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null)
                    {
                        Route route = (Route) result.getData().getSerializableExtra("route");
                        updateRouteList(route);
                        updateRouteDesc();
                    }
                }
        );

        /*binding.listViewRoutes.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener()
        {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id)
            {
                Route routeToRemove = adapter.getItem(position);

                if (routeToRemove != null)
                    showDeleteConfirmationDialog(routeToRemove, position);

                return true;
            }
        });*/

        binding.listViewRoutes.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id)
            {
                Route selectedRoute = adapter.getItem(position);
                if (selectedRoute != null) {

                }
            }
        });

        Button btnNavigateToMapActivity = root.findViewById(R.id.add_route);
        btnNavigateToMapActivity.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Intent intent = new Intent(getActivity(), MapActivity.class);
                mapActivityResultLauncher.launch(intent);
            }
        });

        textView.setText(getString(R.string.frgt_routes_desc));

        return root;
    }

    private void updateRouteList(Route newRoute)
    {
        if (routeList != null)
        {
            routeList.add(newRoute);
            adapter.notifyDataSetChanged();
        }
    }

    private void updateRouteDesc()
    {
        if (!routeList.isEmpty())
            textView.setVisibility(View.GONE);
        else
            textView.setVisibility(View.VISIBLE);
    }

    private void showDeleteConfirmationDialog(Route route, int position)
    {
        new AlertDialog.Builder(getContext())
                .setTitle(getString(R.string.alert_title_delete_route))
                .setMessage(getString(R.string.alert_msg_delete_route) + " '" + route.getName() + "'?")
                .setPositiveButton(R.string.alert_answer_positive, new DialogInterface.OnClickListener()
                {
                    public void onClick(DialogInterface dialog, int which)
                    {
                        routeList.remove(position);
                        adapter.notifyDataSetChanged();
                        saveRoutesToSharedPreferences();
                    }
                })
                .setNegativeButton(R.string.alert_answer_negative, null)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

    private void saveRoutesToSharedPreferences() {
        // Отримання SharedPreferences з назвою "RoutesPref" у приватному режимі
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("RoutesPref", Context.MODE_PRIVATE);
        // Отримання редактора для SharedPreferences
        SharedPreferences.Editor editor = sharedPreferences.edit();
        // Створення нового об'єкта Gson для серіалізації
        Gson gson = new Gson();
        // Серіалізація списку маршрутів у JSON рядок
        String jsonRoutes = gson.toJson(routeList);
        // Збереження серіалізованого JSON рядка у SharedPreferences під ключем "SavedRoutes"
        editor.putString("SavedRoutes", jsonRoutes);
        // Застосування змін до SharedPreferences
        editor.apply();
    }

    // Метод для завантаження списку маршрутів з SharedPreferences
    private void loadRoutesFromSharedPreferences() {
        // Отримання SharedPreferences, де зберігаються маршрути
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("RoutesPref", Context.MODE_PRIVATE);
        // Створення нового об'єкта Gson для десеріалізації
        Gson gson = new Gson();
        // Отримання збереженого JSON рядка з маршрутами
        String jsonRoutes = sharedPreferences.getString("SavedRoutes", null);
        // Визначення типу для десеріалізації списку маршрутів
        Type type = new TypeToken<ArrayList<Route>>() {}.getType();
        // Десеріалізація JSON рядка у список маршрутів
        routeList = gson.fromJson(jsonRoutes, type);

        // Якщо список маршрутів не існує, створення нового порожнього списку
        if (routeList == null)
            routeList = new ArrayList<>();
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