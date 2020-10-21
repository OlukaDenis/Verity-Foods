package com.verityfoods.data.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.smarteist.autoimageslider.SliderViewAdapter;
import com.verityfoods.R;
import com.verityfoods.data.model.ProductSlider;
import com.verityfoods.utils.Vars;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class BannerSliderAdapter extends SliderViewAdapter<BannerSliderViewHolder> {
    private List<ProductSlider> productSliders = new ArrayList<>();
    private Vars vars;
    private Context context;

    public BannerSliderAdapter(Vars vars, Context context) {
        this.vars = vars;
        this.context = context;
    }

    public void renewItems(List<ProductSlider> sliderItems) {
        this.productSliders = sliderItems;
        notifyDataSetChanged();
    }

    public void deleteItem(int position) {
        this.productSliders.remove(position);
        notifyDataSetChanged();
    }

    public void addItem(ProductSlider sliderItem) {
        this.productSliders.add(sliderItem);
        notifyDataSetChanged();
    }

    @Override
    public BannerSliderViewHolder onCreateViewHolder(ViewGroup parent) {
        View inflate = LayoutInflater.from(parent.getContext()).inflate(R.layout.sliding_banner_layout, null);
        return new BannerSliderViewHolder(inflate, vars);
    }

    @Override
    public void onBindViewHolder(BannerSliderViewHolder viewHolder, int position) {
        ProductSlider image = productSliders.get(position);
        if (image != null){
            viewHolder.bindSlidingImage(image);
        }
    }

    @Override
    public int getCount() {
        return productSliders==null ? 0 : productSliders.size();
    }
}

class BannerSliderViewHolder extends SliderViewAdapter.ViewHolder {
    @BindView(R.id.sliding_image)
    ImageView image;

    private Vars vars;

    public BannerSliderViewHolder(View itemView, Vars vars) {
        super(itemView);

        ButterKnife.bind(this, itemView);
        this.vars = vars;
    }

    public  void bindSlidingImage(ProductSlider slidingImage) {

        Glide.with(vars.context.getApplicationContext())
                .load(slidingImage.getImage())
                .centerCrop()
                .error(R.drawable.ic_baseline_image_24)
                .placeholder(R.drawable.ic_baseline_image_24)
                .into(image);
    }
}