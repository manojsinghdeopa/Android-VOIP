package com.example.voipsim.util;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;

import androidx.core.app.NotificationManagerCompat;

/**
 * Helper to create notification channels used by the app.
 */
public class NotificationUtils {
    public static void createChannels(Context ctx){
        if (Build.VERSION.SDK_INT < 26) return;
        NotificationManager nm = (NotificationManager) ctx.getSystemService(Context.NOTIFICATION_SERVICE);
        if (nm == null) return;
        NotificationChannel incoming = new NotificationChannel("incoming_calls","Incoming Calls", NotificationManager.IMPORTANCE_HIGH);
        NotificationChannel ongoing = new NotificationChannel("ongoing_calls","Ongoing Calls", NotificationManager.IMPORTANCE_LOW);
        NotificationChannel missed  = new NotificationChannel("missed_calls","Missed Calls", NotificationManager.IMPORTANCE_DEFAULT);
        nm.createNotificationChannel(incoming);
        nm.createNotificationChannel(ongoing);
        nm.createNotificationChannel(missed);
    }


    public static void clearNotifications(Context ctx){
        // Clear notifications
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(ctx);
        notificationManager.cancelAll();
    }
}
