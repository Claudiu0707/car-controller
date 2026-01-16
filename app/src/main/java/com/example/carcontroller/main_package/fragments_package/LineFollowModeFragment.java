package com.example.carcontroller.main_package.fragments_package;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.example.carcontroller.R;
import com.example.carcontroller.devices_package.DeviceManager;

public class LineFollowModeFragment extends Fragment {
    private static final String TAG = "LineFollowModeFragmentTAG";

    private DeviceManager deviceManager = DeviceManager.getInstance();
    private Button backButton, startFollowLineButton, stopFollowLineButton;
    private View view;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_line_follow_mode, container, false);
        initializeViews();
        setOnClickListeners();
        return view;
    }

    private void initializeViews() {
        backButton = (Button) view.findViewById(R.id.backButtonID);
        startFollowLineButton = (Button) view.findViewById(R.id.startFollowLineButtonID);
        stopFollowLineButton = (Button) view.findViewById(R.id.stopFollowLineButtonID);
    }

    private void setOnClickListeners () {
        backButton.setOnClickListener(v -> {
            close();
        });
        startFollowLineButton.setOnClickListener(v -> {
            deviceManager.getCarDevice().setLineFollowing(true);
        });
        stopFollowLineButton.setOnClickListener(v -> {
            deviceManager.getCarDevice().setLineFollowing(false);
        });
    }

    private void close () {
        requireActivity().getSupportFragmentManager().popBackStack();
    }


}