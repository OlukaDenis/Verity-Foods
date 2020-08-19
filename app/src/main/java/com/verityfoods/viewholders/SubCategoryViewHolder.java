package com.verityfoods.viewholders;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.verityfoods.R;
import com.verityfoods.data.model.SubCategory;

import butterknife.BindView;
import butterknife.ButterKnife;

public class SubCategoryViewHolder extends RecyclerView.ViewHolder {
    @BindView(R.id.sub_category_name)
    TextView subCategoryName;

    public SubCategoryViewHolder(@NonNull View itemView) {
        super(itemView);
        ButterKnife.bind(this, itemView);
    }

    public void bindSubCategory(SubCategory subCategory) {
        subCategoryName.setText(subCategory.getName());
    }
}
