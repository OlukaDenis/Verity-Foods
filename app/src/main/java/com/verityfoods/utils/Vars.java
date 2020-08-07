package com.verityfoods.utils;

import android.content.Context;

import com.verityfoods.VerityApp;

public class Vars {
    public Context context;
    public VerityApp verityApp;

    public Vars(Context context) {
        this.context = context;
        verityApp = (VerityApp) context.getApplicationContext();
    }
}
