package com.example.voipsim;

import android.Manifest;
import android.app.AlarmManager;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.service.notification.StatusBarNotification;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.voipsim.databinding.ActivityMainBinding;
import com.example.voipsim.ui.CallLogActivity;
import com.example.voipsim.ui.IncomingCallActivity;
import com.example.voipsim.ui.OngoingCallActivity;
import com.example.voipsim.ui.ScheduleCallActivity;
import com.example.voipsim.util.NotificationHelper;
import com.example.voipsim.util.NotificationUtils;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;

    private final ActivityResultLauncher<String> notifPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), granted -> {
                if (!granted) showPermissionDialog("Notification", () -> finish());
            });

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        checkOngoingCall();
        NotificationUtils.createChannels(this);

        requestNotificationPermission();
        checkAndRequestAlarmPermission();

        binding.btnSchedule.setOnClickListener(v -> startActivity(new Intent(this, ScheduleCallActivity.class)));
        binding.btnLogs.setOnClickListener(v -> startActivity(new Intent(this, CallLogActivity.class)));
    }

    @Override
    protected void onResume() {
        super.onResume();
        handleIncomingCallNotification();
    }

    // ---------- Helper Methods ----------

    private void checkOngoingCall() {
        SharedPreferences prefs = getSharedPreferences("call_state", MODE_PRIVATE);
        if (prefs.getBoolean("isCallActive", false)) {
            startActivity(new Intent(this, OngoingCallActivity.class)
                    .putExtra("caller", prefs.getString("caller", ""))
                    .putExtra("startTime", prefs.getLong("callStartTime", System.currentTimeMillis())));
            finish();
        }
    }

    private void handleIncomingCallNotification() {
        NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (nm != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            for (StatusBarNotification sbn : nm.getActiveNotifications()) {
                if (sbn.getId() == NotificationHelper.NOTIF_ID) {
                    startActivity(new Intent(this, IncomingCallActivity.class)
                            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP));
                    nm.cancel(NotificationHelper.NOTIF_ID);
                    break;
                }
            }
        }
    }

    private void requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS)
                        != android.content.pm.PackageManager.PERMISSION_GRANTED) {
            showPermissionDialog("Notification", () -> notifPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS));
        }
    }

    private void checkAndRequestAlarmPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            AlarmManager am = getSystemService(AlarmManager.class);
            if (am != null && !am.canScheduleExactAlarms()) {
                showPermissionDialog("Alarm & reminders", () -> {
                    Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                            .setData(Uri.fromParts("package", getPackageName(), null));
                    startActivity(intent);
                });
            }
        }
    }

    private void showPermissionDialog(String type, Runnable onGrant) {
        new AlertDialog.Builder(this)
                .setTitle(type + " Permission Required")
                .setMessage("You must allow " + type + " permission for this feature to work. Please enable it.")
                .setCancelable(false)
                .setPositiveButton("Grant", (dialog, which) -> onGrant.run())
                .setNegativeButton("Exit App", (dialog, which) -> finish())
                .show();
    }
}
