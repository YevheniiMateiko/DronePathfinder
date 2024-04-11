package com.example.dronepathfinder.ui.drones;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.dronepathfinder.R;
import com.example.dronepathfinder.databinding.FragmentDronesBinding;

public class DronesFragment extends Fragment
{

    private FragmentDronesBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState)
    {
        DronesViewModel dronesViewModel =
                new ViewModelProvider(this).get(DronesViewModel.class);

        binding = FragmentDronesBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        final TextView textView = binding.textDrones;

        String dronesDesc = getString(R.string.frgt_drones_desc);
        textView.setText(dronesDesc);
        return root;
    }

    @Override
    public void onDestroyView()
    {
        super.onDestroyView();
        binding = null;
    }
}