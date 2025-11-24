//package com.example.smartfireapp;
//
//
//import android.app.NotificationChannel;
//import android.app.NotificationManager;
//import android.app.Service;
//import android.content.Intent;
//import android.os.Build;
//import android.os.IBinder;
//
//import androidx.annotation.NonNull;
//import androidx.annotation.Nullable;
//import androidx.core.app.NotificationCompat;
//
//import android.app.Notification;
//
//import com.google.firebase.database.DataSnapshot;
//import com.google.firebase.database.DatabaseError;
//import com.google.firebase.database.DatabaseReference;
//import com.google.firebase.database.ValueEventListener;
//
////FireMonitorService Test
//public class TestClass extends Service {
//
//    private static final String CHANNEL_ID = "FireMonitorService";
//    private DatabaseReference db;
//
//    @Override
//    public void onCreate() {
//        super.onCreate();
//        createNotification();
//
//
//        db.orderByKey().limitToLast(1).addValueEventListener(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot snapshot) {
//
//                for(DataSnapshot child: snapshot.getChildren()){
//                    String event = child.child("event").getValue(String.class);
//
//                    if("Fire Detected".equalsIgnoreCase(event) || "Smoke Detected".equalsIgnoreCase(event)){
//
//                        NotificationHelperService.showCustomNotification(TestClass.this, event);
//                    }else if("Safe".equalsIgnoreCase(event)){
//                        NotificationHelperService.showSafeNotification(TestClass.this, event);
//                    }
//                }
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError error) {
//
//            }
//        });
//    }
//
//    @Override
//    public int onStartCommand(Intent intent, int flags, int startId) {
////        return super.onStartCommand(intent, flags, startId);
//        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
//                .setContentTitle("SmartFireApp Running")
//                .setContentText("Monitoring fire and smoke events in the background")
//                .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
//                .setOngoing(true)
//                .setCategory(Notification.CATEGORY_SERVICE)
//                .build();
//
//        startForeground(1, notification);
//
//        return START_STICKY;
//    }
//
//    @Override
//    public void onDestroy() {
//        super.onDestroy();
//        stopForeground(true);
//    }
//
//    @Nullable
//    @Override
//    public IBinder onBind(Intent intent) {
//        return null;
//    }
//
//    private void createNotification() {
//        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            NotificationChannel serviceChannel = new NotificationChannel(
//                    CHANNEL_ID,
//                    "Fire Monitor Service",
//                    NotificationManager.IMPORTANCE_LOW
//            );
//            NotificationManager manager = getSystemService(NotificationManager.class);
//            if(manager != null) manager.createNotificationChannel(serviceChannel);
//        }
//    }
//}