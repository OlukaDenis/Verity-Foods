package com.verityfoods.ui.auth;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.verityfoods.MainActivity;
import com.verityfoods.R;

import butterknife.ButterKnife;
import butterknife.OnClick;

public class LoginActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        ButterKnife.bind(this);
    }

    @OnClick({R.id.login_btn, R.id.customer_login, R.id.admin_login, R.id.forgot_password})
    public void onItemClicked(View view) {
        switch (view.getId()) {
            case R.id.login_btn:
                startActivity(new Intent(getApplicationContext(), MainActivity.class));
                break;
            case R.id.customer_login:
                Toast.makeText(getApplicationContext(), "Customer", Toast.LENGTH_SHORT).show();
                break;
            case R.id.admin_login:
                Toast.makeText(getApplicationContext(), "Admin", Toast.LENGTH_SHORT).show();
                break;
            case R.id.forgot_password:
                Toast.makeText(getApplicationContext(), "Forgot", Toast.LENGTH_SHORT).show();
                break;
        }

    }
}