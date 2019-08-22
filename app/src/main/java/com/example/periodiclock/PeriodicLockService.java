package com.example.periodiclock;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.app.admin.DevicePolicyManager;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.Date;

import static com.example.periodiclock.App.CHANNEL_ID;

public class PeriodicLockService extends Service {

    DevicePolicyManager devicePolicyManager;

    private final int mNotificationId = 1;
    private NotificationCompat.Builder mBuilder;
    private Notification notification;
    private NotificationManager mNotificationManager;
    private Runnable runnable;
    private Handler handler;
    private ScreenReceiver screenReceiver;
//    private final String title = "Example Title";

    // timeValue is in milliseconds
    private int timeValue;

    @Override
    public void onCreate() {
        super.onCreate();
        this.mBuilder = new NotificationCompat.Builder(this, CHANNEL_ID);
        this.mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        this.devicePolicyManager = (DevicePolicyManager) getSystemService(DEVICE_POLICY_SERVICE);
        this.handler = new Handler();
        this.runnable = new Runnable() {
            @Override
            public void run() {
                devicePolicyManager.lockNow();
            }
        };

        // initialize receiver
        IntentFilter filter = new IntentFilter(Intent.ACTION_SCREEN_ON);
        filter.addAction(Intent.ACTION_SCREEN_OFF);
        this.screenReceiver = new ScreenReceiver();
        registerReceiver(screenReceiver, filter);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d("onStart", "onStart");
        // first time starting service which is called by MainActivity
        if (intent.getBooleanExtra("mainCall", false)) {
            Log.d("mainCall", "mainCall");

            // the timeValue extra is in seconds which is converted to milliseconds
            this.timeValue = intent.getIntExtra("timeValue", 0) * 1000;
            Log.d("timeValue", "time: " + timeValue);

            Intent notificationIntent = new Intent(this, MainActivity.class);
            PendingIntent pendingIntent = PendingIntent.getActivity(this,
                    0, notificationIntent, 0);

            String title = createNotifyContent(timeValue);
            String content = "Tap to disable screen locking.";

            notification = createNotification(title, content, pendingIntent);
            startForeground(1, notification);
            handler.postDelayed(runnable, timeValue);
        }
        else {
            Log.d("elseCall", "elseCall");
            Log.d("timeValue", "time: " + timeValue);
            cancelLockCountDown();
            String title = createNotifyContent(timeValue);
            updateNotification(title);
            handler.postDelayed(runnable, timeValue);
        }

        return START_NOT_STICKY;
    }


    private Notification createNotification(String title, String content, PendingIntent pendingIntent) {
        mBuilder.setContentTitle(title)
                .setContentText(content)
                .setSmallIcon(R.drawable.ic_android)
                .setContentIntent(pendingIntent)
                .setOnlyAlertOnce(true);
        return mBuilder.build();
    }

    private void updateNotification(String title) {
        mBuilder.setContentTitle(title);
        mNotificationManager.notify(mNotificationId, mBuilder.build());
    }

    public String createNotifyContent(int timeValue) {
        SimpleDateFormat formatter = new SimpleDateFormat("h:mm:ss a");
        Date date = new Date();
//        Log.d("date1", formatter.format(date));
        date.setTime(date.getTime() + timeValue);
//        Log.d("date2", formatter.format(date));
        return "Screen will lock at " + formatter.format(date);

    }

    public void cancelLockCountDown() {
        handler.removeCallbacks(runnable);
    }

    @Override
    public void onDestroy() {
        cancelLockCountDown();
        unregisterReceiver(screenReceiver);
        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

}
