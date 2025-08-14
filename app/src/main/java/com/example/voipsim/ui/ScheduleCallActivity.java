package com.example.voipsim.ui;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.voipsim.databinding.ActScheduleCallBinding;
import com.example.voipsim.util.CallScheduler;

public class ScheduleCallActivity extends AppCompatActivity {
    private ActScheduleCallBinding b;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        b = ActScheduleCallBinding.inflate(getLayoutInflater());
        setContentView(b.getRoot());

        // Update avatar initials when user types
        b.inputName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String initials = getInitials(s.toString());
                b.avatarInitials.setText(initials);
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        b.btnScheduleNow.setOnClickListener(v -> {
            String name = b.inputName.getText().toString().trim();
            String secStr = b.inputDelay.getText().toString().trim();
            long sec = secStr.isEmpty() ? 5 : Long.parseLong(secStr);

            if (name.isEmpty()) name = "Caller";

            CallScheduler.schedule(this, name, sec * 1000);
            Toast.makeText(this, "Call scheduled in " + sec + "s", Toast.LENGTH_SHORT).show();
            finish();
            // Reset inputs
            b.inputName.setText("");
            b.inputDelay.setText("");
        });
    }

    private String getInitials(String name) {
        if (name.isEmpty()) return "C";
        String[] parts = name.split(" ");
        String initials = "";
        for (int i = 0; i < Math.min(2, parts.length); i++) {
            if (!parts[i].isEmpty()) initials += parts[i].charAt(0);
        }
        return initials.toUpperCase();
    }
}
