package com.verityfoods.ui.auth;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.api.Status;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.widget.Autocomplete;
import com.google.android.libraries.places.widget.AutocompleteActivity;
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode;
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

import java.util.Arrays;
import java.util.List;
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
    TextView userAddress;

    @BindView(R.id.signup_btn)
    MaterialButton signUpBtn;

    private String verificationID;
    private PhoneAuthProvider.ForceResendingToken token;
    private ProgressDialog progressDialog;
    private User user;
    private LocationManager locationManager;
    private List<Place.Field> fields = Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG, Place.Field.ADDRESS);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);
        ButterKnife.bind(this);
        vars = new Vars(this);
        progressDialog = new ProgressDialog(this);
        user = new User();
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        displayPhoneLayout();

        if (!Places.isInitialized()) {
            Places.initialize(getApplicationContext(), getString(R.string.google_maps_key));
        }
    }

    private void pickLocation() {
        Log.d(TAG, "pickLocation called: ");

        Intent intent = new Autocomplete.IntentBuilder(AutocompleteActivityMode.OVERLAY, fields).setCountry("UG")
                .build(this);
        startActivityForResult(intent, Globals.AUTOCOMPLETE_REQUEST_CODE);
    }

    @OnClick({ R.id.location})
    public void onItemClicked(View view) {
        switch (view.getId()) {

            case R.id.location:
                pickLocation();
                break;
            default:
                break;
        }

    }


    public void  startOTPRequest() {
        Log.d(TAG, "startOTPRequest called: ");
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
                            Toast.makeText(SignupActivity.this, "OTP code has been sent to your phone!", Toast.LENGTH_LONG).show();
                        }

                        @Override
                        public void onCodeAutoRetrievalTimeOut(@NonNull String s) {
                            super.onCodeAutoRetrievalTimeOut(s);
                        }

                        @Override
                        public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {
                            Log.d(TAG, "Verification Completed: ");
                        }

                        @Override
                        public void onVerificationFailed(@NonNull FirebaseException e) {
                            vars.verityApp.crashlytics.recordException(e);
                            vars.verityApp.crashlytics.log("Phone verification failed" + e.getMessage());
                            Log.e(TAG, "Phone verification failed: ", e);
                            progressDialog.dismiss();
                        }
                    });
        } else {
            progressDialog.dismiss();
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
            progressDialog.dismiss();
            code.setError("OTP is invalid");
        }
    }

    public void displayCodeLayout() {
        Log.d(TAG, "displayCodeLayout called: ");
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
        progressDialog.setMessage("Saving user details ...");
        progressDialog.show();

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
                            progressDialog.dismiss();
                        } else {
                            progressDialog.dismiss();
                            Toast.makeText(this, "Registration unsuccessful", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(e -> {
                        progressDialog.dismiss();
                        vars.verityApp.crashlytics.recordException(e);
                        Log.e(TAG, "Error while saving data: ",e);
                    });

        } else {
            progressDialog.dismiss();
            Toast.makeText(this, "All fields are required", Toast.LENGTH_SHORT).show();
            return;
        }
    }

    public void displayPhoneLayout() {
        Log.d(TAG, "displayPhoneLayout called: ");
        stepCodeLayout.setVisibility(View.GONE);
        stepInfoLayout.setVisibility(View.GONE);
        stepPhoneLayout.setVisibility(View.VISIBLE);
        signUpBtn.setText(CONTINUE);

        signUpBtn.setOnClickListener(view -> {
            if (signUpBtn.getText().toString().equalsIgnoreCase(CONTINUE)) {
                startOTPRequest();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == Globals.AUTOCOMPLETE_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                Place place = Autocomplete.getPlaceFromIntent(data);
                userAddress.setText(place.getAddress());
            } else if (resultCode == AutocompleteActivity.RESULT_ERROR) {
                Status status = Autocomplete.getStatusFromIntent(data);
                Log.d(TAG, "Error while picking location: "+status.getStatusMessage());
                Toast.makeText(this, status.getStatusMessage(), Toast.LENGTH_SHORT).show();
            } else if (resultCode == RESULT_CANCELED) {
                Log.d(TAG, "onActivityResult: " + "Cancelled...");
            }
        }
    }

                @Override
    protected void onStart() {
        super.onStart();
        if (vars.isLoggedIn()) {
            checkUserProfile();
        }
    }
}