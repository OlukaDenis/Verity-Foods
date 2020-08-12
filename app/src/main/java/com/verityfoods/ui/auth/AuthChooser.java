package com.verityfoods.ui.auth;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.material.button.MaterialButton;
import com.verityfoods.MainActivity;
import com.verityfoods.R;
import com.verityfoods.utils.Globals;
import com.verityfoods.utils.Vars;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class AuthChooser extends AppCompatActivity {
    private static final String TAG = "AuthChooser";
    private Vars vars;
    @BindView(R.id.auth_progress)
    ProgressBar loading;

    @BindView(R.id.choose_login)
    MaterialButton loginBtn;

    @BindView(R.id.choose_skip)
    TextView skip;

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

    public void checkUserProfile() {
        loading.setVisibility(View.VISIBLE);
        loginBtn.setEnabled(false);
        loginBtn.setBackgroundColor(getResources().getColor(R.color.bg));
        skip.setVisibility(View.GONE);

        vars.verityApp.db.collection(Globals.USERS)
                .document(vars.verityApp.mAuth.getCurrentUser().getUid())
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        startActivity(new Intent(getApplicationContext(), MainActivity.class));
                        finish();
                    } else {
                        startActivity(new Intent(getApplicationContext(), SignupActivity.class));
                        finish();
                    }
                });
    }

    @Override
    protected void onStart() {
        super.onStart();

        if (vars.isLoggedIn()) {
            checkUserProfile();
        }
    }
}