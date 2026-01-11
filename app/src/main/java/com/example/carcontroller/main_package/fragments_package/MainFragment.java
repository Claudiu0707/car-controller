package com.example.carcontroller.main_package.fragments_package;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.example.carcontroller.main_package.activities_package.DriverModeActivity;
import com.example.carcontroller.R;

public class MainFragment extends Fragment {

    private static final String TAG = "MainFragmentTAG";

    Button settingsButton, driverButton, driverLoginButton;
    Context context;
    View view;

    @Override
    public View onCreateView (LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_main, container, false);
        context = requireContext();

        // Buttons initialization
        driverButton = (Button) view.findViewById(R.id.driverButtonID);
        settingsButton = (Button) view.findViewById(R.id.settingsButtonID);
        driverLoginButton = (Button) view.findViewById(R.id.driverLoginButtonID);

        // ---------------- BUTTON ONCLICK LISTENERS ----------------
        driverButton.setOnClickListener(v -> {
            launchActivityDriverMode();
        });
        driverLoginButton.setOnClickListener(v -> {
            open(new DriverLoginFragment());
        });
        settingsButton.setOnClickListener(v -> {
            launchActivitySettings();
        });

        return view;
    }

    private void launchActivityDriverMode () {
        Intent intent = new Intent(context, DriverModeActivity.class);
        startActivity(intent);
    }

    private void launchActivitySettings () {
        Intent intent = new Intent(context, SettingsActivity.class);
        startActivity(intent);
    }

    private void open (Fragment fragment) {
        requireActivity().getSupportFragmentManager().beginTransaction().replace(R.id.main_container_id, fragment).addToBackStack(null).commit();
    }
}