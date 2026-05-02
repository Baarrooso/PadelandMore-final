package com.example.tmanager;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;

import androidx.annotation.Nullable;

import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.firebase.auth.FirebaseAuth;

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

    public static void signOutToLogin(Context context, FirebaseAuth auth) {
        if (auth != null) {
            auth.signOut();
        }
        goToLoginClean(context);
    }

    public static void signOutGoogleToLogin(Context context,
                                           FirebaseAuth auth,
                                           @Nullable GoogleSignInClient googleClient) {
        signOutGoogleAndGoTo(context, auth, googleClient, LoginActivity.class);
    }

    public static void signOutGoogleAndGoTo(Context context,
                                           FirebaseAuth auth,
                                           @Nullable GoogleSignInClient googleClient,
                                           Class<?> destination) {
        if (auth != null) {
            auth.signOut();
        }
        if (googleClient != null) {
            googleClient.revokeAccess();
            googleClient.signOut();
        }
        goToClean(context, destination);
    }
}

