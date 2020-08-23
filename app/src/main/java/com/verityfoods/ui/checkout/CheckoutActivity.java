package com.verityfoods.ui.checkout;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.verityfoods.MainActivity;
import com.verityfoods.R;
import com.verityfoods.data.model.Cart;
import com.verityfoods.data.model.Order;
import com.verityfoods.data.model.User;
import com.verityfoods.utils.AppUtils;
import com.verityfoods.utils.Globals;
import com.verityfoods.utils.Vars;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Random;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

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

    private RadioButton radioButtonDay;

    @BindView(R.id.rg_delivery_days)
    RadioGroup deliveryDays;

    @BindView(R.id.nine_to_eleven)
    CheckBox nineToEleven;

    @BindView(R.id.eleven_to_one)
    CheckBox elevenToOne;

    @BindView(R.id.one_to_three)
    CheckBox oneToThree;

    @BindView(R.id.three_to_five)
    CheckBox threeToFive;

    private RadioButton radioButtonPayment;

    @BindView(R.id.rg_payment_methods)
    RadioGroup paymentMethods;

    private String deliveryMethod = "";

    private String userUid;
    private String strDeliveryDay = "";
    private String strDeliveryTime = "";
    private String strPaymentMethod = "";
    private int orderNumber;
    private ProgressDialog loading;
    private Order order;
    private List<Cart> cartList;
    private Cart cart;

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
        order = new Order();
        cartList = new ArrayList<>();

        Random random = new Random();
        orderNumber = random.nextInt(1000000000);

        loading = new ProgressDialog(this);
        loading.setMessage("Please wait");
        loading.setCancelable(false);
        userUid = vars.verityApp.mAuth.getCurrentUser().getUid();

        subTotal = Objects.requireNonNull(getIntent().getExtras()).getInt(Globals.ORDER_TOTAL);
        totalSum = findViewById(R.id.total_order_summary);
        totalSum.setText(AppUtils.formatCurrency(total));

        standardShipping.setOnCheckedChangeListener(this);
        pickupStation.setOnCheckedChangeListener(this);
        nineToEleven.setOnCheckedChangeListener(this);
        elevenToOne.setOnCheckedChangeListener(this);
        oneToThree.setOnCheckedChangeListener(this);
        threeToFive.setOnCheckedChangeListener(this);

        deliveryDays.setOnCheckedChangeListener((group, checkedId) -> {
            radioButtonDay = findViewById(checkedId);
            strDeliveryDay = radioButtonDay.getText().toString();
        });

        paymentMethods.setOnCheckedChangeListener((group, checkedId) -> {
            radioButtonPayment = findViewById(checkedId);
            strPaymentMethod = radioButtonPayment.getText().toString();
        });

        getAllCart();
        populateUserDetails();
        updateTotals(shipping);
    }

    public void getAllCart() {
        Query cartQuery = vars.verityApp.db.collection(Globals.CART)
                .document(vars.getShoppingID())
                .collection(Globals.MY_CART);

        cartQuery.get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : Objects.requireNonNull(task.getResult())) {
                            cart = document.toObject(Cart.class);
                            cartList.add(cart);
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "getAllCart: ",e );
                });
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
        total = shippingFee + subTotal;

        shippingTotal.setText(AppUtils.formatCurrency(shippingFee));
        textSubTotal.setText(AppUtils.formatCurrency(subTotal));
        totalSum.setText(AppUtils.formatCurrency(total));
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        int id = buttonView.getId();
        if (isChecked) {
            if (id == R.id.standard_shipping){
                pickupStation.setChecked(false);
                updateDeliveryMethod(standardShipping.getText().toString());
                updateTotals(2000);
            }

            if (id == R.id.pickup_station) {
                standardShipping.setChecked(false);
                updateDeliveryMethod(pickupStation.getText().toString());
                updateTotals(0);
            }

            if (id == R.id.nine_to_eleven) {
                strDeliveryTime = nineToEleven.getText().toString();
                elevenToOne.setChecked(false);
                oneToThree.setChecked(false);
                threeToFive.setChecked(false);
            }

            if (id == R.id.eleven_to_one) {
                strDeliveryTime = elevenToOne.getText().toString();
                nineToEleven.setChecked(false);
                oneToThree.setChecked(false);
                threeToFive.setChecked(false);
            }

            if (id == R.id.one_to_three) {
                strDeliveryTime = oneToThree.getText().toString();
                nineToEleven.setChecked(false);
                elevenToOne.setChecked(false);
                threeToFive.setChecked(false);
            }

            if (id == R.id.three_to_five) {
                strDeliveryTime = threeToFive.getText().toString();
                nineToEleven.setChecked(false);
                elevenToOne.setChecked(false);
                oneToThree.setChecked(false);
            }
        }

    }

    @OnClick(R.id.submit_order_btn)
    void submitOrder() {
        loading.show();
        order.setDeliveryDay(strDeliveryDay);
        order.setDeliveryTime(strDeliveryTime);
        order.setDeliveryMethod(deliveryMethod);
        order.setPaymentMethod(strPaymentMethod);
        order.setDateAdded(AppUtils.currentDate());
        order.setTimeAdded(AppUtils.currentTime());
        order.setUser(user);
        order.setShippingFee(shipping);
        order.setTotal(total);
        order.setSubTotal(subTotal);
        order.setProducts(cartList);
        order.setOrder_number(String.valueOf(orderNumber));
        order.setStatus(Globals.ORDER_PLACED);

        if (deliveryMethod.isEmpty()) {
            Toast.makeText(this, "Please select your delivery method", Toast.LENGTH_SHORT).show();
            loading.dismiss();
        } else if (strPaymentMethod.isEmpty()) {
            Toast.makeText(this, "Please select your preferred payment method", Toast.LENGTH_SHORT).show();
            loading.dismiss();
        } else if (strDeliveryTime.isEmpty()) {
            Toast.makeText(this, "Please select your preferred delivery time", Toast.LENGTH_SHORT).show();
            loading.dismiss();
        } else if (strDeliveryDay.isEmpty()) {
            Toast.makeText(this, "Please select your preferred delivery day", Toast.LENGTH_SHORT).show();
            loading.dismiss();
        } else if (addressName.getText().toString().isEmpty()) {
            Toast.makeText(this, "Please provide your address", Toast.LENGTH_SHORT).show();
            loading.dismiss();
        } else {
            saveOrder();
        }
    }

    public void updateDeliveryMethod(String selected) {
        if (selected.isEmpty()) {
            deliveryMethod = null;
        } else {
            deliveryMethod = selected;
        }
    }

    private void saveOrder() {
        Map<String, Object> mOrder = new HashMap<>();
        mOrder.put("name", "Orders");

        vars.verityApp.db.collection(Globals.ORDERS)
                .document(userUid)
                .set(mOrder)
                .addOnSuccessListener(aVoid ->
                        vars.verityApp.db.collection(Globals.ORDERS)
                                .document(userUid)
                                .collection(Globals.MY_ORDERS)
                                .document(String.valueOf(orderNumber))
                                .set(order)
                                .addOnSuccessListener(documentReference -> {
                                    deleteAllCarts();
                                    startSuccessActivity(String.valueOf(orderNumber));
                                    Toast.makeText(this, "Order successfully placed!", Toast.LENGTH_SHORT).show();
                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(this, "Something bad happened please try agine", Toast.LENGTH_SHORT).show();;
                                    loading.dismiss();
                                }));
    }


    private void deleteAllCarts() {
        vars.verityApp.db.collection(Globals.CART)
                .document(vars.getShoppingID())
                .collection(Globals.MY_CART)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : Objects.requireNonNull(task.getResult())) {
                            vars.verityApp.db.collection(Globals.CART)
                                    .document(vars.getShoppingID())
                                    .collection(Globals.MY_CART)
                                    .document(document.getId())
                                    .delete();
                            loading.dismiss();
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "deleteAllCarts: ",e );
                });
    }

    private void showDialog(String orderNo){
        final AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setTitle("Success")
                .setMessage("Your order " + orderNo + " has been submitted successfully. " +
                        "Please go to your account and track your order.")
                .setPositiveButton("OKAY", (paramDialogInterface, paramInt) -> {
                    startActivity(new Intent(getApplicationContext(), MainActivity.class));
                });
        dialog.setCancelable(false);
        dialog.show();
    }

    private void startSuccessActivity(String orderNo) {
        Intent successIntent = new Intent(this, SuccessActivity.class);
        successIntent.putExtra(Globals.ORDER_NUMBER, orderNo);
        startActivity(successIntent);
        finish();
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