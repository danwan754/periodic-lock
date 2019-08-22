package com.example.periodiclock;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class ScreenReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {

        if (intent.getAction().equals(Intent.ACTION_SCREEN_ON)) {
            Intent i = new Intent(context, PeriodicLockService.class);
            context.startService(i);
        }
//        else (intent.getAction().equals(Intent.ACTION_SCREEN_OFF)) {
//        }
    }
}