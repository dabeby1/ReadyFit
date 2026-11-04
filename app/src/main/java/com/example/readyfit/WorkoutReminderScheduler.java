
package com.example.readyfit;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class WorkoutReminderScheduler {

    private static final int BEFORE_ID = 1800;
    private static final int MISSED_ID = 1900;

    private static final String PREFS_NAME = "ReadyFitReminders";
    private static final String COMPLETED_DATE = "completedDate";

    public static void scheduleDailyReminders(Context context) {

        if (isWorkoutCompletedToday(context)) {
            cancelReminders(context);
            return;
        }

        scheduleReminder(context, BEFORE_ID, 17, 55, "before");
        scheduleReminder(context, MISSED_ID, 19, 0, "missed");
    }

    public static void markWorkoutCompleted(Context context) {

        String today = getTodayDate();

        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .edit()
                .putString(COMPLETED_DATE, today)
                .apply();

        cancelReminders(context);
    }

    public static boolean isWorkoutCompletedToday(Context context) {

        String savedDate = context.getSharedPreferences(
                PREFS_NAME,
                Context.MODE_PRIVATE
        ).getString(COMPLETED_DATE, "");

        return getTodayDate().equals(savedDate);
    }

    private static String getTodayDate() {

        return new SimpleDateFormat(
                "yyyyMMdd",
                Locale.getDefault()
        ).format(new Date());
    }

    private static void scheduleReminder(
            Context context,
            int requestCode,
            int hour,
            int minute,
            String reminderType) {

        AlarmManager alarmManager =
                (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        if (alarmManager == null) {
            return;
        }

        Intent intent = new Intent(context, WorkoutReminderReceiver.class);
        intent.putExtra("reminderType", reminderType);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                requestCode,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, minute);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        if (calendar.getTimeInMillis() <= System.currentTimeMillis()) {
            calendar.add(Calendar.DAY_OF_YEAR, 1);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
                && !alarmManager.canScheduleExactAlarms()) {

            alarmManager.setAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    calendar.getTimeInMillis(),
                    pendingIntent
            );

        } else {

            alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    calendar.getTimeInMillis(),
                    pendingIntent
            );
        }
    }

    public static void cancelReminders(Context context) {

        AlarmManager alarmManager =
                (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        cancelReminder(context, alarmManager, BEFORE_ID, "before");
        cancelReminder(context, alarmManager, MISSED_ID, "missed");
    }

    private static void cancelReminder(
            Context context,
            AlarmManager alarmManager,
            int requestCode,
            String reminderType) {

        Intent intent = new Intent(context, WorkoutReminderReceiver.class);
        intent.putExtra("reminderType", reminderType);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                requestCode,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        if (alarmManager != null) {
            alarmManager.cancel(pendingIntent);
        }

        pendingIntent.cancel();
    }
}
