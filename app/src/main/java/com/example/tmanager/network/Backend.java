package com.example.tmanager.network;

import android.content.Context;
import android.content.SharedPreferences;
import android.widget.Toast;

import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class Backend {

    private static final String PREFS = "tmanager_prefs";
    private static final String KEY_UID = "current_uid";

    public interface SuccessCallback {
        void onSuccess(Map<String, Object> resp);
    }

    public interface FailureCallback {
        void onFailure(Throwable t);
    }

    public static void register(Context ctx, Map<String, Object> body, SuccessCallback onSuccess, FailureCallback onFailure) {
        ApiClient.getService().register(body).enqueue(new Callback<Map<String, Object>>() {
            @Override
            public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Map<String, Object> b = response.body();
                    // store uid if present
                    if (b.containsKey("uid")) saveCurrentUid(ctx, b.get("uid").toString());
                    onSuccess.onSuccess(b);
                } else {
                    onFailure.onFailure(new Exception("Registro fallido"));
                }
            }

            @Override
            public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                onFailure.onFailure(t);
            }
        });
    }

    public static void login(Context ctx, Map<String, Object> body, SuccessCallback onSuccess, FailureCallback onFailure) {
        ApiClient.getService().login(body).enqueue(new Callback<Map<String, Object>>() {
            @Override
            public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Map<String, Object> b = response.body();
                    if (b.containsKey("uid")) saveCurrentUid(ctx, b.get("uid").toString());
                    onSuccess.onSuccess(b);
                } else {
                    onFailure.onFailure(new Exception("Login fallido"));
                }
            }

            @Override
            public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                onFailure.onFailure(t);
            }
        });
    }

    public static void postToCollection(Context ctx, String collection, Map<String, Object> body, SuccessCallback onSuccess, FailureCallback onFailure) {
        ApiClient.getService().postToCollection(collection, body).enqueue(new Callback<Map<String, Object>>() {
            @Override
            public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    onSuccess.onSuccess(response.body());
                } else {
                    onFailure.onFailure(new Exception("Error al guardar en backend"));
                }
            }

            @Override
            public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                onFailure.onFailure(t);
            }
        });
    }

    public static void saveCurrentUid(Context ctx, String uid) {
        SharedPreferences prefs = ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        prefs.edit().putString(KEY_UID, uid).apply();
    }

    public static String getCurrentUid(Context ctx) {
        SharedPreferences prefs = ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        return prefs.getString(KEY_UID, null);
    }

    public static void logout(Context ctx) {
        saveCurrentUid(ctx, null);
        Toast.makeText(ctx, "Sesión cerrada", Toast.LENGTH_SHORT).show();
    }
}

