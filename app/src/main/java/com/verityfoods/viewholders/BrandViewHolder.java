package com.verityfoods.viewholders;

import android.content.Context;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.verityfoods.R;
import com.verityfoods.data.model.Product;

public class BrandViewHolder extends RecyclerView.ViewHolder {
    public TextView brandName;
    private Context context;

    public BrandViewHolder(@NonNull View itemView, Context context) {
        super(itemView);
        this.context = context;

        brandName = itemView.findViewById(R.id.brand_name);
    }

    public void bindBrand(Product product) {
        brandName.setText(product.getBrand());
//        brandName.setBackground(context.getResources().getDrawable(R.drawable.varible_bg));
    }
}