package com.example.voipsim.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.example.voipsim.ui.IncomingCallActivity;
import com.example.voipsim.util.NotificationHelper;

public class IncomingCallReceiver extends BroadcastReceiver {
    private static final String TAG = "IncomingCallReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        String caller = intent != null ? intent.getStringExtra("caller") : null;
        if (caller == null) caller = "Unknown";
        Log.d(TAG, "Trigger received for: " + caller);


        if (NotificationHelper.isForeground()) {
            // App visible → launch immediately
            Intent full = new Intent(context, IncomingCallActivity.class)
                    .putExtra("caller", caller)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            context.startActivity(full);
        } else {
            // Background/killed → full-screen notification
            NotificationHelper.showIncomingCallNotification(context, caller);
        }
    }
}
