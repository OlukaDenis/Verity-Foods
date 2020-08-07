package com.verityfoods.ui.auth;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.verityfoods.R;

import butterknife.ButterKnife;
import butterknife.OnClick;

public class SignupActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);
        ButterKnife.bind(this);
    }

    @OnClick({R.id.signup_btn, R.id.location, R.id.already_logged_in})
    public void onItemClicked(View view) {
        switch (view.getId()) {
            case R.id.signup_btn:
                startActivity(new Intent(getApplicationContext(), LoginActivity.class));
                break;
            case R.id.location:
                Toast.makeText(getApplicationContext(), "Open maps", Toast.LENGTH_SHORT).show();
                break;
            case R.id.already_logged_in:
                startActivity(new Intent(this, LoginActivity.class));
                break;
        }

    }
}