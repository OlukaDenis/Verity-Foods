package com.verityfoods;

import android.app.Application;

import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.storage.FirebaseStorage;

public class VerityApp extends Application {
    public static VerityApp mInstance;
    public FirebaseAnalytics mFirebaseAnalytics;
    public FirebaseMessaging mFirebaseMessagin;
    public FirebaseAuth mAuth;
    public FirebaseFirestore db;
    public String firebaseid;
    public FirebaseStorage mStorageRef;
    public FirebaseCrashlytics crashlytics;

    public static synchronized VerityApp getInstance() {
        return mInstance;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        mInstance = this;

        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
        mFirebaseMessagin = FirebaseMessaging.getInstance();
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        mStorageRef = FirebaseStorage.getInstance();
        crashlytics = FirebaseCrashlytics.getInstance();

        FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder()
                .setPersistenceEnabled(true)
                .setSslEnabled(true)
                .build();
        db.setFirestoreSettings(settings);

//        mFirebaseAnalytics.setUserId(mAuth.getUid());
//        Places.initialize(getApplicationContext(), getString(R.string.google_maps_key));
    }
}
