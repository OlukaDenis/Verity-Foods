package com.verityfoods.viewholders;

import android.content.Context;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.verityfoods.R;
import com.verityfoods.data.model.Cart;
import com.verityfoods.utils.AppUtils;

import butterknife.BindView;
import butterknife.ButterKnife;

public class CartViewHolder extends RecyclerView.ViewHolder {
    private static final String TAG = "CartViewHolder";
    private LinearLayout plusMinusButton;
    public TextView plusButton;
    public TextView minusButton;
    public TextView total;

    @BindView(R.id.cart_image)
    ImageView cartImage;

    @BindView(R.id.cart_name)
    TextView cartName;

    @BindView(R.id.cart_category)
    TextView cartCategory;

    @BindView(R.id.cart_price)
    TextView cartPrice;

    public ImageButton removeCart;
    private Context context;

    public CartViewHolder(@NonNull View itemView, Context context) {
        super(itemView);
        this.context = context;
        ButterKnife.bind(this, itemView);
        plusButton = itemView.findViewById(R.id.plus_btn);
        minusButton = itemView.findViewById(R.id.minus_btn);
        total = itemView.findViewById(R.id.counter_total);
        plusMinusButton = itemView.findViewById(R.id.plus_minus_button);
        removeCart = itemView.findViewById(R.id.remove_cart);
    }

    public void bindCart(Cart cart) {
        cartName.setText(cart.getProduct_name());
        cartCategory.setText(cart.getCategory_name());
        cartPrice.setText(AppUtils.formatCurrency(cart.getAmount()));
        total.setText(String.valueOf(cart.getQuantity()));

        Glide.with(context)
                .load(cart.getProduct_image())
                .centerCrop()
                .error(R.drawable.ic_baseline_image_24)
                .placeholder(R.drawable.ic_baseline_image_24)
                .into(cartImage);

    }


}

