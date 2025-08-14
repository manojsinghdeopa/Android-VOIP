package com.example.voipsim.ui;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.voipsim.R;
import com.example.voipsim.data.db.AppDb;
import com.example.voipsim.data.db.CallLogEntity;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;
import java.util.Random;

public class CallLogActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.act_call_log);

        RecyclerView rv = findViewById(R.id.recycler);
        TextView tvEmpty = findViewById(R.id.tvEmpty); // Add TextView in XML for "No Calls History"
        rv.setLayoutManager(new LinearLayoutManager(this));

        LogAdapter adapter = new LogAdapter(tvEmpty);
        rv.setAdapter(adapter);

        AppDb.i(this).dao().getAll().observe(this, data -> {
            adapter.submit(data);
            tvEmpty.setVisibility(data == null || data.isEmpty() ? View.VISIBLE : View.GONE);
        });
    }

    static class LogAdapter extends RecyclerView.Adapter<VH> {
        private List<CallLogEntity> data;
        private final TextView tvEmpty;

        LogAdapter(TextView tvEmpty) { this.tvEmpty = tvEmpty; }

        @SuppressLint("NotifyDataSetChanged")
        void submit(List<CallLogEntity> d) { this.data = d; notifyDataSetChanged(); }

        @NonNull
        @Override
        public VH onCreateViewHolder(@NonNull android.view.ViewGroup parent, int viewType) {
            return new VH(android.view.LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_call_log, parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull VH holder, int position) {
            CallLogEntity e = data.get(position);

            holder.title.setText(e.caller);
            holder.imgCaller.setText(getInitials(e.caller));
            ((GradientDrawable) holder.imgCaller.getBackground()).setColor(getColorForName(e.caller));

            String formattedTime = new SimpleDateFormat("dd MMM, HH:mm", Locale.getDefault()).format(e.startTime);
            String dur = (e.type.equals("INCOMING") || e.type.equals("ANSWERED"))
                    ? " • Duration: " + formatDuration(e.durationSec) : "";
            holder.subtitle.setText(formattedTime + dur);

            int icon = switch (e.type) {
                case "MISSED" -> android.R.drawable.sym_call_missed;
                case "OUTGOING" -> android.R.drawable.sym_call_outgoing;
                default -> android.R.drawable.sym_call_incoming;
            };
            holder.imgCallType.setImageResource(icon);

            holder.itemView.setOnClickListener(v -> { /* TODO: show details or call back */ });
        }

        @Override
        public int getItemCount() { return data == null ? 0 : data.size(); }

        private String getInitials(String name) {
            if (name == null || name.isEmpty()) return "?";
            String[] p = name.trim().split("\\s+");
            return ("" + p[0].charAt(0) + (p.length > 1 ? p[1].charAt(0) : "")).toUpperCase();
        }

        private int getColorForName(String name) {
            if (name == null || name.isEmpty()) return Color.GRAY;
            Random rnd = new Random(name.hashCode());
            return Color.rgb(rnd.nextInt(200), rnd.nextInt(200), rnd.nextInt(200));
        }

        private String formatDuration(long sec) {
            return String.format(Locale.getDefault(), "%02d:%02d", sec / 60, sec % 60);
        }
    }

    static class VH extends RecyclerView.ViewHolder {
        final TextView title, subtitle, imgCaller;
        final android.widget.ImageView imgCallType;

        VH(@NonNull android.view.View v) {
            super(v);
            title = v.findViewById(R.id.title);
            subtitle = v.findViewById(R.id.subtitle);
            imgCaller = v.findViewById(R.id.imgCaller);
            imgCallType = v.findViewById(R.id.imgCallType);
        }
    }
}
