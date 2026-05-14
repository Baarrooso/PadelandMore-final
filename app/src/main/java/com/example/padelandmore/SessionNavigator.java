package com.example.padelandmore;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;

import androidx.annotation.Nullable;

import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.example.padelandmore.network.Backend;

public final class SessionNavigator {

    private SessionNavigator() {
    }

    public static void goToLoginClean(Context context) {
        goToClean(context, LoginActivity.class);
    }

    public static void goToRegisterClean(Context context) {
        goToClean(context, RegisterActivity.class);
    }

    public static void goToClean(Context context, Class<?> destination) {
        Intent intent = new Intent(context, destination);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        context.startActivity(intent);

        if (context instanceof Activity) {
            ((Activity) context).finish();
        }
    }

    public static void signOutToLogin(Context context) {
        Backend.logout(context);
        goToLoginClean(context);
    }

    public static void signOutGoogleToLogin(Context context,
                                           @Nullable GoogleSignInClient googleClient) {
        signOutGoogleAndGoTo(context, googleClient, LoginActivity.class);
    }

    public static void signOutGoogleAndGoTo(Context context,
                                           @Nullable GoogleSignInClient googleClient,
                                           Class<?> destination) {
        if (googleClient != null) {
            googleClient.revokeAccess();
            googleClient.signOut();
        }
        Backend.logout(context);
        goToClean(context, destination);
    }
}

