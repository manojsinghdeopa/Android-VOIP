package com.example.voipsim.util;

import android.app.ActivityManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Build;

import androidx.core.app.NotificationCompat;

import com.example.voipsim.R;
import com.example.voipsim.ui.IncomingCallActivity;

public class NotificationHelper {

    private static final String CHANNEL_ID = "incoming_calls";
    public static final int NOTIF_ID = 9001;

    public static void showIncomingCallNotification(Context context, String caller) {
        createChannelIfNeeded(context);

        Intent intent = new Intent(context, IncomingCallActivity.class)
                .putExtra("caller", caller)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);

        PendingIntent pendingIntent = PendingIntent.getActivity(
                context, 2001, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        Notification notification = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.mipmap.ic_launcher) // your custom small icon
                .setLargeIcon(BitmapFactory.decodeResource(context.getResources(),
                        android.R.drawable.sym_call_incoming)) // system call icon as large icon
                .setContentTitle("Incoming call : ")
                .setContentText(caller)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(NotificationCompat.CATEGORY_CALL)
                .setFullScreenIntent(pendingIntent, true)
                .setAutoCancel(true)
                .setOngoing(true)
                .build();

        NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (nm != null) nm.notify(NOTIF_ID, notification);
    }

    private static void createChannelIfNeeded(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager nm = context.getSystemService(NotificationManager.class);
            if (nm != null && nm.getNotificationChannel(CHANNEL_ID) == null) {
                NotificationChannel channel = new NotificationChannel(
                        CHANNEL_ID, "Incoming Calls", NotificationManager.IMPORTANCE_HIGH
                );
                channel.setDescription("Full-screen notifications for incoming calls");
                nm.createNotificationChannel(channel);
            }
        }
    }

    public static boolean isForeground() {
        ActivityManager.RunningAppProcessInfo info = new ActivityManager.RunningAppProcessInfo();
        ActivityManager.getMyMemoryState(info);
        return info.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND
                || info.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_VISIBLE;
    }
}
