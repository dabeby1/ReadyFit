
package com.example.readyfit;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

public class WorkoutReminderReceiver extends BroadcastReceiver {

    public static final String CHANNEL_ID = "readyfit_workout_reminders";

    @Override
    public void onReceive(Context context, Intent intent) {

        String reminderType = intent.getStringExtra("reminderType");

        createNotificationChannel(context);

        String title;
        String message;
        int notificationId;

        if ("before".equals(reminderType)) {

            title = "ReadyFit – מתכוננים לאימון 💪";
            message = "האימון שלך מתחיל בעוד 5 דקות. קדימה, הגיע הזמן לעבוד!";
            notificationId = 1800;

        } else {

            title = "ReadyFit – מחכים לך באימון";
            message = "השעה כבר אחרי 18:00 ועדיין לא התחלת. אל תוותר על התוכנית שלך!";
            notificationId = 1900;
        }

        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(context, CHANNEL_ID)
                        .setSmallIcon(android.R.drawable.ic_dialog_info)
                        .setContentTitle(title)
                        .setContentText(message)
                        .setStyle(new NotificationCompat.BigTextStyle()
                                .bigText(message))
                        .setPriority(NotificationCompat.PRIORITY_HIGH)
                        .setAutoCancel(true);

        try {

            NotificationManagerCompat.from(context)
                    .notify(notificationId, builder.build());

        } catch (SecurityException e) {
            e.printStackTrace();
        }
    }

    private void createNotificationChannel(Context context) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "תזכורות אימונים",
                    NotificationManager.IMPORTANCE_HIGH
            );

            channel.setDescription("תזכורות לאימונים של ReadyFit");

            NotificationManager manager =
                    context.getSystemService(NotificationManager.class);

            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }
    }
}
