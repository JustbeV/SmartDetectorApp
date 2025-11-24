package com.example.smartfireapp;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class ManageWifiActivity extends AppCompatActivity {

    EditText etWifiName, etWifiPassword;
    Button btnSaveWifi;

    DatabaseReference wifiRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_wifi);

        etWifiName = findViewById(R.id.etWifiName);
        etWifiPassword = findViewById(R.id.etWifiPassword);
        btnSaveWifi = findViewById(R.id.btnSaveWifi);

        // Firebase path: wifi_settings/
        wifiRef = FirebaseDatabase.getInstance().getReference("wifi_settings");

        btnSaveWifi.setOnClickListener(v -> {
            String ssid = etWifiName.getText().toString().trim();
            String pass = etWifiPassword.getText().toString().trim();

            if (ssid.isEmpty() || pass.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            wifiRef.child("ssid").setValue(ssid);
            wifiRef.child("password").setValue(pass);

            Toast.makeText(this, "WiFi settings saved!", Toast.LENGTH_LONG).show();
        });
    }
}
