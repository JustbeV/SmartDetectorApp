package com.example.smartfireapp;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

//helps avoid null errors
import androidx.annotation.NonNull;
//helps on older android versions
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

//Firebase...
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
//ref to specific node
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class MainActivity extends AppCompatActivity {
    private static final int REQUEST_NOTIFICATION_PERMISSION = 100;

    private TextView tvCurrentStatus;
    private DatabaseReference db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        UI setup
        setContentView(R.layout.activity_main);

        tvCurrentStatus = findViewById(R.id.tvCurrentStatus);

        Button btnStatus = findViewById(R.id.btnStatus);
        Button btnCall911 = findViewById(R.id.btnCall911);
        Button btnManageNumbers = findViewById(R.id.btnManageNumbers);



        requestNotificationPermission();



        FirebaseAuth mAuth = FirebaseAuth.getInstance();

// Sign in anonymously if not already signed in
        if (mAuth.getCurrentUser() == null) {
            mAuth.signInAnonymously()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            FirebaseUser user = mAuth.getCurrentUser();
                            if (user != null) {
                                Log.d("FirebaseAuth", "Signed in as: " + user.getUid());
                            }
                        } else {
                            Log.e("FirebaseAuth", "Sign-in failed: ", task.getException());
                        }
                    });
        }


        // Start FireMonitorService keeping app alive in background
        //starts FireMonitorService class
        Intent serviceIntent = new Intent(this, FireMonitorService.class);
//        for android 8+(API 25)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(serviceIntent);
        } else {
            startService(serviceIntent);
        }

//         listening to firebase realtime DB
        db = FirebaseDatabase.getInstance().getReference("fire_events");
            //get the last data(child)
        db.orderByKey().limitToLast(1).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot child : snapshot.getChildren()) {
                    String event = child.child("event").getValue(String.class);

                    ImageView imgLogo = findViewById(R.id.imgLogo);

                    // pulse animation for fire logo
                    if ("Fire Detected".equalsIgnoreCase(event)) {
                        Animation pulse = AnimationUtils.loadAnimation(MainActivity.this, R.anim.pulse);
                        imgLogo.startAnimation(pulse);
                    } else {
                        imgLogo.clearAnimation();
                    }

                    // display event on app homepage
                    if (tvCurrentStatus != null) {
                        tvCurrentStatus.setText("Status: " + (event != null ? event : "--"));
                    }

                    // trigger notification while app is open
                    if ("Fire Detected".equalsIgnoreCase(event) ||
                            "Smoke Detected".equalsIgnoreCase(event)) {

                        NotificationHelperService.showCustomNotification(MainActivity.this, event);

                    } else if ("Safe".equalsIgnoreCase(event)) {
                        NotificationHelperService.showSafeNotification(MainActivity.this, event);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(MainActivity.this, "Firebase error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        // button actions
        btnStatus.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, StatusActivity.class)));

        //opens the phone dialer
        btnCall911.setOnClickListener(v -> {
            Intent i = new Intent(MainActivity.this, EmergencyNumbersActivity.class);
            startActivity(i);
//            Intent intent = new Intent(Intent.ACTION_DIAL);
//            //opens the dialer app that has 911 already type in
//            intent.setData(android.net.Uri.parse("tel:911"));
//            try {
//                startActivity(intent);
//            } catch (Exception e) {
//                Toast.makeText(this, "Unable to open dialer", Toast.LENGTH_SHORT).show();
//            }
        });
        btnManageNumbers.setOnClickListener(v -> {
            Intent i = new Intent(MainActivity.this, ManageNumbersActivity.class);
            startActivity(i);
        });
        Button btnManageWifi = findViewById(R.id.btnManageWifi);

        btnManageWifi.setOnClickListener(v -> {
            Intent i = new Intent(MainActivity.this, ManageWifiActivity.class);
            startActivity(i);
        });


        // Footer link
        TextView tvFooter = findViewById(R.id.tvFooter);
        tvFooter.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, FaqActivity.class)));

        TextView tvFooter2 = findViewById(R.id.tvFooter2);

        tvFooter2.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, NewsLetter.class)));
    }



    private void requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) { // API 33
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {

                ActivityCompat.requestPermissions(
                        this,
                        new String[]{Manifest.permission.POST_NOTIFICATIONS},
                        REQUEST_NOTIFICATION_PERMISSION
                );
            }
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_NOTIFICATION_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // User allowed notifications
            } else {
                // User denied – maybe show a Toast
            }
        }
    }
}
