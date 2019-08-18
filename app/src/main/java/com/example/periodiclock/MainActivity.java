package com.example.periodiclock;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
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

//    private static Boolean enable = false;
    String timeUnit = "Minutes";
    int timeValue = 0;
    public static final int RESULT_ENABLE = 11;
    DevicePolicyManager devicePolicyManager;
    ActivityManager activityManager;
    ComponentName compName;
//    DBHelper MyDB;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

//        MyDB = new DBHelper(this);

        compName = new ComponentName(this, MyAdmin.class);
        devicePolicyManager = (DevicePolicyManager) getSystemService(DEVICE_POLICY_SERVICE);
        activityManager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);

        Switch enableSwitch = findViewById(R.id.enableSwitch);
        Button aboutButton = findViewById(R.id.aboutButton);
        Button intervalButton = findViewById(R.id.intervalButton);

        enableSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener(){
            public void onCheckedChanged (CompoundButton buttonView, boolean isChecked) {
//                enable = isChecked;
                boolean active = devicePolicyManager.isAdminActive(compName);
                if (active && isChecked) {
                    devicePolicyManager.lockNow();
                } else if (isChecked && !active){
                    Toast.makeText(MainActivity.this, "Problem to enable the Admin Device features", Toast.LENGTH_SHORT).show();
                    getLockPermissions();
                }
            }
        });
    }



    public void startService(View v) {

        Intent serviceIntent = new Intent(this, PeriodicLockService.class);
        serviceIntent.putExtra("inputExtra", "TESTING");

        ContextCompat.startForegroundService(this, serviceIntent);
    }

    public void stopService(View v) {
        Intent serviceIntent = new Intent(this, PeriodicLockService.class);
        stopService(serviceIntent);
    }

    public void showIntervalDialog(View view) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        //set the new exercise layout in dialog
        LayoutInflater inflater = LayoutInflater.from(this);
        View dialogueLayout = inflater.inflate(R.layout.interval_dialog, null);
        builder.setView(dialogueLayout);
//
//        final InputMethodManager imm = (InputMethodManager) this.getSystemService(INPUT_METHOD_SERVICE);
//        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED,InputMethodManager.HIDE_IMPLICIT_ONLY);

//        final TextView timeValueTextView = findViewById(R.id.time_value_edit_text);
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
        units.add("Minutes");
        units.add("Seconds");

        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, units);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        unitSpinner.setAdapter(dataAdapter);

        builder.setTitle("Edit Time Interval");

        // button closes settings dialog on click
        Button cancelButton = dialogueLayout.findViewById(R.id.cancel_button);
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                imm.hideSoftInputFromWindow(name_editText.getWindowToken(), 0);
                dialog.cancel();
            }
        });

        // button saves settings dialog on click
        Button saveButton = dialogueLayout.findViewById(R.id.save_button);
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                timeValue = Integer.parseInt(timeValueEditText.getText().toString());
                intervalButton.setText(timeValue + " " + timeUnit);

                // update database to save timeValue and timeUnit

                dialog.dismiss();
            }
        });
    }



    @Override
    protected void onResume() {
        super.onResume();
        boolean isActive = devicePolicyManager.isAdminActive(compName);

    }


    public void getLockPermissions() {
        Intent intent = new Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN);
        intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, compName);
        intent.putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION, "Additional text explaining why we need this permission");
        startActivityForResult(intent, RESULT_ENABLE);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch(requestCode) {
            case RESULT_ENABLE :
                if (resultCode == Activity.RESULT_OK) {
                    Toast.makeText(MainActivity.this, "You have enabled the Admin Device features", Toast.LENGTH_LONG).show();
                    activateTimer();
                } else {
                    Toast.makeText(MainActivity.this, "Problem to enable the Admin Device features", Toast.LENGTH_LONG).show();
                }
                break;
        }

        super.onActivityResult(requestCode, resultCode, data);
    }


    public void activateTimer() {
        
    }

    public void hideKeyboard(View v) {
        final InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
    }



}
