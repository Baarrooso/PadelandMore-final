package com.example.padelandmore.network;

import android.content.Context;
import android.content.SharedPreferences;
import android.widget.Toast;

import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class Backend {

    private static final String PREFS = "padelmore_prefs";
    private static final String KEY_UID = "current_uid";
    private static final String KEY_ROLE = "current_role";

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
                    Object uid = b.get("uid");
                    if (uid != null) {
                        saveCurrentUid(ctx, uid.toString());
                    }
                    Object user = b.get("user");
                    if (user instanceof Map) {
                        Object role = ((Map<?, ?>) user).get("rol");
                        if (role != null) {
                            saveCurrentRole(ctx, role.toString());
                        }
                    }
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
                    Object uid = b.get("uid");
                    if (uid != null) {
                        saveCurrentUid(ctx, uid.toString());
                    }
                    Object user = b.get("user");
                    if (user instanceof Map) {
                        Object role = ((Map<?, ?>) user).get("rol");
                        if (role != null) {
                            saveCurrentRole(ctx, role.toString());
                        }
                    }
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

    public static void getCollection(Context ctx, String collection, SuccessCallback onSuccess, FailureCallback onFailure) {
        ApiClient.getService().getCollection(collection).enqueue(new Callback<Map<String, Object>>() {
            @Override
            public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    onSuccess.onSuccess(response.body());
                } else {
                    onFailure.onFailure(new Exception("No se pudo obtener la coleccion"));
                }
            }

            @Override
            public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                onFailure.onFailure(t);
            }
        });
    }

    public static void updateCollectionItem(Context ctx, String collection, String id, Map<String, Object> body, SuccessCallback onSuccess, FailureCallback onFailure) {
        ApiClient.getService().updateCollectionItem(collection, id, body).enqueue(new Callback<Map<String, Object>>() {
            @Override
            public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    onSuccess.onSuccess(response.body());
                } else {
                    onFailure.onFailure(new Exception("No se pudo actualizar el registro"));
                }
            }

            @Override
            public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                onFailure.onFailure(t);
            }
        });
    }

    public static void deleteCollectionItem(Context ctx, String collection, String id, SuccessCallback onSuccess, FailureCallback onFailure) {
        ApiClient.getService().deleteCollectionItem(collection, id).enqueue(new Callback<Map<String, Object>>() {
            @Override
            public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    onSuccess.onSuccess(response.body());
                } else {
                    onFailure.onFailure(new Exception("No se pudo borrar el registro"));
                }
            }

            @Override
            public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                onFailure.onFailure(t);
            }
        });
    }

    public static void getUser(Context ctx, String uid, SuccessCallback onSuccess, FailureCallback onFailure) {
        ApiClient.getService().getUser(uid).enqueue(new Callback<Map<String, Object>>() {
            @Override
            public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    onSuccess.onSuccess(response.body());
                } else {
                    onFailure.onFailure(new Exception("No se pudo obtener el usuario"));
                }
            }

            @Override
            public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                onFailure.onFailure(t);
            }
        });
    }

    public static void getUserRole(Context ctx, String uid, SuccessCallback onSuccess, FailureCallback onFailure) {
        ApiClient.getService().getUserRole(uid).enqueue(new Callback<Map<String, Object>>() {
            @Override
            public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Map<String, Object> body = response.body();
                    Object role = body.get("rol");
                    if (role != null) {
                        saveCurrentRole(ctx, role.toString());
                    }
                    onSuccess.onSuccess(body);
                } else {
                    onFailure.onFailure(new Exception("No se pudo obtener el rol"));
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

    public static void saveCurrentRole(Context ctx, String role) {
        SharedPreferences prefs = ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        prefs.edit().putString(KEY_ROLE, role).apply();
    }

    public static String getCurrentUid(Context ctx) {
        SharedPreferences prefs = ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        return prefs.getString(KEY_UID, null);
    }

    public static String getCurrentRole(Context ctx) {
        SharedPreferences prefs = ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        return prefs.getString(KEY_ROLE, null);
    }

    public static void logout(Context ctx) {
        saveCurrentUid(ctx, null);
        saveCurrentRole(ctx, null);
        Toast.makeText(ctx, "Sesión cerrada", Toast.LENGTH_SHORT).show();
    }
}

