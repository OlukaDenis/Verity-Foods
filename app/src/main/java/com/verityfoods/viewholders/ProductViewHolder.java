package com.verityfoods.viewholders;

import android.graphics.Paint;
import android.view.View;
import android.widget.ImageView;
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

public class ProductViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
    private CustomItemClickListener itemClickListener;
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

    @BindView(R.id.product_quantity_counter)
    ElegantNumberButton productQuantity;

    public ProductViewHolder(@NonNull View itemView) {
        super(itemView);
        ButterKnife.bind(this, itemView);
        itemView.setOnClickListener(this);
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

        productQuantity.setOnValueChangeListener((view, oldValue, newValue) -> {
            Globals.CART_COUNT = newValue;
        });
    }

    public void setItemClickListener(CustomItemClickListener itemClickListener) {
        this.itemClickListener = itemClickListener;
    }

    @Override
    public void onClick(View v) {
        itemClickListener.onItemClick(v, getAdapterPosition());
    }
}
