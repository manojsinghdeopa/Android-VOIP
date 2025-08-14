package com.example.voipsim.util;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.provider.Settings;
import android.util.Log;

import androidx.work.Data;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import com.example.voipsim.receiver.IncomingCallReceiver;
import com.example.voipsim.work.IncomingCallWorker;

import java.util.concurrent.TimeUnit;

public class CallScheduler {

    private static final String TAG = "CallScheduler";
    private static final String WM_TAG = "voipsim_incoming_call";

    public static void schedule(Context ctx, String caller, long delayMs) {
        requestExactAlarmIfNeeded(ctx);

        long triggerAt = System.currentTimeMillis() + delayMs;

        // Primary: exact AlarmManager
        Intent i = new Intent(ctx, IncomingCallReceiver.class).putExtra("caller", caller);
        PendingIntent pi = PendingIntent.getBroadcast(ctx, (int) (System.currentTimeMillis() & 0x7FFFFFFF), i,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        AlarmManager am = (AlarmManager) ctx.getSystemService(Context.ALARM_SERVICE);
        if (am != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pi);
            } else {
                am.setExact(AlarmManager.RTC_WAKEUP, triggerAt, pi);
            }
        }

        // Fallback: WorkManager with same delay (dedupe by tag)
        WorkManager.getInstance(ctx).cancelAllWorkByTag(WM_TAG);
        OneTimeWorkRequest req = new OneTimeWorkRequest.Builder(IncomingCallWorker.class)
                .setInitialDelay(delayMs, TimeUnit.MILLISECONDS)
                .setInputData(new Data.Builder()
                        .putString(IncomingCallWorker.KEY_CALLER, caller)
                        .build())
                .addTag(WM_TAG)
                .build();
        WorkManager.getInstance(ctx).enqueue(req);

        Log.d(TAG, "Scheduled incoming call in " + delayMs + "ms for: " + caller);
    }

    private static void requestExactAlarmIfNeeded(Context ctx) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            AlarmManager am = (AlarmManager) ctx.getSystemService(Context.ALARM_SERVICE);
            if (am != null && !am.canScheduleExactAlarms()) {
                Intent intent = new Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                ctx.startActivity(intent);
            }
        }
    }
}
