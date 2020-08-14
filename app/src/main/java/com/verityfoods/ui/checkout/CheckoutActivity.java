package com.verityfoods.ui.checkout;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.os.Bundle;
import android.view.MenuItem;
import android.widget.CompoundButton;
import android.widget.RadioButton;
import android.widget.TextView;

import com.verityfoods.R;
import com.verityfoods.data.model.User;
import com.verityfoods.utils.AppUtils;
import com.verityfoods.utils.Globals;
import com.verityfoods.utils.Vars;

import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;

public class CheckoutActivity extends AppCompatActivity implements CompoundButton.OnCheckedChangeListener  {
    private static final String TAG = "CheckoutActivity";
    private TextView totalSum;
    private TextView changeAddress;
    private User user;
    private Vars vars;

    private int total = 0;
    private int subTotal = 0;
    private int shipping = 0;

    //totals
    @BindView(R.id.sub_total)
    TextView textSubTotal;

    @BindView(R.id.shipping_total)
    TextView shippingTotal;

    //Address
    @BindView(R.id.text_address_name)
    TextView userName;

    @BindView(R.id.text_address)
    TextView addressName;

    @BindView(R.id.text_phone)
    TextView userPhone;

    @BindView( R.id.standard_shipping)
    RadioButton standardShipping;

    @BindView(R.id.pickup_station)
    RadioButton pickupStation;

    private String deliveryMethod = "";

    private String userUid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_checkout);

        Toolbar toolbar = findViewById(R.id.checkout_toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        this.getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_baseline_close_24);

        ButterKnife.bind(this);
        vars = new Vars(this);
        userUid = vars.verityApp.mAuth.getCurrentUser().getUid();

        total = Objects.requireNonNull(getIntent().getExtras()).getInt(Globals.ORDER_TOTAL);
        totalSum = findViewById(R.id.total_order_summary);
        totalSum.setText(AppUtils.formatCurrency(total));

        standardShipping.setOnCheckedChangeListener(this);
        pickupStation.setOnCheckedChangeListener(this);

        populateUserDetails();
        updateTotals(shipping);
    }

    public void populateUserDetails() {
        vars.verityApp.db.collection(Globals.USERS)
                .document(userUid)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        user = documentSnapshot.toObject(User.class);
                        if (user != null) {
                            addressName.setText(user.getAddress());
                            userName.setText(user.getName());
                            userPhone.setText(user.getPhone());
                        }
                    }
                });
    }

    public void updateTotals(int shippingFee) {
        shipping = shippingFee;
        int grandTotal = shippingFee + total;

        shippingTotal.setText(AppUtils.formatCurrency(shippingFee));
        textSubTotal.setText(AppUtils.formatCurrency(total));
        totalSum.setText(AppUtils.formatCurrency(grandTotal));
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

        if (isChecked) {
            if (buttonView.getId() == R.id.standard_shipping){
                pickupStation.setChecked(false);
                updateDeliveryMethod(standardShipping.getText().toString());
                updateTotals(2000);
            }

            if (buttonView.getId() == R.id.pickup_station) {
                standardShipping.setChecked(false);
                updateDeliveryMethod(pickupStation.getText().toString());
                updateTotals(0);
            }
        }

    }

    public void updateDeliveryMethod(String selected) {
        if (selected.isEmpty()) {
            deliveryMethod = null;
        } else {
            deliveryMethod = selected;
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