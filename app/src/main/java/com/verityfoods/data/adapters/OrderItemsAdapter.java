package com.verityfoods.data.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;
import com.verityfoods.R;
import com.verityfoods.data.model.Cart;
import com.verityfoods.utils.AppUtils;

import java.util.List;

public class OrderItemsAdapter extends RecyclerView.Adapter<OrderItemsViewHolder> {
    private List<Cart> itemList;

    public OrderItemsAdapter(List<Cart> itemList) {
        this.itemList = itemList;
    }

    @NonNull
    @Override
    public OrderItemsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_detail_order_item, parent, false);
        return new OrderItemsViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OrderItemsViewHolder holder, int position) {
        Cart cart = itemList.get(position);

        if (cart != null) {
            holder.bindOrderTo(cart);
        }
    }

    @Override
    public int getItemCount() {
        return itemList == null ? 0 : itemList.size();
    }
}

class OrderItemsViewHolder extends RecyclerView.ViewHolder {
    private ImageView itemImage;
    private TextView category;
    private TextView name;
    private TextView price;
    private TextView quantity;

    public OrderItemsViewHolder(@NonNull View itemView) {
        super(itemView);

        itemImage = itemView.findViewById(R.id.detail_order_item_image);
        category = itemView.findViewById(R.id.detail_order_item_category);
        name = itemView.findViewById(R.id.detail_order_item_name);
        price = itemView.findViewById(R.id.detail_order_item_price);
        quantity = itemView.findViewById(R.id.detail_order_item_quantity);
    }

    public void bindOrderTo(Cart cart) {
        name.setText(cart.getProduct_name());
        category.setText(cart.getCategory_name());
        price.setText(AppUtils.formatCurrency(cart.getAmount()));

        String qty = "Quantity: " + cart.getQuantity();
        quantity.setText(qty);
        Picasso.get()
                .load(cart.getProduct_image())
                .error(R.drawable.ic_baseline_image_24)
                .placeholder(R.drawable.ic_baseline_image_24)
                .into(itemImage);
    }
}

