package com.example.smartpantrymanager;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;

public class SettingsActivity extends AppCompatActivity {

    private SwitchCompat switchShowExpiry;
    private RadioGroup radioGroupSort;
    private RadioButton radioAlphabetical;
    private RadioButton radioExpiry;

    private SharedPreferences sharedPreferences;

    // Name of the SharedPreferences file
    public static final String PREFS_NAME = "SmartPantrySettings";

    // Keys used to save individual settings
    public static final String KEY_SHOW_EXPIRY = "show_expiry";
    public static final String KEY_SORT_OPTION = "sort_option";

    // Sorting values
    public static final String SORT_ALPHABETICAL = "alphabetical";
    public static final String SORT_EXPIRY = "expiry";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Connect Activity to Settings layout
        setContentView(R.layout.activity_settings);

        // Connect Java variables to XML views
        switchShowExpiry = findViewById(R.id.switchShowExpiry);
        radioGroupSort = findViewById(R.id.radioGroupSort);
        radioAlphabetical = findViewById(R.id.radioAlphabetical);
        radioExpiry = findViewById(R.id.radioExpiry);

        // SharedPreferences stores small settings
        // locally on Android device
        sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

        // Load previously saved settings
        loadSettings();

        // Save expiry-date preference when changed
        switchShowExpiry.setOnCheckedChangeListener((buttonView, isChecked) -> {

            sharedPreferences.edit().putBoolean(KEY_SHOW_EXPIRY, isChecked).apply();
        });

        // Save pantry sorting preference when changed
        radioGroupSort.setOnCheckedChangeListener((group, checkedId) -> {

            String selectedSort = SORT_ALPHABETICAL;

            if (checkedId == R.id.radioExpiry) {
                selectedSort = SORT_EXPIRY;
            }

            sharedPreferences.edit().putString(KEY_SORT_OPTION, selectedSort).apply();
        });
    }

    // Loads the users previously saved preferences whenever
    // the Settings screen is opened
    private void loadSettings() {

        // Expiry dates are shown by default
        boolean showExpiry = sharedPreferences.getBoolean(KEY_SHOW_EXPIRY, true);

        switchShowExpiry.setChecked(showExpiry);

        // Alphabetical sorting will be the default
        String sortOption = sharedPreferences.getString(KEY_SORT_OPTION, SORT_ALPHABETICAL);

        if (SORT_EXPIRY.equals(sortOption)) {
            radioExpiry.setChecked(true);
        } else {
            radioAlphabetical.setChecked(true);
        }
    }
}
