package com.example.carcontroller.main_package.fragments_package;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.example.carcontroller.R;
import com.example.carcontroller.devices_package.CarDevice;
import com.example.carcontroller.devices_package.DeviceManager;
import com.example.carcontroller.main_package.SessionManager;

/**
 * Main entry fragment of the application.
 * <p>
 * Provides navigation to:
 * <ul>
 *     <li>Drive / Line Follower mode</li>
 *     <li>Driver login</li>
 *     <li>Application settings</li>
 * </ul>
 * Performs validation before allowing navigation to driving modes.
 */
public class MainFragment extends Fragment {
    // private static final String TAG = "MainFragment";

    // ============ UI elements ============
    private Button settingsButton;
    private Button modeFragmentButton;
    private Button driverLoginButton;

    // ============ Context & root view ============
    private Context context;
    private View rootView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_main, container, false);
        context = requireContext();

        initializeViews();
        initializeListeners();

        return rootView;
    }

    /**
     * Binds UI components to their respective fields.
     */
    private void initializeViews() {
        modeFragmentButton = rootView.findViewById(R.id.modeFragmentButtonID);
        settingsButton = rootView.findViewById(R.id.settingsButtonID);
        driverLoginButton = rootView.findViewById(R.id.driverLoginButtonID);
    }

    /**
     * Registers click listeners for all main navigation buttons.
     */
    private void initializeListeners() {
        modeFragmentButton.setOnClickListener(v -> handleModeNavigation());

        driverLoginButton.setOnClickListener(v -> open(new DriverLoginFragment()));
        settingsButton.setOnClickListener(v -> open(new SettingsFragment()));
    }

    /**
     * Handles navigation to the appropriate operation mode fragment. Performs required checks.
     */
    private void handleModeNavigation() {
        DeviceManager deviceManager = DeviceManager.getInstance();
        SessionManager sessionManager = SessionManager.getInstance();

        if (!deviceManager.hasCarDevice()) {
            Toast.makeText(context, "Please connect a device!", Toast.LENGTH_LONG).show();
            return;
        }

        if (!sessionManager.isDriverLogged()) {
            Toast.makeText(context, "Please log in a driver!", Toast.LENGTH_LONG).show();
            return;
        }

        CarDevice carDevice = deviceManager.getCarDevice();

        if (carDevice.getCurrentMode() == CarDevice.OperationMode.DRIVE) {
            open(new DriveModeFragment());
        } else if (carDevice.getCurrentMode() == CarDevice.OperationMode.LINE_FOLLOWER) {
            open(new LineFollowModeFragment());
        }
    }

    private void open(Fragment fragment) {
        requireActivity().getSupportFragmentManager().beginTransaction().replace(R.id.main_container_id, fragment).addToBackStack(null).commit();
    }
}
