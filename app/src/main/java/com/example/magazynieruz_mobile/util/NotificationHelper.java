package com.example.magazynieruz_mobile.util;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.core.app.NotificationCompat;

import com.example.magazynieruz_mobile.MainActivity;
import com.example.magazynieruz_mobile.R;

public final class NotificationHelper {

    public static final String CHANNEL_GENERAL = "magazynier_general";
    public static final String CHANNEL_FALL = "magazynier_fall";

    private NotificationHelper() {}

    public static void ensureChannels(Context context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return;
        NotificationManager nm = context.getSystemService(NotificationManager.class);
        if (nm == null) return;

        NotificationChannel general = new NotificationChannel(
                CHANNEL_GENERAL, "Powiadomienia ogólne", NotificationManager.IMPORTANCE_DEFAULT);
        general.setDescription("Ogólne powiadomienia aplikacji");

        NotificationChannel fall = new NotificationChannel(
                CHANNEL_FALL, "Alarm upadku", NotificationManager.IMPORTANCE_HIGH);
        fall.setDescription("Wykrywanie upadku urządzenia");

        nm.createNotificationChannel(general);
        nm.createNotificationChannel(fall);
    }

    public static void postPush(Context context, int id, String title, String text) {
        ensureChannels(context);
        PendingIntent pi = PendingIntent.getActivity(
                context, 0,
                new Intent(context, MainActivity.class),
                PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder b = new NotificationCompat.Builder(context, CHANNEL_GENERAL)
                .setSmallIcon(R.drawable.ic_notifications)
                .setContentTitle(title)
                .setContentText(text)
                .setContentIntent(pi)
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        NotificationManager nm = context.getSystemService(NotificationManager.class);
        if (nm != null) nm.notify(id, b.build());
    }

    public static void postFallAlarm(Context context, int id) {
        ensureChannels(context);
        NotificationCompat.Builder b = new NotificationCompat.Builder(context, CHANNEL_FALL)
                .setSmallIcon(R.drawable.ic_notifications)
                .setContentTitle("Wykryto upadek!")
                .setContentText("Urządzenie zarejestrowało gwałtowny ruch / upadek.")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(NotificationCompat.CATEGORY_ALARM)
                .setAutoCancel(true);

        NotificationManager nm = context.getSystemService(NotificationManager.class);
        if (nm != null) nm.notify(id, b.build());
    }
}
