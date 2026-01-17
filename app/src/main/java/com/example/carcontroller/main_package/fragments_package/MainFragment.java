package com.example.carcontroller.main_package.fragments_package;

import android.content.Context;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.example.carcontroller.R;
import com.example.carcontroller.devices_package.CarDevice;
import com.example.carcontroller.devices_package.DeviceManager;
import com.example.carcontroller.main_package.SessionManager;

public class MainFragment extends Fragment {

    private static final String TAG = "MainFragmentTAG";

    Button settingsButton, modeFragmentButton, driverLoginButton;
    Context context;
    View view;

    @Override
    public View onCreateView (LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_main, container, false);
        context = requireContext();

        // Buttons initialization
        modeFragmentButton = (Button) view.findViewById(R.id.modeFragmentButtonID);
        settingsButton = (Button) view.findViewById(R.id.settingsButtonID);
        driverLoginButton = (Button) view.findViewById(R.id.driverLoginButtonID);

        // ---------------- BUTTON ONCLICK LISTENERS ----------------
        modeFragmentButton.setOnClickListener(v -> {
            if (!DeviceManager.getInstance().hasCarDevice()) {
                Toast.makeText(requireActivity(), "Please connect a device!", Toast.LENGTH_LONG).show();
            }
            else if (!SessionManager.getInstance().isDriverLogged()) {
                Toast.makeText(requireActivity(), "Please log in a driver!", Toast.LENGTH_LONG).show();
            }
            else {
                if (DeviceManager.getInstance().getCarDevice().getCurrentMode() == CarDevice.OperationMode.DRIVE)
                    open(new DriveModeFragment());
                else if (DeviceManager.getInstance().getCarDevice().getCurrentMode() == CarDevice.OperationMode.LINE_FOLLOWER)
                    open(new LineFollowModeFragment());
            }
        });
        driverLoginButton.setOnClickListener(v -> {
            open(new DriverLoginFragment());
        });
        settingsButton.setOnClickListener(v -> {
            open(new SettingsFragment());
        });

        return view;
    }


    private void open (Fragment fragment) {
        requireActivity().getSupportFragmentManager().beginTransaction().replace(R.id.main_container_id, fragment).addToBackStack(null).commit();
    }
}