package com.example.smartfireapp;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashSet;

public class AddNumberActivity extends AppCompatActivity {

    EditText etName, etNumber;
    Button btnSave;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_number);

        etName = findViewById(R.id.etName);
        etNumber = findViewById(R.id.etNumber);
        btnSave = findViewById(R.id.btnSave);

        btnSave.setOnClickListener(v -> {
            String name = etName.getText().toString().trim();
            String number = etNumber.getText().toString().trim();

            if (name.isEmpty() || number.isEmpty()) {
                Toast.makeText(this, "Fill all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            HashSet<String> set = (HashSet<String>) getSharedPreferences("emergency", MODE_PRIVATE)
                    .getStringSet("list", new HashSet<>());

            ArrayList<String> list = new ArrayList<>(set);
            list.add(name + " " + number);  // Store as "Name Number"

            getSharedPreferences("emergency", MODE_PRIVATE)
                    .edit()
                    .putStringSet("list", new HashSet<>(list))
                    .apply();

            Toast.makeText(this, "Number saved!", Toast.LENGTH_SHORT).show();
            finish();
        });
    }
}
