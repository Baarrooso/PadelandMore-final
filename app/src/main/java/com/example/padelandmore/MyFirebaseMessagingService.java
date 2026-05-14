package com.example.padelandmore;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    @Override
    public void onMessageReceived(RemoteMessage message) {

        String title = "Pádel&More";
        String body = "Tienes una nueva notificación";

        if (message.getData() != null && !message.getData().isEmpty()) {

            if (message.getData().get("title") != null) {
                title = message.getData().get("title");
            }

            if (message.getData().get("body") != null) {
                body = message.getData().get("body");
            }
        }

        mostrarNotificacion(title, body);
    }





    // 🔄 CUANDO CAMBIA EL TOKEN
    @Override
    public void onNewToken(String token) {
        super.onNewToken(token);
        FCMUtil.guardarToken(getApplicationContext());
    }

    private void mostrarNotificacion(String title, String body) {

        // 🔐 ANDROID 13+: si no hay permiso, no mostrar
        if (Build.VERSION.SDK_INT >= 33 &&
                checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS)
                        != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        String channelId = "padelmore_notifs";

        // 📢 CANAL ANDROID 8+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    channelId,
                    "Notificaciones Pádel&More",
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription("Notificaciones generales de Pádel&More");

            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) manager.createNotificationChannel(channel);
        }

        // 👉 AL TOCAR → ABRE LA APP
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);

        PendingIntent pendingIntent = PendingIntent.getActivity(
                this,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(this, channelId)
                        .setSmallIcon(android.R.drawable.ic_dialog_info)
                        .setContentTitle(title)
                        .setContentText(body)
                        .setContentIntent(pendingIntent)
                        .setAutoCancel(true)
                        .setPriority(NotificationCompat.PRIORITY_HIGH);

        NotificationManagerCompat.from(this)
                .notify((int) System.currentTimeMillis(), builder.build());
    }
}
