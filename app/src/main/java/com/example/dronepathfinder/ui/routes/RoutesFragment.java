package com.example.dronepathfinder.ui.routes;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.dronepathfinder.MapActivity;
import com.example.dronepathfinder.R;
import com.example.dronepathfinder.Route;
import com.example.dronepathfinder.databinding.FragmentRoutesBinding;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.osmdroid.util.GeoPoint;

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
                            "Назва " + route.getName() +
                            "\nПочаток: " + route.getStart().toString() +
                            "\nКінець: " + route.getEnd().toString() +
                            "\nДовжина: " + route.getLength() + " км";
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