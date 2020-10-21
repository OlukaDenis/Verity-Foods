package com.verityfoods.viewholders;

import android.content.Context;
import android.view.View;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.smarteist.autoimageslider.SliderViewAdapter;
import com.verityfoods.R;
import com.verityfoods.data.model.Deal;

import butterknife.ButterKnife;

public class DealsViewHolder extends SliderViewAdapter.ViewHolder {

    private ImageView dealImage;
    private Context context;

    public DealsViewHolder(@NonNull View itemView, Context context) {
        super(itemView);
        this.context = context;
        ButterKnife.bind(this, itemView);
        dealImage = itemView.findViewById(R.id.deal_image);
    }

    public void bindSlider(Deal deal) {
        Glide.with(context)
                .load(deal.getImage())
                .centerCrop()
                .error(R.drawable.ic_baseline_image_24)
                .placeholder(R.drawable.ic_baseline_image_24)
                .into(dealImage);
    }
}
