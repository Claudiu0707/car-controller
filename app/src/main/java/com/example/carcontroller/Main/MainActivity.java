/**
 * @Author Muresan Claudiu
 *
 * */


package com.example.carcontroller.Main;

import android.content.Intent;
import android.os.Bundle;
import android.util.*;
import android.view.*;
import android.widget.*;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.carcontroller.R;
import com.example.carcontroller.Settings.SettingsActivity;

public class MainActivity extends AppCompatActivity{
    private static final String TAG = "MainActivityTAG";
    Button settingsButton, driverButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });


        // Buttons initialization
        driverButton = (Button) findViewById(R.id.driverButtonID);
        settingsButton = (Button) findViewById(R.id.settingsButtonID);

        // ---------------- BUTTON ONCLICK LISTENERS ----------------
        driverButton.setOnClickListener(v -> {
            Log.d(TAG, "onClick: driverMode");
            launchActivityDriverMode();
        });
        settingsButton.setOnClickListener(v -> {
            Log.d(TAG, "onClick: settings");
            launchActivitySettings();
        });
    }

    public void handleDriverNameText (View v) {
        String driverName = ((EditText) findViewById(R.id.driverTextBoxID)).getText().toString();
        ((TextView) findViewById(R.id.inputName)).setText(driverName);
        Toast.makeText(this, "Driver introduced", Toast.LENGTH_LONG).show();
        Log.d("Driver name", driverName);
    }

    private void launchActivityDriverMode () {
        Intent intent = new Intent(this, DriverModeActivity.class);
        startActivity(intent);
    }

    private void launchActivitySettings () {
        Intent intent = new Intent(this, SettingsActivity.class);
        startActivity(intent);
    }
}
