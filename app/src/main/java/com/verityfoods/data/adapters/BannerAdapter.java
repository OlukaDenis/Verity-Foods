package com.verityfoods.data.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.verityfoods.R;
import com.verityfoods.data.model.ProductSlider;
import com.verityfoods.viewholders.ProductSliderViewHolder;

import java.util.List;

import static com.verityfoods.utils.Globals.MAX_LIST_SIZE;

public class BannerAdapter extends RecyclerView.Adapter<ProductSliderViewHolder> {
    private Context context;
    private List<ProductSlider> sliderList;
    public BannerAdapter(Context context,List<ProductSlider> sliderList){
        this.context=context;
        this.sliderList = sliderList;
        MAX_LIST_SIZE = this.sliderList.size() + 100;
    }

    @NonNull
    @Override
    public ProductSliderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view= LayoutInflater.from(context).inflate(R.layout.layout_product_slider,parent,false);
        return new ProductSliderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductSliderViewHolder holder, int position) {
        ProductSlider slider = sliderList.get(position%sliderList.size());
        if (slider != null) {
            holder.bindSlider(slider);
        }
    }

    @Override
    public int getItemCount() {
        return sliderList == null ? 0 : MAX_LIST_SIZE;
    }
}