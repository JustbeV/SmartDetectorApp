package com.example.smartfireapp;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

//Service = runs in the background
public class FireMonitorService extends Service {

    private static final String CHANNEL_ID = "FireMonitorServiceChannel";
    private DatabaseReference db;

    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannel();


        // firebase ref
        db = FirebaseDatabase.getInstance().getReference("fire_events");

        db.orderByKey().limitToLast(1).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot child : snapshot.getChildren()) {
                    String event = child.child("event").getValue(String.class);

                    if ("Fire Detected".equalsIgnoreCase(event) ||
                            "Smoke Detected".equalsIgnoreCase(event)) {

                        NotificationHelperService.showCustomNotification(FireMonitorService.this, event);

                    } else if ("Safe".equalsIgnoreCase(event)) {
                        NotificationHelperService.showSafeNotification(FireMonitorService.this, event);
                    }
                }
            }

            //service that act like a background firebase listener
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // Foreground service notification
        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("SmartFireApp Running")
                .setContentText("Monitoring fire and smoke events in background")
                .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
                .setOngoing(true)
                .setCategory(Notification.CATEGORY_SERVICE)
                .build();

        startForeground(1, notification);

        return START_STICKY;
    }

    //stops the foreground notification if system is destroyed
    @Override
    public void onDestroy() {
        super.onDestroy();
        stopForeground(true);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        //not meant to be bound by activities
        // null cuz only foreground service
        return null;
    }

    //must create notification for android 8+
    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel serviceChannel = new NotificationChannel(
                    CHANNEL_ID,
                    "Fire Monitor Service",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) manager.createNotificationChannel(serviceChannel);
        }
    }
}
