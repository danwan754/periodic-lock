package com.example.periodiclock;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class ScreenReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(Intent.ACTION_SCREEN_OFF)) {
//            screenOff = true;
        } else if (intent.getAction().equals(Intent.ACTION_SCREEN_ON)) {
//            screenOff = false;
            Intent i = new Intent(context, PeriodicLockService.class);
            context.startService(i);
        }
//        Intent i = new Intent(context, PeriodicLockService.class);
//        context.startService(i);
    }
}