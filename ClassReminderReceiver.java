package com.vitpulse.app;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import androidx.core.app.NotificationCompat;

public class ClassReminderReceiver extends BroadcastReceiver {
    @Override public void onReceive(Context context, Intent intent) {
        NotificationScheduler.createChannel(context);
        String subject = intent.getStringExtra("subject");
        String building = intent.getStringExtra("building");
        String room = intent.getStringExtra("room");
        String slot = intent.getStringExtra("slot");
        String start = intent.getStringExtra("start");
        String body = start + " · " + building + "-" + room + " · Slot " + slot;

        Intent open = new Intent(context, MainActivity.class).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent content = PendingIntent.getActivity(context, 7, open,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder b = new NotificationCompat.Builder(context, NotificationScheduler.CHANNEL_ID)
                .setSmallIcon(com.vitpulse.app.R.drawable.ic_stat_vitpulse)
                .setContentTitle((subject == null ? "Class" : subject) + " starts soon")
                .setContentText(body)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(body))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(NotificationCompat.CATEGORY_REMINDER)
                .setAutoCancel(true)
                .setContentIntent(content);

        NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        int id = intent.getIntExtra("requestCode", (int)(System.currentTimeMillis() & 0x7fffffff));
        nm.notify(id, b.build());
    }
}
