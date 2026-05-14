package com.example.tmanager;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;

import com.example.tmanager.network.Backend;

/**
 * Utilitario para obtener el rol del usuario desde SharedPreferences.
 * Migrado de Firebase Firestore → Backend REST / caché local.
 */
public class AuthUtil {

    public interface RoleCallback {
        void onResult(boolean isJugador);
    }

    private static final String PREFS    = "auth_prefs";
    private static final String KEY_ROLE = "user_role";

    public static void isJugador(final Context context, @NonNull final RoleCallback callback) {
        // El UID se guarda localmente en SharedPreferences cuando el usuario hace login/registro
        String uid = Backend.getCurrentUid(context);
        if (uid == null) {
            callback.onResult(false);
            return;
        }

        SharedPreferences prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        String cached = prefs.getString(KEY_ROLE + "_" + uid, null);

        if (cached != null) {
            callback.onResult("jugador".equalsIgnoreCase(cached));
        } else {
            // Por defecto se asigna rol "jugador" hasta que se obtenga el perfil completo
            callback.onResult(true);
        }
    }

    /**
     * Guarda el rol del usuario en caché local para consultas rápidas.
     */
    public static void saveRole(Context context, String uid, String role) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        prefs.edit().putString(KEY_ROLE + "_" + uid, role == null ? "jugador" : role).apply();
    }
}
