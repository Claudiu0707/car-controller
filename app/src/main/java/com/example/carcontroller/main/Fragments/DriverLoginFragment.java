package com.example.carcontroller.main.Fragments;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.carcontroller.R;
import com.example.carcontroller.main.SessionManager;
import com.example.carcontroller.api.DriverRepository;
import com.example.carcontroller.api.models.DriverResponse;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;

import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class DriverLoginFragment extends Fragment {
    private static final String TAG = "DriverLoginFragment";
    private static final String DATE_FORMAT = "yyyy-MM-dd";
    private static final String DISPLAY_DATE_FORMAT = "dd:MM:yyyy";

    private final SessionManager sessionManager = SessionManager.getInstance();
    private DriverRepository driverRepository;


    MaterialAutoCompleteTextView dropdownGenderOptions;
    private DatePickerDialog datePickerDialog;
    private TextView firstNameView, lastNameView, ageView;
    private SessionManager.Gender selectedGender;
    private Button dateButton, backButton, createProfileButton;

    private String selectedBirthdate;

    @Nullable
    @Override
    public View onCreateView (@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_driver_login, container, false);

        driverRepository = DriverRepository.getInstance();

        initializeViews(view);
        initializeDatePicker();
        setupListeners();

        return view;
    }

    private void initializeViews (View view) {
        firstNameView = view.findViewById(R.id.firstNameInputID);
        lastNameView = view.findViewById(R.id.lastNameInputID);
        ageView = view.findViewById(R.id.ageInputID);
        dateButton = view.findViewById(R.id.datePickupButtonID);

        dropdownGenderOptions = view.findViewById(R.id.inputGenderID);

        backButton = view.findViewById(R.id.backButtonID);
        createProfileButton = view.findViewById(R.id.createProfileButtonID);

        dateButton.setText("Birthday");   // TODO: R.string.birthdate_label


    }

    private void setupListeners () {
        dateButton.setOnClickListener(v -> {
            if (datePickerDialog != null) {
                datePickerDialog.show();
            }
        });
        backButton.setOnClickListener(v -> close());
        createProfileButton.setOnClickListener(v -> createProfile());
        dropdownGenderOptions.setOnItemClickListener((parent, v, position, id) -> {
            SessionManager.Gender[] genders = SessionManager.Gender.values();
            selectedGender = genders[position];
        });
    }

    private void initializeDatePicker () {
        Calendar calendar = Calendar.getInstance();
        int currentYear = calendar.get(Calendar.YEAR);
        int currentMonth = calendar.get(Calendar.MONTH);
        int currentDay = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog.OnDateSetListener dateSetListener = (datePicker, year, month, day) -> {
            String displayDate = formatDisplayDate(day, month + 1, year);
            selectedBirthdate = formatDate(day, month + 1, year);
            dateButton.setText(displayDate);
        };

        datePickerDialog = new DatePickerDialog(requireContext(), AlertDialog.THEME_HOLO_DARK, dateSetListener, currentYear, currentMonth, currentDay);

        // Set max date to today
        datePickerDialog.getDatePicker().setMaxDate(calendar.getTimeInMillis());
    }

    private Date createDate (int year, int month, int day) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(year, month, day, 0, 0, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTime();
    }

    private String formatDisplayDate (int day, int month, int year) {
        return String.format(Locale.getDefault(), "%02d:%02d:%d", day, month, year);
    }

    private String formatDate (int day, int month, int year) {
        return String.format(Locale.getDefault(), "%d-%02d-%02d", year, month, day);
    }

    private void createProfile () {
        if (!validateInput()) {
            return;
        }

        String firstName = firstNameView.getText().toString().trim();
        String lastName = lastNameView.getText().toString().trim();
        int age = Integer.parseInt(ageView.getText().toString().trim());

        SessionManager.Driver driver = new SessionManager.Driver(firstName, lastName, age, selectedGender, selectedBirthdate);

        sessionManager.setCurrentDriver(driver);

        driverRepository.saveDriver(driver, new DriverRepository.DriverCallback() {
            @Override
            public void onSuccess (DriverResponse driver) {
                Toast.makeText(requireContext(), "Profile saved", Toast.LENGTH_LONG).show();
            }

            @Override
            public void onError (String error) {
                Toast.makeText(requireContext(), "Error: " + error, Toast.LENGTH_LONG).show();
            }
        });
    }

    private boolean validateInput () {
        String firstName = firstNameView.getText().toString().trim();
        String lastName = lastNameView.getText().toString().trim();
        String ageString = ageView.getText().toString().trim();

        if (TextUtils.isEmpty(firstName)) {
            showError("Please enter first name");
            return false;
        }

        if (TextUtils.isEmpty(lastName)) {
            showError("Please enter last name");
            return false;
        }

        if (TextUtils.isEmpty(ageString)) {
            showError("Please enter age");
            return false;
        }

        try {
            int age = Integer.parseInt(ageString);
            if (age < 0 || age > 150) {
                showError("Please enter a valid age");
                return false;
            }
        } catch (NumberFormatException e) {
            showError("Please enter a valid age");
            Log.e(TAG, "Invalid age format", e);
            return false;
        }

        if (selectedBirthdate == null) {
            showError("Please select birthdate");
            return false;
        }

        if (selectedGender == null) {
            showError("Please select gender");
            return false;
        }

        return true;
    }

    private void showError (String message) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
    }

    private void close () {
        requireActivity().getSupportFragmentManager().popBackStack();
    }
}