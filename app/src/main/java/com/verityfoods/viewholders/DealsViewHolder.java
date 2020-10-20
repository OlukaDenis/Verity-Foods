package com.verityfoods.viewholders;

import android.view.View;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;
import com.verityfoods.R;
import com.verityfoods.data.model.Deal;

import butterknife.ButterKnife;

public class DealsViewHolder extends RecyclerView.ViewHolder {

    private ImageView dealImage;

    public DealsViewHolder(@NonNull View itemView) {
        super(itemView);
        ButterKnife.bind(this, itemView);
        dealImage = itemView.findViewById(R.id.deal_image);
    }

    public void bindSlider(Deal deal) {
        Picasso.get()
                .load(deal.getImage())
                .error(R.drawable.ic_baseline_image_24)
                .placeholder(R.drawable.ic_baseline_image_24)
                .into(dealImage);
    }
}
