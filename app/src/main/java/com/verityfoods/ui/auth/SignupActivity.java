package com.verityfoods.ui.auth;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.verityfoods.MainActivity;
import com.verityfoods.R;
import com.verityfoods.data.model.User;
import com.verityfoods.utils.Globals;
import com.verityfoods.utils.Vars;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class SignupActivity extends AppCompatActivity {
    private static final String TAG = "SignupActivity";
    private static String VERIFY = "verify number";
    private static String CONTINUE = "continue";
    private static String SAVE = "save";

    private Vars vars;

    @BindView(R.id.step_phone)
    LinearLayout stepPhoneLayout;

    @BindView(R.id.step_code)
    LinearLayout stepCodeLayout;

    @BindView(R.id.step_info)
    LinearLayout stepInfoLayout;

    @BindView(R.id.signup_phone)
    TextInputEditText phone;

    @BindView(R.id.code)
    TextInputEditText code;

    @BindView(R.id.name)
    TextInputEditText userName;

    @BindView(R.id.email)
    TextInputEditText userEmail;

    @BindView(R.id.location)
    AutoCompleteTextView userAddress;

    @BindView(R.id.signup_btn)
    MaterialButton signUpBtn;

    private String verificationID;
    private PhoneAuthProvider.ForceResendingToken token;
    private ProgressDialog progressDialog;
    private User user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);
        ButterKnife.bind(this);
        vars = new Vars(this);
        progressDialog = new ProgressDialog(this);
        user = new User();
    }

    @OnClick({R.id.signup_btn, R.id.location, R.id.already_logged_in})
    public void onItemClicked(View view) {
        switch (view.getId()) {

            case R.id.signup_btn:
                if (signUpBtn.getText().toString().equalsIgnoreCase(CONTINUE)) {
                    startOTPRequest();
                }
                break;

            case R.id.already_logged_in:
                startActivity(new Intent(this, LoginActivity.class));
                break;
            default:
                break;
        }

    }


    public void  startOTPRequest() {
        if (!phone.getText().toString().isEmpty() && phone.getText().toString().length() == 9) {
            String contact = "+256" + phone.getText().toString();
            progressDialog.setMessage("Sending OTP ...");
            progressDialog.show();
            vars.verityApp.phoneAuth.verifyPhoneNumber(
                    contact,
                    60L,
                    TimeUnit.SECONDS,
                    this,
                    new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

                        @Override
                        public void onCodeSent(@NonNull String s, @NonNull PhoneAuthProvider.ForceResendingToken forceResendingToken) {
                            super.onCodeSent(s, forceResendingToken);
                            progressDialog.dismiss();
                            verificationID = s;
                            token = forceResendingToken;
                            displayCodeLayout();
                            Toast.makeText(SignupActivity.this, "OTP sent", Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onCodeAutoRetrievalTimeOut(@NonNull String s) {
                            super.onCodeAutoRetrievalTimeOut(s);
                        }

                        @Override
                        public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {
                            
                        }

                        @Override
                        public void onVerificationFailed(@NonNull FirebaseException e) {
                            vars.verityApp.crashlytics.recordException(e);
                            vars.verityApp.crashlytics.log("Phone verification failed" + e.getMessage());
                            Log.d(TAG, "Phone verification failed: ", e);
                        }
                    });
        } else {
            phone.setError("Invalid phone number");
            phone.requestFocus();
        }
    }

    public void checkUserProfile() {
        vars.verityApp.db.collection(Globals.USERS)
                .document(vars.verityApp.mAuth.getCurrentUser().getUid())
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        startActivity(new Intent(getApplicationContext(), MainActivity.class));
                        finish();
                        progressDialog.dismiss();
                    } else {
                        progressDialog.dismiss();
                        displayInfoLayout();
                    }
                });
    }

    private void verifyOTP() {
        progressDialog.setMessage("Verifying OTP ...");
        progressDialog.show();

        String userOTP = Objects.requireNonNull(code.getText()).toString();
        if (!userOTP.isEmpty() && userOTP.length() == 6) {
            PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationID, userOTP);
            vars.verityApp.mAuth.signInWithCredential(credential)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            checkUserProfile();
                            Toast.makeText(this, "Phone verification is successful", Toast.LENGTH_SHORT).show();
                        } else {
                            progressDialog.dismiss();
                            displayPhoneLayout();
                            Toast.makeText(this, "Phone authentication failed, please try again", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(e -> {
                        progressDialog.dismiss();
                        Toast.makeText(this, "Verification process failed", Toast.LENGTH_SHORT).show();
                        Log.e(TAG, "verifyOTP error: ",e);
                        vars.verityApp.crashlytics.recordException(e);
                    });
        } else {
            code.setError("OTP is invalid");
        }
    }

    public void displayCodeLayout() {
        stepPhoneLayout.setVisibility(View.GONE);
        stepCodeLayout.setVisibility(View.VISIBLE);
        signUpBtn.setText(VERIFY);
        signUpBtn.setOnClickListener(view -> {
            if (signUpBtn.getText().toString().equalsIgnoreCase(VERIFY)) {
                verifyOTP();
            }
        });
    }


    public void displayInfoLayout() {
        stepCodeLayout.setVisibility(View.GONE);
        stepInfoLayout.setVisibility(View.VISIBLE);
        signUpBtn.setText(SAVE);
        signUpBtn.setOnClickListener(view -> {
            if (signUpBtn.getText().toString().equalsIgnoreCase(SAVE)) {
               saveUserDetails();
            }
        });
    }

    private void saveUserDetails() {
        String userID = Objects.requireNonNull(vars.verityApp.mAuth.getCurrentUser()).getUid();
        if (!userEmail.getText().toString().isEmpty() &&
        !userAddress.getText().toString().isEmpty() &&
        !userName.getText().toString().isEmpty()) {
            user.setName(userName.getText().toString());
            user.setUserID(userID);
            user.setPhone(vars.verityApp.mAuth.getCurrentUser().getPhoneNumber());
            user.setAddress(userAddress.getText().toString());
            user.setEmail(userEmail.getText().toString());

            vars.verityApp.db.collection(Globals.USERS)
                    .document(userID)
                    .set(user)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            startActivity(new Intent(getApplicationContext(), MainActivity.class));
                            finish();
                            Toast.makeText(this, "Your details are successfully saved", Toast.LENGTH_SHORT).show();

                        } else {
                            Toast.makeText(this, "Registration unsuccessful", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(e -> {
                        vars.verityApp.crashlytics.recordException(e);
                        Log.e(TAG, "Error while saving data: ",e);
                    });

        } else {
            Toast.makeText(this, "All fields are required", Toast.LENGTH_SHORT).show();
            return;
        }
    }

    public void displayPhoneLayout() {
        stepCodeLayout.setVisibility(View.GONE);
        stepInfoLayout.setVisibility(View.GONE);
        stepPhoneLayout.setVisibility(View.VISIBLE);
        signUpBtn.setText(CONTINUE);
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (vars.isLoggedIn()) {
            checkUserProfile();
        }
    }
}