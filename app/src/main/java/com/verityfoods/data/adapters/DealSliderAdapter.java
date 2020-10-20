package com.verityfoods.data.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.smarteist.autoimageslider.SliderViewAdapter;
import com.verityfoods.R;
import com.verityfoods.data.model.Deal;
import com.verityfoods.utils.Vars;
import com.verityfoods.viewholders.DealsViewHolder;

import java.util.ArrayList;
import java.util.List;

public class DealSliderAdapter extends SliderViewAdapter<DealsViewHolder> {
    private List<Deal> dealSliders = new ArrayList<>();
    private Vars vars;
    private Context context;

    public DealSliderAdapter(Vars vars, Context context) {
        this.vars = vars;
        this.context = context;
    }

    public void renewItems(List<Deal> sliderItems) {
        this.dealSliders = sliderItems;
        notifyDataSetChanged();
    }

    public void deleteItem(int position) {
        this.dealSliders.remove(position);
        notifyDataSetChanged();
    }

    public void addItem(Deal sliderItem) {
        this.dealSliders.add(sliderItem);
        notifyDataSetChanged();
    }

    @Override
    public DealsViewHolder onCreateViewHolder(ViewGroup parent) {
        View inflate = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_deals, null);
        return new DealsViewHolder(inflate);
    }

    @Override
    public void onBindViewHolder(DealsViewHolder viewHolder, int position) {
        Deal image = dealSliders.get(position);
        if (image != null){
            viewHolder.bindSlider(image);
        }
    }

    @Override
    public int getCount() {
        return dealSliders ==null ? 0 : dealSliders.size();
    }
}
