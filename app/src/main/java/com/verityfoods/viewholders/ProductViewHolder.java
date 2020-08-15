package com.verityfoods.viewholders;

import android.graphics.Paint;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;
import com.verityfoods.R;
import com.verityfoods.data.model.Product;
import com.verityfoods.utils.AppUtils;

import butterknife.BindView;
import butterknife.ButterKnife;

public class ProductViewHolder extends RecyclerView.ViewHolder {
    private static final String TAG = "ProductViewHolder";
    private LinearLayout plusMinusButton;
    public TextView plusButton;
    public TextView minusButton;
    public TextView total;
    public int value;

    @BindView(R.id.prduct_image)
    ImageView productImage;

    @BindView(R.id.product_name)
    TextView productName;

    @BindView(R.id.product_brand)
    TextView productBrand;

    @BindView(R.id.product_mrp)
    TextView productMRP;

    @BindView(R.id.product_price)
    TextView productPrice;

    @BindView(R.id.discount_layout)
    FrameLayout discountLayout;

    @BindView(R.id.offer_value)
    TextView offerValue;

    public Button addToCart;

    public ProductViewHolder(@NonNull View itemView) {
        super(itemView);
        ButterKnife.bind(this, itemView);
        plusButton = itemView.findViewById(R.id.plus_btn);
        minusButton = itemView.findViewById(R.id.minus_btn);
        total = itemView.findViewById(R.id.counter_total);
        plusMinusButton = itemView.findViewById(R.id.plus_minus_button);
        addToCart = itemView.findViewById(R.id.add_to_cart);
        Log.d(TAG, "ProductViewHolder: "+total.getText().toString().trim());
    }

    public void bindProduct(Product product) {

        value = Integer.parseInt(total.getText().toString());
        plusButton.setOnClickListener(view -> {
            int p = Integer.parseInt(total.getText().toString());
            total.setText(String.valueOf(p += 1));
            value = Integer.parseInt(total.getText().toString());
            Log.d(TAG, "on Plus: "+value);
        });

        minusButton.setOnClickListener(view -> {
            int m = Integer.parseInt(total.getText().toString());
            if (m > 1) {
                total.setText(String.valueOf(m -= 1));
                value = Integer.parseInt(total.getText().toString());
            }
            Log.d(TAG, "on Minus: "+value);
        });

        if (product.isOffer()) {
            discountLayout.setVisibility(View.VISIBLE);
            offerValue.setText(AppUtils.formatOffer(product.getOffer_value()));

            double discount = (product.getOffer_value() * product.getSelling_price()) / 100;
            double actual = product.getSelling_price() - discount;
            int m = (int) actual;
            productPrice.setText(AppUtils.formatCurrency(m));
        } else {
            discountLayout.setVisibility(View.GONE);
            productPrice.setText(AppUtils.formatCurrency(product.getSelling_price()));
        }
        productName.setText(product.getName());
        productBrand.setText(product.getBrand());
        productMRP.setText(AppUtils.formatCurrency(product.getMrp()));
        productMRP.setPaintFlags(productMRP.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
        Log.d(TAG, "It is an offer: "+product.isOffer());

        Picasso.get()
                .load(product.getImage())
                .error(R.drawable.ic_baseline_image_24)
                .placeholder(R.drawable.ic_baseline_image_24)
                .into(productImage);
    }


}
