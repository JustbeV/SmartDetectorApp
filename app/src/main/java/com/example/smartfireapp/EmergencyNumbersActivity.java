package com.example.smartfireapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.HashSet;

public class EmergencyNumbersActivity extends AppCompatActivity {

    ListView listView;
    Button btnAdd;

    ArrayList<String> list;
    ArrayAdapter<String> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_emergency_numbers);

        listView = findViewById(R.id.listView);
        btnAdd = findViewById(R.id.btnAdd);

        loadNumbers();
        refreshList();

        // Tap → dial
        listView.setOnItemClickListener((parent, view, pos, id) -> {
            String item = list.get(pos);
            String number = item.substring(item.lastIndexOf(" ") + 1); // number is last part

            Intent dial = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + number));
            startActivity(dial);
        });

        // Go to AddNumberActivity
        btnAdd.setOnClickListener(v -> {
            startActivity(new Intent(this, AddNumberActivity.class));
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadNumbers();
        refreshList();
    }

    private void loadNumbers() {
        HashSet<String> set = (HashSet<String>) getSharedPreferences("emergency", MODE_PRIVATE)
                .getStringSet("list", new HashSet<>());
        list = new ArrayList<>(set);
    }

    private void refreshList() {
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, list);
        listView.setAdapter(adapter);
    }
}
