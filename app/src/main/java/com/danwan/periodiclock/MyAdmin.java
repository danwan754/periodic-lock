package com.danwan.periodiclock;

import android.app.admin.DeviceAdminReceiver;
import android.content.Context;
import android.content.Intent;


public class MyAdmin extends DeviceAdminReceiver {

    @Override
    public void onEnabled(Context context, Intent intent) {
//        Toast.makeText(context, "Device Admin : enabled", Toast.LENGTH_SHORT).show();
//        Toast.makeText(context, "Granted admin locking permission by user.", Toast.LENGTH_LONG).show();

    }

    @Override
    public void onDisabled(Context context, Intent intent) {
//        Toast.makeText(context, "Device Admin : disabled", Toast.LENGTH_SHORT).show();
//        Toast.makeText(context, "Denied admin locking permission by user.", Toast.LENGTH_LONG).show();

    }
}