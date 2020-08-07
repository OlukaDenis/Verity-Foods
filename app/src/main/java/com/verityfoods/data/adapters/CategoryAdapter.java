package com.verityfoods.data.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.squareup.picasso.Picasso;
import com.verityfoods.R;
import com.verityfoods.data.interfaces.ItemClickListener;
import com.verityfoods.data.model.Category;
import com.verityfoods.utils.Vars;

import butterknife.BindView;
import butterknife.ButterKnife;

public class CategoryAdapter extends FirestoreRecyclerAdapter<Category, CategoryAdapter.CategoryViewHolder> {
    private ItemClickListener itemClickListener;
    private Vars vars;
    private Context context;

    public CategoryAdapter(@NonNull FirestoreRecyclerOptions<Category> options, Context context, Vars vars) {
        super(options);
        this.vars = vars;
        this.context = context;
    }

    @Override
    protected void onBindViewHolder(@NonNull CategoryViewHolder holder, int position, @NonNull Category model) {
        holder.bindCategory(model);
    }

    @NonNull
    @Override
    public CategoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_shop_category, parent, false);
        return new CategoryAdapter.CategoryViewHolder(view);
    }

    public class CategoryViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.category_image)
        ImageView categoryImage;

        @BindView(R.id.category_name)
        TextView categoryName;

        public CategoryViewHolder(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);

            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && itemClickListener != null) {
                    itemClickListener.onItemClick(getSnapshots().getSnapshot(position), position);
                }
            });
        }

        public void bindCategory(Category category) {
            categoryName.setText(category.getName());

            Picasso.get()
                    .load(category.getImage())
                    .centerCrop()
                    .error(R.drawable.ic_baseline_image_24)
                    .placeholder(R.drawable.ic_baseline_image_24)
                    .into(categoryImage);
        }
    }
}
