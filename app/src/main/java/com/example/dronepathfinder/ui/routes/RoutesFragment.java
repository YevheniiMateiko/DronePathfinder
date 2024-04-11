package com.example.dronepathfinder.ui.routes;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.dronepathfinder.MapActivity;
import com.example.dronepathfinder.R;
import com.example.dronepathfinder.databinding.FragmentRoutesBinding;

public class RoutesFragment extends Fragment {

    private FragmentRoutesBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        RoutesViewModel routesViewModel = new ViewModelProvider(this).get(RoutesViewModel.class);

        binding = FragmentRoutesBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        final TextView textView = binding.textRoutes;
        String routesDesc = getString(R.string.frgt_routes_desc);
        textView.setText(routesDesc);
        //routesViewModel.getText().observe(getViewLifecycleOwner(), textView::setText);

        Button btnNavigateToActivity = root.findViewById(R.id.add_route);
        btnNavigateToActivity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Create an Intent to navigate to your target activity
                Intent intent = new Intent(getActivity(), MapActivity.class);
                startActivity(intent);
            }
        });

        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}