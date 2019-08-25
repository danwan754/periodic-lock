package com.danwan.periodiclock;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.ColorDrawable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
//import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private int maxMinutes = 1440;
    private int maxSeconds = 86400;
    private int maxHours = 24;

    // timeValue may be in seconds or minutes
    private int timeValue;
    private int timeValueSeconds;
    private String timeUnit;
    private SharedPreferences sharedPref;
    public static final int RESULT_ENABLE = 11;
    DevicePolicyManager devicePolicyManager;
    ActivityManager activityManager;
    ComponentName compName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sharedPref = this.getPreferences(Context.MODE_PRIVATE);
        timeValue = sharedPref.getInt(getString(R.string.time_value), 60);
        timeValueSeconds = sharedPref.getInt(getString(R.string.time_value_seconds), 60);
        timeUnit = sharedPref.getString(getString(R.string.time_unit), "Seconds");
//        Log.d("time", "timeValue: " + timeValue);
//        Log.d("time", "timeValueSeconds: " + timeValueSeconds);
//        Log.d("time", "timeUnit: " + timeUnit);

        compName = new ComponentName(this, MyAdmin.class);
        devicePolicyManager = (DevicePolicyManager) getSystemService(DEVICE_POLICY_SERVICE);
        activityManager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);

        final Switch enableSwitch = findViewById(R.id.enableSwitch);
        Button intervalButton = findViewById(R.id.intervalButton);

        intervalButton.setText(timeValue + " " + timeUnit);

        Intent intent = getIntent();
        if (intent.getBooleanExtra("fromService", false) || isMyServiceRunning(PeriodicLockService.class)) {
            enableSwitch.setChecked(true);
        }

        enableSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener(){
            public void onCheckedChanged (CompoundButton buttonView, boolean isChecked) {
                boolean active = devicePolicyManager.isAdminActive(compName);
                if (active && isChecked) {
                    startService();
                } else if (isChecked && !active){
                    getLockPermissions();
                    if (active) {
                        startService();
                    }
                    else {
                        enableSwitch.setChecked(false);
                    }
                }
                else {
                    stopService();
                }
            }
        });
    }

    public void startService() {
        Intent serviceIntent = new Intent(this, PeriodicLockService.class);
        serviceIntent.putExtra("mainCall", true);
        serviceIntent.putExtra("timeValue", timeValueSeconds);
        ContextCompat.startForegroundService(this, serviceIntent);
    }

    public void stopService() {
        Intent serviceIntent = new Intent(this, PeriodicLockService.class);
        stopService(serviceIntent);
    }

    public void showIntervalDialog(View view) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = LayoutInflater.from(this);
        View dialogueLayout = inflater.inflate(R.layout.interval_dialog, null);
        builder.setView(dialogueLayout);

        final AlertDialog dialog = builder.show();
        final Spinner unitSpinner = dialogueLayout.findViewById(R.id.unit_spinner);
        final Button intervalButton = findViewById(R.id.intervalButton);
        final EditText timeValueEditText = dialogueLayout.findViewById(R.id.time_value_edit_text);

        // hide the default rectangular container of the dialog
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));

        unitSpinner.setOnItemSelectedListener( new AdapterView.OnItemSelectedListener() {
            public void onNothingSelected(AdapterView<?> arg0) {
            }
            public void onItemSelected(AdapterView<?> parent, View v, int position, long id) {
                timeUnit = parent.getItemAtPosition(position).toString();
            }
        });
        List<String> units = new ArrayList<String>();
        units.add("Seconds");
        units.add("Minutes");
        units.add("Hours");

        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, units);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        unitSpinner.setAdapter(dataAdapter);

        builder.setTitle("Edit Time Interval");

        // button closes dialog on click
        Button cancelButton = dialogueLayout.findViewById(R.id.cancel_button);
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.cancel();
            }
        });

        // button saves user inputs on click
        Button saveButton = dialogueLayout.findViewById(R.id.save_button);
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String timeValueString = timeValueEditText.getText().toString();

                if (timeValueString.equals("")) {
                    if (timeUnit.equals("Seconds")) {
                        // minimal of 20 seconds if time unit is in seconds
                        timeValue = 20;
                        timeValueSeconds = 20;
                    } else if (timeUnit.equals("Minutes")){
                        // minimal of 1 minute of time unit is in minutes
                        timeValue = 1;
                        timeValueSeconds = 60;
                    } else {
                        // minimum of 1 hour if time unit is in hours
                        timeValue = 1;
                        timeValueSeconds = 3600;
                    }
                } else {
                    timeValue = Integer.parseInt(timeValueString);

                    if (timeUnit.equals("Minutes")) {
                        if (timeValue > maxMinutes) {
                            timeValue = maxMinutes;
                        }
                        timeValueSeconds = timeValue * 60;
                    }
                    else if (timeUnit.equals("Seconds")) {
                        if (timeValue > maxSeconds) {
                            timeValue = maxSeconds;
                        }
                        timeValueSeconds = timeValue;
                    }
                    else {
                        if (timeValue > maxHours) {
                            timeValue = maxHours;
                        }
                        timeValueSeconds = timeValue * 3600;
                    }
                }
//                Log.d("timeUnit",": " + timeUnit);
//                Log.d("timeValue", ": " + timeValue);
//                Log.d("timeValueSeconds", ": " + timeValueSeconds);

                intervalButton.setText(timeValue + " " + timeUnit);

                // store timeValue and timeUnit in sharedPreferences
                SharedPreferences.Editor editor = sharedPref.edit();
                editor.putString(getString(R.string.time_unit), timeUnit);
                editor.putInt(getString(R.string.time_value), timeValue);
                editor.putInt(getString(R.string.time_value_seconds), timeValueSeconds);
                editor.commit();

                dialog.dismiss();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    public void getLockPermissions() {
        Intent intent = new Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN);
        intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, compName);
        intent.putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION, "This app requires locking permission to lock the screen.");
        startActivityForResult(intent, RESULT_ENABLE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == RESULT_ENABLE) {
            if (resultCode == Activity.RESULT_OK) {
                Toast.makeText(MainActivity.this, "Granted permission by user.", Toast.LENGTH_LONG).show();
            }
            else {
                Toast.makeText(MainActivity.this, "Denied permission by user.", Toast.LENGTH_LONG).show();
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }


    // checks if the service is already running
    // source: https://stackoverflow.com/questions/600207/how-to-check-if-a-service-is-running-on-android by geekQ
    private boolean isMyServiceRunning(Class<?> serviceClass) {
        for (ActivityManager.RunningServiceInfo service : activityManager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }
}
