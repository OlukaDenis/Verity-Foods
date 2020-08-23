package com.verityfoods.data.adapters;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;

import com.verityfoods.R;
import com.verityfoods.data.model.Deal;
import com.verityfoods.viewholders.DealsViewHolder;

import java.util.List;

import static com.verityfoods.utils.Globals.MAX_LIST_SIZE;

public class DealsAdapter extends RecyclerView.Adapter<DealsViewHolder> {
    private Activity activity;
    private List<Deal> dealList;
    private NavController navController;

    public DealsAdapter(Activity activity, List<Deal> sliderList){
        this.activity = activity;
        this.dealList = sliderList;
        navController = Navigation.findNavController(activity, R.id.nav_host_fragment);
        MAX_LIST_SIZE = this.dealList.size() + 50;
    }

    @NonNull
    @Override
    public DealsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view= LayoutInflater.from(activity).inflate(R.layout.layout_deals,parent,false);
        return new DealsViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DealsViewHolder holder, int position) {
        Deal slider = dealList.get(position% dealList.size());
        if (slider != null) {
            holder.bindSlider(slider);
        }

        holder.itemView.setOnClickListener(view -> navController.navigate(R.id.navigation_offers));
    }

    @Override
    public int getItemCount() {
        return dealList == null ? 0 : MAX_LIST_SIZE;
    }
}