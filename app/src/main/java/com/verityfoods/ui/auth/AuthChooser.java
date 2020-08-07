package com.verityfoods.ui.auth;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.verityfoods.R;

import butterknife.ButterKnife;
import butterknife.OnClick;

public class AuthChooser extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auth_chooser);
        ButterKnife.bind(this);
    }

    @OnClick({R.id.choose_login, R.id.choose_signup})
    public void onButtonClicked(View view) {
        switch (view.getId()) {
            case R.id.choose_login:
                startActivity(new Intent(getApplicationContext(), LoginActivity.class));
                break;
            case R.id.choose_signup:
                startActivity(new Intent(getApplicationContext(), SignupActivity.class));
                break;
        }

    }
}