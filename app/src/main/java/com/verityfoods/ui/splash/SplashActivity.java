package com.verityfoods.ui.splash;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import com.verityfoods.MainActivity;
import com.verityfoods.R;
import com.verityfoods.ui.auth.AuthChooser;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);


        int SPLASH_TIMEOUT = 3000;
        new Handler().postDelayed(() -> {
            // This method will be executed once the timer is over
            Intent i = new Intent(getApplicationContext(), AuthChooser.class);
            startActivity(i);
            finish();
        }, SPLASH_TIMEOUT);
    }
}