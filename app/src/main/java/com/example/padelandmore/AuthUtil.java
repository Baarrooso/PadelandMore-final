package com.example.padelandmore;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;

import com.example.padelandmore.network.Backend;

/**
 * Utilitario para obtener el rol del usuario desde SharedPreferences.
 * Migrado de Firebase Firestore → Backend REST / caché local.
 * Usa el backend y el campo "rol" (string) por convención.
 */
public class AuthUtil {

    public interface RoleCallback {
        void onResult(boolean isJugador);
    }

    private static final String PREFS    = "auth_prefs";
    private static final String KEY_ROLE = "user_role";

    public static void isJugador(final Context context, @NonNull final RoleCallback callback) {
        String uid = Backend.getCurrentUid(context);
        if (uid == null) {
            callback.onResult(false);
            return;
        }

        // Leer el rol cacheado en Backend
        String cachedRole = Backend.getCurrentRole(context);
        if (cachedRole != null) {
            callback.onResult("jugador".equalsIgnoreCase(cachedRole));
            // De todos modos, refrescar de fondo
            refreshRoleFromServer(context, uid, callback);
            return;
        }

        refreshRoleFromServer(context, uid, callback);
    }

    private static void refreshRoleFromServer(Context context, String uid, @NonNull RoleCallback callback) {
        Backend.getUserRole(context, uid, resp -> {
            Object roleValue = resp.get("rol");
            String role = roleValue == null ? null : roleValue.toString();
            if (role != null) {
                Backend.saveCurrentRole(context, role);
            }
            callback.onResult("jugador".equalsIgnoreCase(role));
        }, t -> callback.onResult(false));
    }
}
