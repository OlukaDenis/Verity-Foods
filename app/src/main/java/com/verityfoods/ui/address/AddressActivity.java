package com.verityfoods.ui.address;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.widget.Autocomplete;
import com.google.android.libraries.places.widget.AutocompleteActivity;
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.Query;
import com.verityfoods.R;
import com.verityfoods.data.adapters.AddressAdapter;
import com.verityfoods.data.model.Address;
import com.verityfoods.data.model.User;
import com.verityfoods.utils.Globals;
import com.verityfoods.utils.Vars;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class AddressActivity extends AppCompatActivity {
    private static final String TAG = "AddressActivity";
    private RecyclerView addressRecycler;
    private Vars vars;
    private AddressAdapter adapter;
    private FloatingActionButton addAddress;
    private LocationManager locationManager;
    private User user;
    private String userID;
    private Address address;
    private List<Place.Field> fields = Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG, Place.Field.ADDRESS);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_address);
        setTitle("My Addresses");

        Toolbar toolbar = findViewById(R.id.address_toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        this.getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_close);

        vars = new Vars(this);
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        address = new Address();
        user = new User();

        userID = vars.verityApp.mAuth.getCurrentUser().getUid();

        addressRecycler = findViewById(R.id.addresses_recyclerview);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        addressRecycler.setLayoutManager(layoutManager);

        if (!Places.isInitialized()) {
            Places.initialize(getApplicationContext(), getString(R.string.google_maps_key));
        }

        addAddress = findViewById(R.id.fab_add_address);
        addAddress.setOnClickListener(v -> pickPlace());

        getCurrentUser();
        populateAddresses();
    }

    private void getCurrentUser() {
        vars.verityApp.db.collection(Globals.USERS)
                .document(vars.verityApp.mAuth.getCurrentUser().getUid())
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        user = documentSnapshot.toObject(User.class);
                    }
                });
    }

    private void pickPlace() {
        try {
            // Start the autocomplete intent.
            Intent intent = new Autocomplete.IntentBuilder(AutocompleteActivityMode.OVERLAY, fields).setCountry("UG")
                    .build(this);
            startActivityForResult(intent, Globals.AUTOCOMPLETE_REQUEST_CODE);
        } catch (Exception e) {
            vars.verityApp.crashlytics.recordException(e);
        }
    }

    private void populateAddresses() {
        Query addressQuery = vars.verityApp.db
                .collection(Globals.ADDRESS)
                .document(userID)
                .collection(Globals.MY_ADDRESS);


        FirestoreRecyclerOptions<Address> addressOptions = new FirestoreRecyclerOptions.Builder<Address>()
                .setLifecycleOwner(this)
                .setQuery(addressQuery, Address.class)
                .build();
        adapter = new AddressAdapter(addressOptions, this);
        addressRecycler.setAdapter(adapter);

        adapter.setItemClickListener((documentSnapshot, position) -> {
            if (documentSnapshot != null) {
                Address mAddress = documentSnapshot.toObject(Address.class);
                if (mAddress != null) {
                    mAddress.setUuid(documentSnapshot.getId());
                    Intent returnIntent = new Intent();
                    returnIntent.putExtra(Globals.MY_SELECTED_ADDRESS, mAddress);
                    setResult(Activity.RESULT_OK, returnIntent);
                    finish();
                }
            }


        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == Globals.AUTOCOMPLETE_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {

                Place place = Autocomplete.getPlaceFromIntent(data);
                address.setAddress(place.getAddress());
                address.setName(place.getName());
                address.setPhone(user.getPhone());
                address.setEmail(user.getEmail());
                address.setUuid(vars.verityApp.mAuth.getCurrentUser().getUid());
                address.setUsername(user.getName());
                address.setLatitude(Objects.requireNonNull(place.getLatLng()).latitude);
                address.setLongitude(place.getLatLng().longitude);

                vars.verityApp.db.collection(Globals.ADDRESS)
                        .document(userID)
                        .collection(Globals.MY_ADDRESS)
                        .add(address);
            } else if (resultCode == AutocompleteActivity.RESULT_ERROR) {
                Toast.makeText(getApplicationContext(), "Unable to get address", Toast.LENGTH_LONG).show();

            }

        }
    }


    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}