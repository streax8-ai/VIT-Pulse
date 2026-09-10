package com.vitpulse.app;

import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import java.util.Calendar;
import java.util.Locale;
import org.json.JSONArray;
import org.json.JSONObject;

public final class NotificationScheduler {
    public static final String CHANNEL_ID = "class_reminders";
    public static final String ACTION_CLASS_REMINDER = "com.vitpulse.app.CLASS_REMINDER";
    private static final String PREFS = "vitpulse_notifications";
    private static final String KEY_JSON = "schedule_json";
    private static final String KEY_MINS = "reminder_mins";
    private static final int DAYS_TO_SCHEDULE = 21;

    private NotificationScheduler() {}

    public static void createChannel(Context context) {
        if (Build.VERSION.SDK_INT >= 26) {
            NotificationManager nm = context.getSystemService(NotificationManager.class);
            NotificationChannel ch = new NotificationChannel(CHANNEL_ID, "Class reminders", NotificationManager.IMPORTANCE_HIGH);
            ch.setDescription("Notifications before your VIT Pulse classes");
            nm.createNotificationChannel(ch);
        }
    }

    public static void saveAndSchedule(Context context, String json, int mins) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit()
                .putString(KEY_JSON, json).putInt(KEY_MINS, mins).apply();
        cancelAll(context);
        schedule(context, json, mins);
    }

    public static void rescheduleSaved(Context context) {
        String json = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getString(KEY_JSON, "[]");
        int mins = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getInt(KEY_MINS, 5);
        if (!json.equals("[]")) schedule(context, json, mins);
    }

    public static void clear(Context context) {
        cancelAll(context);
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().clear().apply();
    }

    private static void schedule(Context context, String json, int mins) {
        try {
            JSONArray classes = new JSONArray(json);
            AlarmManager alarm = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            Calendar today = Calendar.getInstance();
            today.set(Calendar.HOUR_OF_DAY, 0); today.set(Calendar.MINUTE, 0); today.set(Calendar.SECOND, 0); today.set(Calendar.MILLISECOND, 0);

            for (int dayOffset = 0; dayOffset < DAYS_TO_SCHEDULE; dayOffset++) {
                Calendar day = (Calendar) today.clone();
                day.add(Calendar.DAY_OF_YEAR, dayOffset);
                int dow = day.get(Calendar.DAY_OF_WEEK); // Sunday=1
                for (int i = 0; i < classes.length(); i++) {
                    JSONObject c = classes.getJSONObject(i);
                    if (c.getInt("dow") != dow) continue;
                    String[] hm = c.getString("start").split(":");
                    Calendar fire = (Calendar) day.clone();
                    fire.set(Calendar.HOUR_OF_DAY, Integer.parseInt(hm[0]));
                    fire.set(Calendar.MINUTE, Integer.parseInt(hm[1]));
                    fire.set(Calendar.SECOND, 0); fire.set(Calendar.MILLISECOND, 0);
                    fire.add(Calendar.MINUTE, -mins);
                    if (fire.getTimeInMillis() <= System.currentTimeMillis()) continue;

                    int requestCode = uniqueCode(dayOffset, i, c.getString("start"));
                    Intent intent = new Intent(context, ClassReminderReceiver.class);
                    intent.setAction(ACTION_CLASS_REMINDER);
                    intent.putExtra("subject", c.getString("subject"));
                    intent.putExtra("building", c.getString("building"));
                    intent.putExtra("room", c.getString("room"));
                    intent.putExtra("slot", c.getString("slot"));
                    intent.putExtra("start", c.getString("start"));
                    intent.putExtra("requestCode", requestCode);
                    PendingIntent pi = PendingIntent.getBroadcast(context, requestCode, intent,
                            PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
                    alarm.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, fire.getTimeInMillis(), pi);
                }
            }
        } catch (Exception ignored) {}
    }

    private static void cancelAll(Context context) {
        AlarmManager alarm = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        String json = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getString(KEY_JSON, "[]");
        try {
            JSONArray classes = new JSONArray(json);
            for (int dayOffset = 0; dayOffset < DAYS_TO_SCHEDULE; dayOffset++) {
                for (int i = 0; i < classes.length(); i++) {
                    JSONObject c = classes.getJSONObject(i);
                    int code = uniqueCode(dayOffset, i, c.getString("start"));
                    Intent intent = new Intent(context, ClassReminderReceiver.class).setAction(ACTION_CLASS_REMINDER);
                    PendingIntent pi = PendingIntent.getBroadcast(context, code, intent,
                            PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
                    alarm.cancel(pi); pi.cancel();
                }
            }
        } catch (Exception ignored) {}
    }

    private static int uniqueCode(int dayOffset, int index, String start) {
        return Math.abs((dayOffset + 1) * 100000 + index * 1000 + start.hashCode() % 997);
    }
}
