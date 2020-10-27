package com.verityfoods.data.adapters;

import android.app.Activity;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;

import com.facebook.shimmer.ShimmerFrameLayout;
import com.verityfoods.R;
import com.verityfoods.data.model.Brand;
import com.verityfoods.utils.Globals;
import com.verityfoods.viewholders.BrandViewHolder;

import java.text.Collator;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class BrandsAdapter extends RecyclerView.Adapter<BrandViewHolder> {
    private static final String TAG = "BrandsAdapter";
    private List<Brand> brandList;
    private NavController navController;
    private ShimmerFrameLayout shimmerLayout;

    public BrandsAdapter(List<Brand> brandList, Activity activity, ShimmerFrameLayout shimmerLayout) {
        Log.d(TAG, "BrandsAdapter called ....: ");
        this.brandList = brandList;
        this.shimmerLayout = shimmerLayout;
        navController = Navigation.findNavController(activity, R.id.nav_host_fragment);
        Log.d(TAG, "BrandsAdapter: "+brandList.size());
    }

    @NonNull
    @Override
    public BrandViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_brand, parent, false);
        return new BrandViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BrandViewHolder holder, int position) {
        Brand str = brandList.get(position);

        if (str != null) {
            holder.bindBrand(str);
        }

        holder.itemView.setOnClickListener(view -> {
            Bundle bundle = new Bundle();
            bundle.putString(Globals.SELECTED_BRAND_OBJ, str.getName());
            navController.navigate(R.id.navigation_brand_products, bundle);
        });

        shimmerLayout.setVisibility(View.GONE);
    }

    @Override
    public int getItemCount() {
        return brandList == null ? 0 : brandList.size();
    }
}
