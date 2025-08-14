package com.example.voipsim.service;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.content.pm.ServiceInfo;

import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.example.voipsim.R;
import com.example.voipsim.ui.OngoingCallActivity;

public class CallService extends Service {

    public static final int NOTIF_ID = 7001;
    private static final String CHANNEL_ID = "ongoing_calls";
    private long startTime;
    private String caller;

    private Handler handler = new Handler(Looper.getMainLooper());
    private Runnable tickRunnable;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        caller = intent.getStringExtra("caller");
        startTime = intent.getLongExtra("startTime", System.currentTimeMillis());

        SharedPreferences prefs = getSharedPreferences("call_state", MODE_PRIVATE);
        prefs.edit()
                .putBoolean("isCallActive", true)
                .putLong("callStartTime", startTime)
                .putString("caller", caller)
                .apply();

        createNotificationChannel();

        Intent tapIntent = new Intent(this, OngoingCallActivity.class);
        tapIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, tapIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder nb = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.mipmap.ic_launcher) // your custom small icon
                .setLargeIcon(BitmapFactory.decodeResource(this.getResources(),
                        android.R.drawable.ic_menu_call)) // system call icon as large icon
                .setContentTitle("On call: " + caller)
                .setContentText("00:00")
                .setOngoing(true)
                .setCategory(NotificationCompat.CATEGORY_CALL)
                .setContentIntent(pendingIntent);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(NOTIF_ID, nb.build(), ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK);
        } else {
            startForeground(NOTIF_ID, nb.build());
        }

        tickRunnable = new Runnable() {
            @Override
            public void run() {
                long elapsedSec = (System.currentTimeMillis() - startTime) / 1000;

                // Update notification live timer
                nb.setContentText(format(elapsedSec));
                if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
                    NotificationManagerCompat.from(CallService.this).notify(NOTIF_ID, nb.build());
                }

                // Broadcast to activity
                Intent tick = new Intent("CALL_TICK");
                tick.putExtra("elapsedSec", elapsedSec);
                tick.setPackage(getPackageName());
                sendBroadcast(tick);

                handler.postDelayed(this, 1000);
            }
        };
        handler.post(tickRunnable);

        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        handler.removeCallbacksAndMessages(null);
        SharedPreferences prefs = getSharedPreferences("call_state", MODE_PRIVATE);
        prefs.edit().putBoolean("isCallActive", false)
                .remove("callStartTime")
                .remove("caller")
                .apply();
        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void createNotificationChannel() {
        NotificationChannel channel = new NotificationChannel(
                CHANNEL_ID, "Ongoing Calls", NotificationManager.IMPORTANCE_LOW);
        channel.setDescription("Notifications for ongoing calls");
        NotificationManager nm = getSystemService(NotificationManager.class);
        nm.createNotificationChannel(channel);
    }

    private String format(long sec) {
        long m = sec / 60;
        long s = sec % 60;
        return String.format("%02d:%02d", m, s);
    }
}
