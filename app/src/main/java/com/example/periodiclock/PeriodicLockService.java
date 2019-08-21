package com.example.periodiclock;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.app.admin.DevicePolicyManager;
import android.content.Context;
import android.content.Intent;
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
    private Handler handler;
    private Runnable runnable;
    private NotificationManager mNotificationManager;
//    private final String title = "Example Title";
    private boolean firstLock = true;
    // timeValue is in milliseconds
    private int timeValue;

    @Override
    public void onCreate() {
        super.onCreate();
        this.mBuilder = new NotificationCompat.Builder(this, CHANNEL_ID);
        this.mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        this.devicePolicyManager = (DevicePolicyManager) getSystemService(DEVICE_POLICY_SERVICE);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        // the timeValue extra is in seconds which is converted to milliseconds
        this.timeValue = intent.getIntExtra("timeValue", 0) * 1000;

        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this,
                0, notificationIntent, 0);

        String title = createNotifyContent(timeValue);
        String content = "Tap to disable screen locking.";

        notification = createNotification(title, content, pendingIntent);
        startForeground(1, notification);

//        Log.d("timeValue", "timeValue: " + timeValue);
        handler = new Handler();
        runnable = new Runnable() {
            @Override
            public void run() {

                String title = createNotifyContent(timeValue);
                updateNotification(title);
                if (firstLock) {
                    firstLock = false;
                }
                else {
                    devicePolicyManager.lockNow();
                }
                handler.postDelayed(this, timeValue);
            }
        };
        handler.post(runnable);

//        startForeground(1, notification);

        return START_NOT_STICKY;
    }

    @Override
    public void onStart(Intent intent, int startId) {

    }

    private Notification createNotification(String title, String content, PendingIntent pendingIntent) {
        mBuilder.setContentTitle(title)
                .setContentText(content)
                .setSmallIcon(R.drawable.ic_android)
                .setContentIntent(pendingIntent)
                .setOnlyAlertOnce(true);

//        mNotificationManager.notify(mNotificationId, mBuilder.build());

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

    @Override
    public void onDestroy() {
        super.onDestroy();
        handler.removeCallbacks(runnable);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

}
