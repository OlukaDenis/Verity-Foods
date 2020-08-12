package com.verityfoods.utils;

import android.content.Context;
import android.content.SharedPreferences;

import com.verityfoods.VerityApp;
import com.verityfoods.data.model.User;

import java.util.Objects;
import java.util.UUID;

public class Vars {
    public Context context;
    public VerityApp verityApp;
    SharedPreferences idSharedPref;

    public Vars(Context context) {
        this.context = context;
        verityApp = (VerityApp) context.getApplicationContext();
        idSharedPref = context.getSharedPreferences(Globals.uniqueIdPrefs, Context.MODE_PRIVATE);
    }

    public void generateUserShoppingID() {
        String uniqueID = UUID.randomUUID().toString();
        if (!idSharedPref.contains(Globals.uniqueShoppingId)) {
            SharedPreferences.Editor editor = idSharedPref.edit();
            editor.putString(Globals.uniqueShoppingId, uniqueID);
            editor.apply();
        }
    }

    public String getShoppingID() {
        return  idSharedPref.getString(Globals.uniqueShoppingId, "");
    }

    public boolean isLoggedIn() {
        return verityApp.mAuth.getCurrentUser() != null;
    }


    public void getFirebaseToken() {

        if (!isLoggedIn()) return;

        Objects.requireNonNull(verityApp.mAuth.getCurrentUser()).getIdToken(true)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        String idToken = Objects.requireNonNull(task.getResult()).getToken();
                        assert idToken != null;
                        if (!idToken.isEmpty() && !idSharedPref.contains(Globals.accessToken)) {
                            SharedPreferences.Editor editor = idSharedPref.edit();
                            editor.putString(Globals.accessToken, idToken);
                            editor.apply();
                        }
                    }
                });

    }
}
