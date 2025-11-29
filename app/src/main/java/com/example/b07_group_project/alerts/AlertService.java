package com.example.b07_group_project.alerts;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.example.b07_group_project.R;

public class AlertService extends Service {

    public static final String EXTRA_PARENT_ID = "parentId";
    private static final String CHANNEL_ID = "smart_air_alerts_channel";
    private static final int NOTIF_ID = 1001;

    public static void start(Context context, String parentId) {
        Intent i = new Intent(context, AlertService.class);
        i.putExtra(EXTRA_PARENT_ID, parentId);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(i);
        } else {
            context.startService(i);
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        createChannel();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String parentId = intent != null ? intent.getStringExtra(EXTRA_PARENT_ID) : null;
        if (parentId == null) {
            stopSelf();
            return START_NOT_STICKY;
        }

        Notification notif = buildNotification();
        startForeground(NOTIF_ID, notif);

        AlertManager.init(getApplicationContext(), parentId);

        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        AlertManager mgr = AlertManager.getInstance();
        if (mgr != null) {
            mgr.stop();
        }
        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void createChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel ch = new NotificationChannel(
                    CHANNEL_ID,
                    "SMART AIR Alerts",
                    NotificationManager.IMPORTANCE_LOW
            );
            NotificationManager nm = getSystemService(NotificationManager.class);
            if (nm != null) {
                nm.createNotificationChannel(ch);
            }
        }
    }

    private Notification buildNotification() {
        NotificationCompat.Builder b = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("SMART AIR monitoring")
                .setContentText("Monitoring asthma alerts in real time")
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setOngoing(true);
        return b.build();
    }
}
