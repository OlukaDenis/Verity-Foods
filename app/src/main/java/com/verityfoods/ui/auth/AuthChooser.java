package com.verityfoods.ui.auth;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.verityfoods.MainActivity;
import com.verityfoods.R;
import com.verityfoods.utils.Vars;

import butterknife.ButterKnife;
import butterknife.OnClick;

public class AuthChooser extends AppCompatActivity {
    private static final String TAG = "AuthChooser";
    private Vars vars;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auth_chooser);
        ButterKnife.bind(this);
        vars = new Vars(this);
    }

    @OnClick({R.id.choose_login, R.id.choose_skip})
    public void onButtonClicked(View view) {
        switch (view.getId()) {
            case R.id.choose_login:
                startActivity(new Intent(getApplicationContext(), SignupActivity.class));
                break;
            case R.id.choose_skip:
                startActivity(new Intent(getApplicationContext(), MainActivity.class));
                break;
        }

    }

    @Override
    protected void onStart() {
        super.onStart();

        if (vars.isLoggedIn()) {
            Log.d(TAG, "User UID: "+vars.verityApp.mAuth.getCurrentUser().getUid());
            startActivity(new Intent(getApplicationContext(), MainActivity.class));
            finish();
        }
    }
}