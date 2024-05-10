package com.example.dronepathfinder.ui.settings;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.dronepathfinder.R;
import com.example.dronepathfinder.databinding.FragmentSettingsBinding;

public class SettingsFragment extends Fragment
{
    private FragmentSettingsBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState)
    {
        SettingsViewModel routesViewModel =
                new ViewModelProvider(this).get(SettingsViewModel.class);

        binding = FragmentSettingsBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        RadioGroup radioGroupAlgorithms = binding.radioGroupAlgorithms;

        radioGroupAlgorithms.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId)
            {
                String algorithmChoice;
                if (checkedId == R.id.radioButtonAStar)
                {
                    algorithmChoice = "A*";
                }
                else if (checkedId == R.id.radioButtonDijkstra)
                {
                    algorithmChoice = "Dijkstra";
                }
                else
                {
                    algorithmChoice = "Default";
                }

                SharedPreferences preferences = getActivity().getSharedPreferences("SettingsPrefs", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = preferences.edit();
                editor.putString("selectedAlgorithm", algorithmChoice);
                editor.apply();
            }
        });

        return root;
    }

    @Override
    public void onDestroyView()
    {
        super.onDestroyView();
        binding = null;
    }
}