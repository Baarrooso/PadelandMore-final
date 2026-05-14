package com.example.padelandmore;

import android.content.Context;

import com.example.padelandmore.network.Backend;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.HashMap;
import java.util.Map;

public class FCMUtil {

    public static void guardarToken(Context context) {

        FirebaseMessaging.getInstance().getToken()
                .addOnSuccessListener(token -> {

                    String uid = Backend.getCurrentUid(context);
                    if (uid == null) return;

                    Map<String, Object> update = new HashMap<>();
                    update.put("fcmToken", token);
                    update.put("actualizado", System.currentTimeMillis());
                    Backend.updateCollectionItem(context, "users", uid, update, resp -> {}, t -> {});
                });
    }
}
