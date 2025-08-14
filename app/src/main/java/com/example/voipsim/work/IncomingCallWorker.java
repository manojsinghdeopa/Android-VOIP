package com.example.voipsim.work;

import android.content.Context;
import android.content.Intent;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.example.voipsim.receiver.IncomingCallReceiver;

public class IncomingCallWorker extends Worker {
    public static final String KEY_CALLER = "caller";

    public IncomingCallWorker(@NonNull Context context, @NonNull WorkerParameters params) {
        super(context, params);
    }

    @NonNull @Override
    public Result doWork() {
        String caller = getInputData().getString(KEY_CALLER);
        Intent i = new Intent(getApplicationContext(), IncomingCallReceiver.class)
                .putExtra("caller", caller);
        getApplicationContext().sendBroadcast(i);
        return Result.success();
    }
}
