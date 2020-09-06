package com.verityfoods.data.adapters;

import android.app.Activity;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.verityfoods.R;
import com.verityfoods.ui.search.BrandSearchResults;
import com.verityfoods.utils.Globals;

import java.util.List;

public class BrandSearchAdapter extends RecyclerView.Adapter<BrandSearchViewHolder> {
    private static final String TAG = "BrandSearchAdapter";
    private List<String> brandSearchList;
    private Activity activity;

    public BrandSearchAdapter(List<String> brandSearchList, Activity activity) {
        Log.d(TAG, "BrandSearchAdapter called...: "+brandSearchList.size());
        this.brandSearchList = brandSearchList;
        this.activity = activity;
    }

    @NonNull
    @Override
    public BrandSearchViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_brand_search_item, parent, false);
        return new BrandSearchViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BrandSearchViewHolder holder, int position) {
        String str = brandSearchList.get(position);

        if (str != null) {
           holder.bindSearchItem(str);
        }

        holder.itemView.setOnClickListener(view -> {
            Intent intent = new Intent(activity, BrandSearchResults.class);
            intent.putExtra(Globals.BRAND_SEARCH_RESULT, str);
            activity.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return brandSearchList == null ? 0 : brandSearchList.size();
    }
}

    class BrandSearchViewHolder extends RecyclerView.ViewHolder {
        private TextView brandName;

        public BrandSearchViewHolder(@NonNull View itemView) {
            super(itemView);
            brandName = itemView.findViewById(R.id.brand_search_item);
        }

        public void bindSearchItem(String search) {
            String mString = search + " in brands";
            brandName.setText(mString);
        }
    }
