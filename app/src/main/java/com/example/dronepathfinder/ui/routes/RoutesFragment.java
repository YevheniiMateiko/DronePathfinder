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
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.dronepathfinder.MapActivity;
import com.example.dronepathfinder.R;
import com.example.dronepathfinder.Route;
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
    public void onCreate(Bundle savedInstanceState) {
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
        adapter = new ArrayAdapter<Route>(getActivity(), android.R.layout.simple_list_item_1, routeList) {
            @NonNull
            @Override
            public View getView(int position, View convertView, @NonNull ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                TextView textView = (TextView) view.findViewById(android.R.id.text1);

                Route route = getItem(position);
                if (route != null) {
                    String routeInfo =
                                    getString(R.string.menu_listview_name)
                                    + " '" + route.getName() + "'"
                                    + "\n" + getString(R.string.menu_listview_start)
                                    + " " + String.format("%.4f", route.getStart().first)
                                    + " " + String.format("%.4f", route.getStart().second)
                                    + "\n" + getString(R.string.menu_listview_end)
                                    + " " + String.format("%.4f", route.getEnd().first)
                                    + " " + String.format("%.4f", route.getEnd().second)
                                    + "\n" + getString(R.string.menu_listview_length)
                                    + " " + String.format("%.3f",route.getLength()/1_000)
                                    + " " + getString(R.string.menu_listview_km);
                    textView.setText(routeInfo);
                }

                return view;
            }
        };
        binding.listViewRoutes.setAdapter(adapter);

        ActivityResultLauncher<Intent> mapActivityResultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        Route route = (Route) result.getData().getSerializableExtra("route");
                        updateRouteList(route);
                        updateRouteDesc();
                    }
                }
        );

        binding.listViewRoutes.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                Route routeToRemove = adapter.getItem(position);
                if (routeToRemove != null) {
                    showDeleteConfirmationDialog(routeToRemove, position);
                }
                return true;
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

    public void updateRouteList(Route newRoute)
    {
        if (routeList != null)
        {
            routeList.add(newRoute);
            adapter.notifyDataSetChanged();
        }
    }

    public void updateRouteDesc()
    {
        if (!routeList.isEmpty())
        {
            textView.setVisibility(View.GONE);
        }
        else
        {
            textView.setVisibility(View.VISIBLE);
        }
    }

    private void showDeleteConfirmationDialog(Route route, int position) {
        new AlertDialog.Builder(getContext())
                .setTitle(getString(R.string.alert_delete_route_title) + route.getName() + "?")
                .setMessage(getString(R.string.alert_delete_route_msg))
                .setPositiveButton(R.string.menu_pos_answer, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        routeList.remove(position);
                        adapter.notifyDataSetChanged();
                        saveRoutesToSharedPreferences();
                    }
                })
                .setNegativeButton(R.string.menu_neg_answer, null)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
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

        if (routeList == null) {
            routeList = new ArrayList<>();
        }

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