package com.verityfoods.ui.checkout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.os.Bundle;
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

public class CheckoutActivity extends AppCompatActivity {
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

    private RadioButton standardShipping;
    private RadioButton pickupStation;
    private String deliveryMethod = "";

    private String userUid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_checkout);

        Toolbar toolbar = findViewById(R.id.checkout_toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitle("");
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        ButterKnife.bind(this);
        vars = new Vars(this);
        userUid = vars.verityApp.mAuth.getCurrentUser().getUid();

        total = Objects.requireNonNull(getIntent().getExtras()).getInt(Globals.ORDER_TOTAL);
        totalSum = findViewById(R.id.total_order_summary);
        totalSum.setText(AppUtils.formatCurrency(total));

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
}