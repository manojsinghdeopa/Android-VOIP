package com.example.voipsim.ui;

import android.content.Intent;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.voipsim.R;
import com.example.voipsim.data.db.AppDb;
import com.example.voipsim.data.db.CallLogEntity;
import com.example.voipsim.service.CallService;
import com.example.voipsim.util.MissedNotifier;
import com.example.voipsim.util.NotificationUtils;

import java.util.concurrent.Executors;

public class IncomingCallActivity extends AppCompatActivity {
    private MediaPlayer mp;
    private Vibrator vb;
    private CountDownTimer timer;
    private String caller;
    private long startAt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.act_incoming_call);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        caller = getIntent().getStringExtra("caller");
        if (caller == null || caller.isEmpty()) caller = "Caller";
        startAt = System.currentTimeMillis();

        // Caller Initial
        TextView txtInitial = findViewById(R.id.txtCallerInitial);
        txtInitial.setText(caller.substring(0, 1).toUpperCase());
        Animation pulse = AnimationUtils.loadAnimation(this, R.anim.pulse);
        txtInitial.startAnimation(pulse);

        // Caller Name
        ((TextView) findViewById(R.id.txtCaller)).setText(caller);

        // Play ringtone
        Uri tone = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);
        mp = MediaPlayer.create(this, tone);
        if (mp != null) {
            mp.setLooping(true);
            mp.start();
        }

        // Vibrate pattern
        vb = (Vibrator) getSystemService(VIBRATOR_SERVICE);
        if (vb != null) {
            if (Build.VERSION.SDK_INT >= 26)
                vb.vibrate(VibrationEffect.createWaveform(new long[]{0, 500, 500}, 0));
            else vb.vibrate(new long[]{0, 500, 500}, 0);
        }

        // Auto-miss countdown
        timer = new CountDownTimer(10_000, 1000) {
            public void onTick(long l) {
                ((TextView) findViewById(R.id.txtCountdown)).setText(String.valueOf(l / 1000));
            }

            public void onFinish() {
                markMissedAndExit();
            }
        }.start();

        findViewById(R.id.btnAnswer).setOnClickListener(v -> answer());
        findViewById(R.id.btnReject).setOnClickListener(v -> markMissedAndExit());
    }

    private void answer() {
        NotificationUtils.clearNotifications(this);
        stopAlerting();
        Intent s = new Intent(this, CallService.class)
                .putExtra("caller", caller)
                .putExtra("startTime", startAt);
        ContextCompat.startForegroundService(this, s);
        startActivity(new Intent(this, OngoingCallActivity.class)
                .putExtra("caller", caller)
                .putExtra("startTime", startAt));
        finishAffinity();
    }

    private void markMissedAndExit() {
        stopAlerting();
        long end = System.currentTimeMillis();
        Executors.newSingleThreadExecutor().execute(() -> {
            CallLogEntity e = new CallLogEntity();
            e.caller = caller;
            e.startTime = startAt;
            e.endTime = end;
            e.type = "MISSED";
            e.durationSec = 0;
            AppDb.i(getApplicationContext()).dao().insert(e);
        });
        MissedNotifier.show(this, caller, startAt);
        finish();
    }

    private void stopAlerting() {
        if (timer != null) timer.cancel();
        if (mp != null) {
            try {
                mp.stop();
            } catch (Exception ignored) {
            }
            mp.release();
        }
        if (vb != null) vb.cancel();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopAlerting();
    }
}
