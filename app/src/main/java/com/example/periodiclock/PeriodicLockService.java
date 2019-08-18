package com.example.periodiclock;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;

import static com.example.periodiclock.App.CHANNEL_ID;

public class PeriodicLockService extends Service {

    private final int mNotificationId = 1;
    private NotificationCompat.Builder mBuilder;
    private NotificationManager mNotificationManager;
    private final String title = "Example Title";

    @Override
    public void onCreate() {
        super.onCreate();
        this.mBuilder = new NotificationCompat.Builder(this, CHANNEL_ID);
        this.mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String input = intent.getStringExtra("inputExtra");

        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this,
                0, notificationIntent, 0);

        Notification notification = createNotification(title, input, pendingIntent);

        startForeground(1, notification);

        return START_NOT_STICKY;
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

    private void updateNotification(String content) {
        mBuilder.setContentText(content);

        mNotificationManager.notify(mNotificationId, mBuilder.build());
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

}
