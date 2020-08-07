package com.verityfoods.viewholders;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;
import com.verityfoods.R;
import com.verityfoods.data.interfaces.CustomItemClickListener;
import com.verityfoods.data.model.Category;

import butterknife.BindView;
import butterknife.ButterKnife;

public class CategoryViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
    private CustomItemClickListener itemClickListener;
    @BindView(R.id.category_image)
    ImageView categoryImage;

    @BindView(R.id.category_name)
    TextView categoryName;

    public CategoryViewHolder(@NonNull View itemView) {
        super(itemView);
        ButterKnife.bind(this, itemView);
    }

    public void bindCategory(Category category) {
        categoryName.setText(category.getName());

        Picasso.get()
                .load(category.getImage())
                .error(R.drawable.ic_baseline_image_24)
                .placeholder(R.drawable.ic_baseline_image_24)
                .into(categoryImage);
    }

    public void setItemClickListener(CustomItemClickListener itemClickListener) {
        this.itemClickListener = itemClickListener;
    }

    @Override
    public void onClick(View v) {
        itemClickListener.onItemClick(v, getAdapterPosition());
    }
}
