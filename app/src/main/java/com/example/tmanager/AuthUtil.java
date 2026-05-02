package com.example.tmanager;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

/**
 * Utilitario sencillo para obtener el rol del usuario y cachearlo localmente.
 * Usa la colección "usuarios" y el campo "rol" (string) por convención.
 */
public class AuthUtil {

    public interface RoleCallback {
        void onResult(boolean isJugador);
    }

    private static final String PREFS = "auth_prefs";
    private static final String KEY_ROLE = "user_role";

    public static void isJugador(final Context context, @NonNull final RoleCallback callback) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            // No logged user -> not jugador by default
            callback.onResult(false);
            return;
        }

        SharedPreferences prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        String cached = prefs.getString(KEY_ROLE + "_" + user.getUid(), null);
        if (cached != null) {
            callback.onResult("jugador".equalsIgnoreCase(cached));
            // Also refresh in background
            refreshRoleFromServer(user.getUid(), prefs, callback);
            return;
        }

        // No cache -> fetch from Firestore
        FirebaseFirestore.getInstance()
                .collection("usuarios")
                .document(user.getUid())
                .get()
                .addOnSuccessListener((DocumentSnapshot doc) -> {
                    if (doc != null && doc.exists()) {
                        String role = doc.getString("rol");
                        if (role == null) role = doc.getString("role");
                        prefs.edit().putString(KEY_ROLE + "_" + user.getUid(), role == null ? "" : role).apply();
                        callback.onResult("jugador".equalsIgnoreCase(role));
                    } else {
                        callback.onResult(false);
                    }
                })
                .addOnFailureListener(e -> callback.onResult(false));
    }

    private static void refreshRoleFromServer(String uid, SharedPreferences prefs, @NonNull RoleCallback callback) {
        FirebaseFirestore.getInstance()
                .collection("usuarios")
                .document(uid)
                .get()
                .addOnSuccessListener((DocumentSnapshot doc) -> {
                    if (doc != null && doc.exists()) {
                        String role = doc.getString("rol");
                        if (role == null) role = doc.getString("role");
                        prefs.edit().putString(KEY_ROLE + "_" + uid, role == null ? "" : role).apply();
                        callback.onResult("jugador".equalsIgnoreCase(role));
                    }
                });
    }
}

