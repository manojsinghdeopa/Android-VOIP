package com.example.voipsim.ui;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.voipsim.R;
import com.example.voipsim.data.db.AppDb;
import com.example.voipsim.data.db.CallLogEntity;
import com.example.voipsim.service.CallService;
import com.example.voipsim.util.NotificationUtils;

import java.util.concurrent.Executors;

public class OngoingCallActivity extends AppCompatActivity {

    private String caller;
    private long startTime;
    private TextView timerView, txtInitial, txtCaller;
    private long elapsedSec = 0;
    private final Handler handler = new Handler(Looper.getMainLooper());

    private final BroadcastReceiver tickReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            elapsedSec = intent.getLongExtra("elapsedSec", elapsedSec);
            timerView.setText(format(elapsedSec));
            pulseTimer(); // Animate timer every second
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Check if call is active
        SharedPreferences prefs = getSharedPreferences("call_state", MODE_PRIVATE);
        boolean isActive = prefs.getBoolean("isCallActive", false);
        if (!isActive) {
            finish(); // no active call, close activity
            return;
        }

        caller = prefs.getString("caller", "Unknown");
        startTime = prefs.getLong("callStartTime", System.currentTimeMillis());

        setContentView(R.layout.act_ongoing_call);

        timerView = findViewById(R.id.txtTimer);
        txtInitial = findViewById(R.id.txtOngoingCallerInitial);
        txtCaller = findViewById(R.id.txtOngoingCaller);

        // Set caller name and initial
        txtCaller.setText(caller);
        if (caller != null && !caller.isEmpty()) {
            txtInitial.setText(caller.substring(0, 1).toUpperCase());
        } else {
            txtInitial.setText("?");
        }

        Button btnEnd = findViewById(R.id.btnEnd);
        btnEnd.setOnClickListener(v -> {
            bounceButton(v);
            handler.postDelayed(this::endCall, 150); // slight delay for animation
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(tickReceiver, new IntentFilter("CALL_TICK"), RECEIVER_NOT_EXPORTED);
    }

    @Override
    protected void onPause() {
        super.onPause();
        try {
            unregisterReceiver(tickReceiver);
        } catch (Exception ignored) {
        }
    }

    private void endCall() {
        stopService(new Intent(this, CallService.class));

        long end = System.currentTimeMillis();
        long duration = (end - startTime) / 1000;

        Executors.newSingleThreadExecutor().execute(() -> {
            CallLogEntity e = new CallLogEntity();
            e.caller = caller;
            e.startTime = startTime;
            e.endTime = end;
            e.type = "ANSWERED";
            e.durationSec = duration;
            AppDb.i(getApplicationContext()).dao().insert(e);
        });

        // Clear call state
        SharedPreferences prefs = getSharedPreferences("call_state", MODE_PRIVATE);
        prefs.edit().clear().apply();

        NotificationUtils.clearNotifications(this);

        // Finish all activities of app so back button doesn't reopen OngoingCallActivity
        finishAffinity();
    }

    private String format(long sec) {
        long m = sec / 60;
        long s = sec % 60;
        return String.format("%02d:%02d", m, s);
    }

    // Animate timer with a small pulse
    private void pulseTimer() {
        ScaleAnimation scale = new ScaleAnimation(
                1f, 1.2f, 1f, 1.2f,
                Animation.RELATIVE_TO_SELF, 0.5f,
                Animation.RELATIVE_TO_SELF, 0.5f
        );
        scale.setDuration(300);
        scale.setRepeatMode(Animation.REVERSE);
        scale.setRepeatCount(1);
        timerView.startAnimation(scale);
    }

    // Bounce effect for button
    private void bounceButton(View v) {
        ScaleAnimation bounce = new ScaleAnimation(
                1f, 0.8f, 1f, 0.8f,
                Animation.RELATIVE_TO_SELF, 0.5f,
                Animation.RELATIVE_TO_SELF, 0.5f
        );
        bounce.setDuration(150);
        bounce.setRepeatMode(Animation.REVERSE);
        bounce.setRepeatCount(1);
        v.startAnimation(bounce);
    }
}
