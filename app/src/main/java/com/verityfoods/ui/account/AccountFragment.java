package com.verityfoods.ui.account;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.linchaolong.android.imagepicker.ImagePicker;
import com.linchaolong.android.imagepicker.cropper.CropImage;
import com.linchaolong.android.imagepicker.cropper.CropImageView;
import com.squareup.picasso.Picasso;
import com.verityfoods.R;
import com.verityfoods.data.model.User;
import com.verityfoods.ui.auth.SignupActivity;
import com.verityfoods.utils.Globals;
import com.verityfoods.utils.Vars;

import java.io.File;
import java.util.Date;
import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;

public class AccountFragment extends Fragment {
    private static final String TAG = "AccountFragment";

    private Vars vars;
    private User user;
    private String userUid;

    @BindView(R.id.p_email)
    TextInputEditText email;
    @BindView(R.id.p_fullnames)
    TextInputEditText fullnames;
    @BindView(R.id.p_address)
    TextInputEditText address;
    @BindView(R.id.p_phone)
    TextInputEditText phonenumber;
    @BindView(R.id.p_update)
    MaterialButton update;
    @BindView(R.id.profile)
    ImageView profileImageView;
    String profileImageUrl = "";
    @BindView(R.id.progressBar)
    ProgressBar progressBar;

    UploadTask uploadTask;
    private ImagePicker imagePicker = new ImagePicker();

    private AccountViewModel accountViewModel;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        accountViewModel =
                ViewModelProviders.of(this).get(AccountViewModel.class);
        View root = inflater.inflate(R.layout.fragment_account, container, false);
        ButterKnife.bind(this, root);
        vars = new Vars(requireActivity());
        progressBar.setVisibility(View.VISIBLE);

        if (vars.isLoggedIn()) {
            userUid = vars.verityApp.mAuth.getCurrentUser().getUid();
            getCurrentUser();
        } else {
            startActivity(new Intent(requireActivity(), SignupActivity.class));
            Toast.makeText(requireActivity(), "You need to login to continue", Toast.LENGTH_SHORT).show();
        }

        return root;
    }

    private void getCurrentUser() {
        vars.verityApp.db.collection(Globals.USERS)
                .document(userUid)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        user = documentSnapshot.toObject(User.class);
                        populateUser(user);
                        progressBar.setVisibility(View.GONE);
                    }
                })
        .addOnFailureListener(e -> Log.e(TAG, "getCurrentUser: ",e ));
    }

    private void populateUser(User user) {
        email.setText(user.getEmail());
        fullnames.setText(user.getName());
        phonenumber.setText(user.getPhone());
        address.setText(user.getAddress());

        if (!user.getEmail().isEmpty() || user.getEmail() != null) {
            Picasso.get()
                    .load(user.getImage())
                    .placeholder(R.drawable.avatar)
                    .error(R.drawable.avatar)
                    .into(profileImageView);
        } else {

            profileImageView.setBackgroundResource(R.drawable.avatar);
        }

        profileImageView.setOnClickListener(view -> pickImage());

        update.setOnClickListener(view -> {

            if (fullnames.getText().toString().length() < 3) {
                fullnames.setError("Name must be at least 3 characters.");
            } else if (vars.isValidEmail(email.getText().toString())) {
                email.setError("Invalid email");
            } else if (phonenumber.getText().toString().length() < 8) {
                phonenumber.setError("Phone Number must be at least 8 figures");
            } else if (address.getText().toString().isEmpty()) {
                address.setError("Your address is need to know where you are.");
            } else {


                fullnames.setEnabled(false);
                email.setEnabled(false);
                phonenumber.setEnabled(false);
                address.setEnabled(false);

                updateProfile(Objects.requireNonNull(vars.verityApp.mAuth.getCurrentUser()),
                        fullnames.getText().toString(),
                        Uri.fromFile(new File(profileImageUrl)),
                        phonenumber.getText().toString()
                );
            }
        });
    }

    public void updateProfile(FirebaseUser user, String displayname, Uri profile, String phonenumber) {

        UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                .setDisplayName(displayname)
                .setPhotoUri(profile)
                .build();

        user.updateProfile(profileUpdates);
        User u = new User();
        u.setImage(profileImageUrl);
        u.setName(displayname);
        u.setPhone(phonenumber);
        u.setEmail(email.getText().toString());
        u.setAddress(address.getText().toString());
        vars.verityApp.db.collection(Globals.USERS)
                .document(userUid)
                .set(u)
                .addOnSuccessListener(documentReference -> {
                    getCurrentUser();
                    Toast.makeText(requireActivity(), "Profile has been updated successfully!", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> Log.e(TAG, "updateProfile: ",e ));

    }

    private void pickImage() {

        imagePicker.startChooser(this, new ImagePicker.Callback() {
            @Override
            public void onPickImage(Uri imageUri) {
                update.setEnabled(false);
                uploadfile(imageUri);
            }


            @Override
            public void onCropImage(Uri imageUri) {
                update.setEnabled(false);
                uploadfile(imageUri);


            }

            @Override
            public void cropConfig(CropImage.ActivityBuilder builder) {
                builder
                        .setMultiTouchEnabled(true)
                        .setGuidelines(CropImageView.Guidelines.OFF)
                        .setCropShape(CropImageView.CropShape.RECTANGLE)
                        .setActivityMenuIconColor(getResources().getColor(R.color.colorAccent))
                        .setAutoZoomEnabled(true)
                        .setRequestedSize(1280, 1280)
                        .setAspectRatio(16, 16);
            }

            @Override
            public void onPermissionDenied(int requestCode, String[] permissions,
                                           int[] grantResults) {
                Toast.makeText(requireActivity(), "Picture permission denied", Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void uploadfile(Uri imageuri) {
        profileImageView.setEnabled(false);
        progressBar.setVisibility(View.VISIBLE);
        profileImageView.setImageURI(imageuri);

        Picasso.get()
                .load(imageuri)
                .error(R.drawable.avatar)
                .placeholder(R.drawable.avatar)
                .into(profileImageView);

        // Create a storage reference from our app
        StorageReference storageRef = vars.verityApp.mStorageRef.getReference();
        final StorageReference spaceRef = storageRef.child("users/profiles" + new Date().getTime() + ".jpg");
        uploadTask = spaceRef.putFile(imageuri);
        // Register observers to listen for when the download is done or if it fails
        uploadTask.addOnFailureListener(exception -> {
            // Handle unsuccessful uploads
            vars.verityApp.crashlytics.recordException(exception);
            Log.e(TAG, "uploadfile: ",exception );
            progressBar.setVisibility(View.GONE);
            update.setEnabled(true);
            Toast.makeText(requireActivity(), "Image upload failed, please try again", Toast.LENGTH_SHORT).show();

        }).addOnSuccessListener(taskSnapshot -> {
            progressBar.setVisibility(View.GONE);
            update.setEnabled(true);


        });

        uploadTask.continueWithTask(task -> {
            if (!task.isSuccessful()) {
                vars.verityApp.crashlytics.recordException(task.getException());
                throw Objects.requireNonNull(task.getException());
            }

            // Continue with the task to get the download URL
            return spaceRef.getDownloadUrl();
        }).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Uri downloadUri = task.getResult();
                profileImageUrl = String.valueOf(downloadUri);
            }
        });
    }



    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        imagePicker.onActivityResult(this, requestCode, resultCode, data);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        imagePicker.onRequestPermissionsResult(this, requestCode, permissions, grantResults);
    }
}