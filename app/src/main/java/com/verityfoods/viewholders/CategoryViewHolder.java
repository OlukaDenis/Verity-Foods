package com.verityfoods.viewholders;

import android.content.Context;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.squareup.picasso.Picasso;
import com.verityfoods.R;
import com.verityfoods.data.interfaces.CustomItemClickListener;
import com.verityfoods.data.model.Category;

import butterknife.BindView;
import butterknife.ButterKnife;

public class CategoryViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
    private CustomItemClickListener itemClickListener;
    private Context context;
    @BindView(R.id.category_image)
    ImageView categoryImage;

    @BindView(R.id.category_name)
    TextView categoryName;

    public CategoryViewHolder(@NonNull View itemView, Context context) {
        super(itemView);
        ButterKnife.bind(this, itemView);
        this.context = context;
        itemView.setOnClickListener(this);
    }

    public void bindCategory(Category category) {
        categoryName.setText(category.getName());

        if (category.getName().isEmpty()) {
            Glide.with(context)
                    .load(R.drawable.ic_baseline_image_24)
                    .centerCrop()
                    .placeholder(R.drawable.ic_baseline_image_24)
                    .into(categoryImage);
        } else {
            Glide.with(context)
                    .load(category.getImage())
                    .centerCrop()
                    .placeholder(R.drawable.ic_baseline_image_24)
                    .error(R.drawable.ic_baseline_image_24)
                    .into(categoryImage);
        }
    }

    public void setItemClickListener(CustomItemClickListener itemClickListener) {
        this.itemClickListener = itemClickListener;
    }

    @Override
    public void onClick(View v) {
        itemClickListener.onItemClick(v, getAdapterPosition());
    }
}
