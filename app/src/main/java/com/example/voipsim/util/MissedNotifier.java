package com.example.voipsim.util;

import android.Manifest;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;

import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.example.voipsim.R;
import com.example.voipsim.ui.CallLogActivity;
import com.example.voipsim.ui.IncomingCallActivity;

import java.text.DateFormat;
import java.util.Date;

/**
 * Shows a missed-call notification with a quick-callback action.
 */
public class MissedNotifier {
    public static void show(Context ctx, String caller, long when) {
        Intent cb = new Intent(ctx, IncomingCallActivity.class)
                .putExtra("caller", caller + " (callback)")
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        PendingIntent cbPi = PendingIntent.getActivity(ctx, 3001, cb, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        PendingIntent logPi = PendingIntent.getActivity(ctx, 3002, new Intent(ctx, CallLogActivity.class), PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder nb = new NotificationCompat.Builder(ctx, "missed_calls")
                .setSmallIcon(R.mipmap.ic_launcher) // your custom small icon
                .setLargeIcon(BitmapFactory.decodeResource(ctx.getResources(),
                        android.R.drawable.sym_call_missed)) // system call icon as large icon
                .setContentTitle("Missed Call: " + caller)
                .setContentText(DateFormat.getTimeInstance().format(new Date(when)))
                .setContentIntent(logPi)
                .addAction(new NotificationCompat.Action(R.drawable.ic_call, "Call back", cbPi))
                .setAutoCancel(true);
        if (ActivityCompat.checkSelfPermission(ctx, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        NotificationManagerCompat.from(ctx).notify(9101, nb.build());
    }
}
