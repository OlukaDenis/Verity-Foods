package com.verityfoods.ui.splash;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import com.verityfoods.MainActivity;
import com.verityfoods.R;
import com.verityfoods.ui.auth.AuthChooser;
import com.verityfoods.utils.Vars;

import static com.verityfoods.utils.Globals.SPLASH_TIMEOUT;

public class SplashActivity extends AppCompatActivity {
    private Vars vars;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        vars = new Vars(this);

        //generating a unique shopping id
        vars.generateUserShoppingID();

        new Handler().postDelayed(() -> {
            // This method will be executed once the timer is over
            Intent i = new Intent(getApplicationContext(), MainActivity.class);
            startActivity(i);
            finish();
        }, SPLASH_TIMEOUT);
    }
}