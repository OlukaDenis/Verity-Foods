package com.verityfoods.viewholders;

import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.smarteist.autoimageslider.SliderViewAdapter;
import com.verityfoods.R;
import com.verityfoods.data.model.Deal;

import butterknife.ButterKnife;

public class DealsViewHolder extends SliderViewAdapter.ViewHolder {

    private ImageView dealImage;
    private Activity context;
    private NavController navController;

    public DealsViewHolder(@NonNull View itemView, Activity activity) {
        super(itemView);
        this.context = activity;
        ButterKnife.bind(this, itemView);
        dealImage = itemView.findViewById(R.id.deal_image);
        navController = Navigation.findNavController(activity, R.id.nav_host_fragment);

        itemView.setOnClickListener(v ->  navController.navigate(R.id.navigation_offers));
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
