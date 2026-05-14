package com.example.magazynieruz_mobile.service;

import android.app.Notification;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ServiceInfo;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.IBinder;
import android.telephony.SmsManager;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.example.magazynieruz_mobile.R;
import com.example.magazynieruz_mobile.util.NotificationHelper;

public class FallDetectionService extends Service implements SensorEventListener {

    private static final int FOREGROUND_ID = 4242;
    private static final int FALL_NOTIF_ID = 4243;
    private static final float FREE_FALL_THRESHOLD = 3.0f;
    private static final float IMPACT_THRESHOLD = 25.0f;
    private static final long IMPACT_WINDOW_MS = 1500L;

    private SensorManager sensorManager;
    private Sensor accelerometer;
    private long freeFallAt = 0L;
    private long lastAlarmAt = 0L;

    @Override
    public void onCreate() {
        super.onCreate();
        NotificationHelper.ensureChannels(this);
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                startForeground(FOREGROUND_ID, buildForegroundNotification(),
                        ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC);
            } else {
                startForeground(FOREGROUND_ID, buildForegroundNotification());
            }
        } catch (Exception e) {
            Log.e("FallDetection", "startForeground failed", e);
            stopSelf();
            return;
        }

        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        if (sensorManager != null) {
            accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
            if (accelerometer != null) {
                sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_GAME);
            } else {
                Log.w("FallDetection", "No accelerometer available");
                stopSelf();
            }
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        if (sensorManager != null) sensorManager.unregisterListener(this);
        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() != Sensor.TYPE_ACCELEROMETER) return;
        float x = event.values[0];
        float y = event.values[1];
        float z = event.values[2];
        double magnitude = Math.sqrt(x * x + y * y + z * z);

        long now = System.currentTimeMillis();
        if (magnitude < FREE_FALL_THRESHOLD) {
            freeFallAt = now;
        } else if (magnitude > IMPACT_THRESHOLD
                && freeFallAt != 0
                && now - freeFallAt < IMPACT_WINDOW_MS
                && now - lastAlarmAt > 5000) {
            lastAlarmAt = now;
            freeFallAt = 0;
            triggerFallAlarm();
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {}

    private void triggerFallAlarm() {
        NotificationHelper.postFallAlarm(this, FALL_NOTIF_ID);

        SharedPreferences prefs = getSharedPreferences("magazynier_prefs", 0);
        if (prefs.getBoolean("sms", true)) {
            String number = prefs.getString("sms_number", "");
            if (!number.isEmpty()) {
                try {
                    SmsManager.getDefault().sendTextMessage(
                            number, null,
                            "MagazynierUZ: Wykryto upadek urządzenia.",
                            null, null);
                } catch (SecurityException ignored) {
                }
            }
        }
    }

    private Notification buildForegroundNotification() {
        return new NotificationCompat.Builder(this, NotificationHelper.CHANNEL_GENERAL)
                .setSmallIcon(R.drawable.ic_notifications)
                .setContentTitle("Detekcja upadku aktywna")
                .setContentText("Monitorowanie czujnika ruchu w tle")
                .setOngoing(true)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .build();
    }
}
