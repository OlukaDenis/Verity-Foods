package com.verityfoods.ui.checkout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import com.verityfoods.MainActivity;
import com.verityfoods.R;
import com.verityfoods.utils.Globals;

import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class SuccessActivity extends AppCompatActivity {

    @BindView(R.id.success_message)
    TextView successMessage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_success);

        Toolbar toolbar = findViewById(R.id.success_toolbar);
        setSupportActionBar(toolbar);
        setTitle("Order Placed");
//        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
//        this.getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_baseline_close_24);

        ButterKnife.bind(this);
        String orderNo = Objects.requireNonNull(getIntent().getExtras()).getString(Globals.ORDER_NUMBER);

        String success = "Your order " + orderNo + " has been submitted successfully. " +
                "Please go to your account and track your order.";

        successMessage.setText(success);
    }

    @OnClick(R.id.btn_success_continue)
    void goShopping() {
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }
}