package com.example.smartfireapp;
//for android notification
import android.app.NotificationChannel;
import android.app.NotificationManager;
//allow notification click
import android.app.PendingIntent;

import android.content.Context;
import android.content.Intent;
//reads the sound file
import android.content.res.AssetFileDescriptor;
import android.media.AudioAttributes;
import android.media.MediaPlayer;

//backward compatible notifications
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

//helper class for notification with alarm sound
public class NotificationHelperService {

    private static final String CHANNEL_ID = "fire_alert_channel";

    //ensures only one alarm sound plays at a time
    private static MediaPlayer mediaPlayer;

    // FIRE ALERT notification
    public static void showCustomNotification(Context context, String message) {
        //ensures the channel exist before showing notification
        createNotificationChannel(context);

        //launch MainActivity when notification is tapped.
        Intent intent = new Intent(context, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                context, 0, intent,
                PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT
        );
        //using the same notification id(no spam) like primary key
        int notifId = 999;

//        builds notification UI
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_dialog_alert)
                .setContentTitle("🔥 FIRE ALERT!")
                .setContentText(message)//message from firebase
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setCategory(NotificationCompat.CATEGORY_ALARM)
                .setOngoing(true)   // non-dismissible
                .setAutoCancel(false) //doesnt disappear
                .setFullScreenIntent(pendingIntent, true)
                .setContentIntent(pendingIntent);

        //gets the system notification manager
        //replacing the old notification with ID 999
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        notificationManager.notify(notifId, builder.build());

        startAlarmSound(context);
    }

    // SAFE notification
    public static void showSafeNotification(Context context, String message) {
        createNotificationChannel(context);

        Intent intent = new Intent(context, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                context, 0, intent,
                PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT
        );

        int notifId = 999;

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle("Everything is Safe (⌐■_■)")
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(NotificationCompat.CATEGORY_STATUS)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        notificationManager.notify(notifId, builder.build());

        stopAlarm();
    }

    // STOP alarm only
    private static void stopAlarm() {
        if (mediaPlayer != null) {
            if (mediaPlayer.isPlaying()) mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }

    // Notification channel
    private static void createNotificationChannel(Context context) {
        NotificationManager notificationManager = context.getSystemService(NotificationManager.class);

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            //defines channel name, description, and importance level
            CharSequence name = "Fire Alert Channel";
            String description = "Channel for fire detection alerts";
            int importance = NotificationManager.IMPORTANCE_HIGH; // heads-up

            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            channel.enableVibration(true);
            channel.enableLights(true);
            channel.setSound(null, null); // custom sound handled separately

            notificationManager.createNotificationChannel(channel);
        }
    }

    // Alarm sound
    private static void startAlarmSound(Context context) {
        if (mediaPlayer == null) {
            mediaPlayer = new MediaPlayer();
            try {
                //open alarm sound
                AssetFileDescriptor afd = context.getResources().openRawResourceFd(R.raw.alertalarm);
                if (afd == null) return;

                mediaPlayer.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());
                afd.close();
                //it bypasses silent mode if allowed
                mediaPlayer.setAudioAttributes(new AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_ALARM)
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .build());

                //loops the alarm sound
                mediaPlayer.setLooping(true);
                mediaPlayer.prepare();
                mediaPlayer.start();
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if (!mediaPlayer.isPlaying()) {
            mediaPlayer.start();
        }
    }
}
