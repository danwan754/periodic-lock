package com.danwan.periodiclock;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.KeyguardManager;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.ColorDrawable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
//import android.util.Log;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
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
    private int minMinutes = 1;
    private int minSeconds = 20;
    private int minHours = 1;

    // timeValue may be in seconds, minutes, hours
    private int timeValue;
    private int timeValueSeconds;
    private String timeUnit;
    private boolean isSecure;
    private SharedPreferences sharedPref;

    // is ready to start a new service
    private boolean isReady = true;
    public static final int RESULT_ENABLE = 11;
    public static final int RESULT_AUTHENTICATE_DISABLE_SERVICE = 22;
    public static final int RESULT_AUTHENTICATE_DISABLE_AUTHENTICATION = 33;

    KeyguardManager keyguard_manager;
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
        isSecure = sharedPref.getBoolean("is_secure", false);

        compName = new ComponentName(this, MyAdmin.class);
        devicePolicyManager = (DevicePolicyManager) getSystemService(DEVICE_POLICY_SERVICE);
        activityManager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
        keyguard_manager = (KeyguardManager) getSystemService(KEYGUARD_SERVICE);


        final Switch enableSwitch = findViewById(R.id.enableSwitch);
        final Switch secureSwitch = findViewById(R.id.secureSwitch);
        Button intervalButton = findViewById(R.id.intervalButton);

        intervalButton.setText(timeValue + " " + timeUnit);
        secureSwitch.setChecked(isSecure);

        Intent intent = getIntent();
        if (intent.getBooleanExtra("fromService", false) || isMyServiceRunning(PeriodicLockService.class)) {
            isReady = false;
            enableSwitch.setChecked(true);
        }

        enableSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener(){
            public void onCheckedChanged (CompoundButton buttonView, boolean isChecked) {
                boolean active = devicePolicyManager.isAdminActive(compName);
                if (active && isChecked && isReady) {
                    isReady = false;
                    startService();
                } else if (isChecked && !active && isReady){
                    getLockPermissions();
                    if (active && isReady) {
                        isReady = false;
                        startService();
                    }
                    else {
                        enableSwitch.setChecked(false);
                    }
                }
                else if (!isChecked && !isReady) {
                    if (isSecure) {
                        verifyUnlock("disable_service", RESULT_AUTHENTICATE_DISABLE_SERVICE);
                    }
                    else {
                        stopService();
                        isReady = true;
                    }
                }
            }
        });

        secureSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                if (isChecked) {
                    if (keyguard_manager.isKeyguardSecure()) {
                        saveSecurePref(true);
//                      Toast.makeText(MainActivity.this, "Enabled authentication.", Toast.LENGTH_LONG).show();
                    }
                    else {
                        Toast.makeText(MainActivity.this, "Unable to secure. Please set up unlock credentials for your phone.", Toast.LENGTH_LONG).show();
                        secureSwitch.setChecked(false);
                    }
                }
                else {
                    verifyUnlock("disable_secure", RESULT_AUTHENTICATE_DISABLE_AUTHENTICATION);
//                    Toast.makeText(MainActivity.this, "secure off", Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.help_option:
                Toast.makeText(getApplicationContext(), "Help Selected", Toast.LENGTH_LONG).show();
                displayHelp();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
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

    public void displayHelp() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = LayoutInflater.from(this);
        View dialogueLayout = inflater.inflate(R.layout.help_dialog, null);
        builder.setView(dialogueLayout);

        final AlertDialog dialog = builder.show();

        // hide the default rectangular container of the dialog
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));

        Button cancelButton = dialogueLayout.findViewById(R.id.cancel_button);
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.cancel();
            }
        });
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
                    setMinTime();
                } else {
                    timeValue = Integer.parseInt(timeValueString);

                    if (timeUnit.equals("Minutes")) {
                        if (timeValue > maxMinutes) {
                            timeValue = maxMinutes;
                        } else if (timeValue < minMinutes) {
                            setMinTime();
                        }
                        timeValueSeconds = timeValue * 60;
                    }
                    else if (timeUnit.equals("Seconds")) {
                        if (timeValue > maxSeconds) {
                            timeValue = maxSeconds;
                        } else if (timeValue < minSeconds) {
                            setMinTime();
                        }
                        timeValueSeconds = timeValue;
                    }
                    else {
                        if (timeValue > maxHours) {
                            timeValue = maxHours;
                        } else if (timeValue < minHours) {
                            setMinTime();
                        }
                        timeValueSeconds = timeValue * 3600;
                    }
                }

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

    public void setMinTime() {
        if (timeUnit.equals("Seconds")) {
            timeValue = minSeconds;
            timeValueSeconds = minSeconds;
        } else if (timeUnit.equals("Minutes")){
            timeValue = minMinutes;
            timeValueSeconds = minMinutes * 60;
        } else {
            timeValue = minHours;
            timeValueSeconds = minHours * 3600;
        }
    }

    public void getLockPermissions() {
        Intent intent = new Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN);
        intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, compName);
        intent.putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION, "This app requires locking permission to lock the screen.");
        startActivityForResult(intent, RESULT_ENABLE);
    }

    public void verifyUnlock(String which_disable, int which_code) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            if (keyguard_manager.isKeyguardSecure()) {
                Intent authIntent;
                if (which_disable.equals("disable_service")) {
                    authIntent = keyguard_manager.createConfirmDeviceCredentialIntent("Disable Locking Service", "Disable periodic locking service.");
                }
                else {
                    authIntent = keyguard_manager.createConfirmDeviceCredentialIntent("Disable Authentication", "Disable authentication for disabling locking service.");
                }
                startActivityForResult(authIntent, which_code);
            }
            else {
                Toast.makeText(MainActivity.this, "Secure unsuccessful. Please set up unlock credentials for your phone.", Toast.LENGTH_LONG).show();
                safeDisable(which_code);
            }
        }
        else {
            Toast.makeText(MainActivity.this, "Secure unsuccessful. Your Android version is not supported. Versions 5.0+ are supported.", Toast.LENGTH_LONG).show();
            safeDisable(which_code);
        }

    }

    private void safeDisable(int code) {
        if (code == RESULT_AUTHENTICATE_DISABLE_SERVICE) {
            disableService();
        }
        else if (code == RESULT_AUTHENTICATE_DISABLE_AUTHENTICATION) {
            toggleAuthentication(false);
        }
    }

    public void disableService() {
        final Switch enableSwitch = findViewById(R.id.enableSwitch);
        stopService();
        isReady = true;
        enableSwitch.setChecked(false);
    }

    // toggles the service switch to ON mode, but do not start a new service
    public void safeEnableService() {
        final Switch enableSwitch = findViewById(R.id.enableSwitch);
        enableSwitch.setChecked(true);
    }

    public void toggleAuthentication(boolean val) {
        final Switch secureSwitch = findViewById(R.id.secureSwitch);
        secureSwitch.setChecked(val);
    }

    // store isSecure in sharedPreferences
    private void saveSecurePref(boolean val) {
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putBoolean("is_disable_secure", val);
        editor.commit();
        isSecure = val;
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

        if (requestCode == RESULT_AUTHENTICATE_DISABLE_SERVICE) {
            if (resultCode == Activity.RESULT_OK) {
                disableService();
                Toast.makeText(MainActivity.this, "Disabled periodic locking.", Toast.LENGTH_LONG).show();
            }
            else {
//                Toast.makeText(MainActivity.this, "Failed authentication. Denied attempt to disable service.", Toast.LENGTH_LONG).show();
                safeEnableService();
            }
        }

        if (requestCode == RESULT_AUTHENTICATE_DISABLE_AUTHENTICATION) {
            if (resultCode == Activity.RESULT_OK) {
                saveSecurePref(false);
                Toast.makeText(MainActivity.this, "Disabled authentication.", Toast.LENGTH_LONG).show();
            }
            else {
//                Toast.makeText(MainActivity.this, "Failed authentication. Denied attempt to disable authentication.", Toast.LENGTH_LONG).show();
                toggleAuthentication(true);
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
