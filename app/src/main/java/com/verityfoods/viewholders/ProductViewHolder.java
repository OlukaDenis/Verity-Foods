package com.verityfoods.viewholders;

import android.graphics.Paint;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.cepheuen.elegantnumberbutton.view.ElegantNumberButton;
import com.squareup.picasso.Picasso;
import com.verityfoods.R;
import com.verityfoods.data.interfaces.CustomItemClickListener;
import com.verityfoods.data.model.Product;
import com.verityfoods.utils.AppUtils;
import com.verityfoods.utils.Globals;

import butterknife.BindView;
import butterknife.ButterKnife;

public class ProductViewHolder extends RecyclerView.ViewHolder {
    private static final String TAG = "ProductViewHolder";
    private LinearLayout plusMinusButton;
    public TextView plusButton;
    public TextView minusButton;
    public TextView total;
    private int value = 0;

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
        productName.setText(product.getName());
        productBrand.setText(product.getBrand());
        productMRP.setText(AppUtils.formatCurrency(product.getMrp_rates()));
        productMRP.setPaintFlags(productMRP.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
        productPrice.setText(AppUtils.formatCurrency(product.getSelling_price()));

        Picasso.get()
                .load(product.getImage())
                .error(R.drawable.ic_baseline_image_24)
                .placeholder(R.drawable.ic_baseline_image_24)
                .into(productImage);
    }


}
