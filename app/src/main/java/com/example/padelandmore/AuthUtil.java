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
        verificarRol(context, "jugador", callback);
    }

    public static void isAdmin(final Context context, @NonNull final RoleCallback callback) {
        verificarRol(context, "admin", callback);
    }

    private static void verificarRol(final Context context, String rolObjetivo, @NonNull final RoleCallback callback) {
        String uid = Backend.getCurrentUid(context);
        if (uid == null) {
            callback.onResult(false);
            return;
        }

        String cachedRole = Backend.getCurrentRole(context);
        if (cachedRole != null) {
            callback.onResult(rolObjetivo.equalsIgnoreCase(cachedRole));
            refreshRoleFromServer(context, uid, callback, rolObjetivo);
            return;
        }

        refreshRoleFromServer(context, uid, callback, rolObjetivo);
    }

    private static void refreshRoleFromServer(Context context, String uid, @NonNull RoleCallback callback, String rolObjetivo) {
        java.util.concurrent.Executors.newSingleThreadExecutor().execute(() -> {
            String role = com.example.padelandmore.network.AWSConnection.obtenerRolUsuario(uid);
            if (role != null) {
                Backend.saveCurrentRole(context, role);
            }
            if (context instanceof android.app.Activity) {
                ((android.app.Activity) context).runOnUiThread(() -> {
                    callback.onResult(rolObjetivo.equalsIgnoreCase(role));
                });
            } else {
                callback.onResult(rolObjetivo.equalsIgnoreCase(role));
            }
        });
    }
}
