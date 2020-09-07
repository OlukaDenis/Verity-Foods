package com.verityfoods.data.adapters;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;

import com.verityfoods.R;
import com.verityfoods.utils.Globals;
import com.verityfoods.viewholders.BrandViewHolder;

import java.util.List;

public class BrandsAdapter extends RecyclerView.Adapter<BrandViewHolder> {
    private static final String TAG = "BrandsAdapter";
    private List<String> brandList;
    private Activity activity;
    private NavController navController;

    public BrandsAdapter(List<String> brandList, Activity activity) {
        Log.d(TAG, "BrandsAdapter called ....: ");
        this.brandList = brandList;
        this.activity = activity;
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
        String str = brandList.get(position);

        if (str != null) {
            holder.bindBrand(str);
        }

        holder.itemView.setOnClickListener(view -> {
            Bundle bundle = new Bundle();
            bundle.putString(Globals.SELECTED_BRAND_OBJ, str);
            navController.navigate(R.id.navigation_brand_products, bundle);
        });
    }

    @Override
    public int getItemCount() {
        return brandList == null ? 0 : brandList.size();
    }
}
