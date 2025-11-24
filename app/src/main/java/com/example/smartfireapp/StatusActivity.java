package com.example.smartfireapp;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

public class StatusActivity extends AppCompatActivity {

    private TextView txtLastUpdate, txtFireCount, txtSmokeCount;
    private LineChart lineChart;
    private Spinner spinnerFilter;
    private DatabaseReference dbRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_status);

        txtFireCount = findViewById(R.id.txtFireCount);
        txtSmokeCount = findViewById(R.id.txtSmokeCount);
        txtLastUpdate = findViewById(R.id.txtLastUpdate);
        lineChart = findViewById(R.id.lineChart);
        spinnerFilter = findViewById(R.id.spinnerFilter);

        dbRef = FirebaseDatabase.getInstance().getReference("fire_events");

        setupSpinner();
        loadRecentData();
    }

    private void setupSpinner() {
        String[] options = {"Recent Data", "Full History"};
        spinnerFilter.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, options));

        spinnerFilter.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, View view, int position, long id) {
                if (position == 0) loadRecentData();
                else loadAllData();
            }

            @Override
            public void onNothingSelected(android.widget.AdapterView<?> parent) {
            }
        });
    }

    private void loadRecentData() {
        dbRef.limitToLast(50).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                plotData(snapshot);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(StatusActivity.this, "Error loading data", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadAllData() {
        dbRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                plotData(snapshot);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(StatusActivity.this, "Error loading data", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void plotData(DataSnapshot snapshot) {
        Map<String, Integer> fireCounts = new TreeMap<>();
        Map<String, Integer> smokeCounts = new TreeMap<>();

        int totalFire = 0, totalSmoke = 0;
        String lastTime = "";

        // Philippine timezone formatters
        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
        SimpleDateFormat fullFormat = new SimpleDateFormat("MMM dd, yyyy - hh:mm a", Locale.getDefault());
        timeFormat.setTimeZone(java.util.TimeZone.getTimeZone("Asia/Manila"));
        fullFormat.setTimeZone(java.util.TimeZone.getTimeZone("Asia/Manila"));

        for (DataSnapshot ds : snapshot.getChildren()) {
            String event = ds.child("event").getValue(String.class);
            Long timestamp = ds.child("timestamp").getValue(Long.class);

            if (event != null && timestamp != null) {
                // Convert timestamp → Philippine time
                String date = timeFormat.format(new Date(timestamp));

                if (event.contains("Fire")) {
                    fireCounts.put(date, fireCounts.getOrDefault(date, 0) + 1);
                    totalFire++;
                } else if (event.contains("Smoke")) {
                    smokeCounts.put(date, smokeCounts.getOrDefault(date, 0) + 1);
                    totalSmoke++;
                }

                lastTime = fullFormat.format(new Date(timestamp));
            }
        }

        // If no data, use current PH time
        if (lastTime.isEmpty()) {
            lastTime = fullFormat.format(new Date());
        }

        txtFireCount.setText(String.valueOf(totalFire));
        txtSmokeCount.setText(String.valueOf(totalSmoke));
        txtLastUpdate.setText("Last update (PH Time): " + lastTime);

        //  CHART DATA
        ArrayList<Entry> fireEntries = new ArrayList<>();
        ArrayList<Entry> smokeEntries = new ArrayList<>();
        ArrayList<String> labels = new ArrayList<>();
        int i = 0;

        int cumulativeFire = 0;
        int cumulativeSmoke = 0;

        for (String date : fireCounts.keySet()) {
            cumulativeFire += fireCounts.get(date);
            cumulativeSmoke += smokeCounts.getOrDefault(date, 0);

            fireEntries.add(new Entry(i, cumulativeFire));
            smokeEntries.add(new Entry(i, cumulativeSmoke));
            labels.add(date);
            i++;
        }

        LineDataSet fireSet = new LineDataSet(fireEntries, "Fire");
        fireSet.setColor(Color.RED);
        fireSet.setCircleColor(Color.RED);
        fireSet.setLineWidth(3f);
        fireSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);
        fireSet.setDrawValues(false);
        fireSet.setDrawFilled(true);
        fireSet.setFillAlpha(60);
        fireSet.setFillColor(Color.RED);

        LineDataSet smokeSet = new LineDataSet(smokeEntries, "Smoke");
        smokeSet.setColor(Color.GRAY);
        smokeSet.setCircleColor(Color.GRAY);
        smokeSet.setLineWidth(3f);
        smokeSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);
        smokeSet.setDrawValues(false);
        smokeSet.setDrawFilled(true);
        smokeSet.setFillAlpha(60);
        smokeSet.setFillColor(Color.LTGRAY);

        LineData data = new LineData(fireSet, smokeSet);
        lineChart.setData(data);
        lineChart.getDescription().setEnabled(false);
        lineChart.setTouchEnabled(true);
        lineChart.setDragEnabled(true);
        lineChart.setScaleEnabled(true);
        lineChart.animateX(800);

        XAxis xAxis = lineChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                int index = (int) value;
                if (index >= 0 && index < labels.size()) return labels.get(index);
                return "";
            }
        });
        xAxis.setLabelRotationAngle(-45);
        xAxis.setGranularity(1f);
        xAxis.setTextColor(Color.DKGRAY);
        xAxis.setDrawGridLines(false);

        int maxFire = fireCounts.values().stream().max(Integer::compareTo).orElse(0);
        int maxSmoke = smokeCounts.values().stream().max(Integer::compareTo).orElse(0);
        int maxValue = Math.max(maxFire, maxSmoke);

        YAxis leftAxis = lineChart.getAxisLeft();
        leftAxis.setAxisMinimum(0f);
        leftAxis.setAxisMaximum(maxValue + (maxValue * 10f)); // Auto-scale
        leftAxis.setTextColor(Color.DKGRAY);
        lineChart.getAxisRight().setEnabled(false);

        lineChart.setExtraBottomOffset(10f);
        lineChart.invalidate();

        //  TABLE DATA
        TableLayout table = findViewById(R.id.tableData);
        table.removeAllViews();

        // Header row
        TableRow header = new TableRow(this);
        String[] headers = {"Time", "Fire Count", "Smoke Count"};
        for (String h : headers) {
            TextView tv = new TextView(this);
            tv.setText(h);
            tv.setTextColor(Color.BLACK);
            tv.setTextSize(14);
            tv.setPadding(16, 8, 16, 8);
            tv.setTypeface(null, android.graphics.Typeface.BOLD);
            header.addView(tv);
        }
        table.addView(header);

        // Data rows
        boolean alternate = false;
        for (String date : fireCounts.keySet()) {
            TableRow row = new TableRow(this);
            if (alternate) row.setBackgroundColor(Color.parseColor("#F9FAFB"));
            alternate = !alternate;

            TextView tvDate = new TextView(this);
            tvDate.setText(date);
            tvDate.setPadding(16, 8, 16, 8);

            TextView tvFire = new TextView(this);
            tvFire.setText(String.valueOf(fireCounts.getOrDefault(date, 0)));
            tvFire.setPadding(16, 8, 16, 8);
            tvFire.setTextColor(Color.RED);

            TextView tvSmoke = new TextView(this);
            tvSmoke.setText(String.valueOf(smokeCounts.getOrDefault(date, 0)));
            tvSmoke.setPadding(16, 8, 16, 8);
            tvSmoke.setTextColor(Color.DKGRAY);

            row.addView(tvDate);
            row.addView(tvFire);
            row.addView(tvSmoke);

            table.addView(row);
        }
    }
}