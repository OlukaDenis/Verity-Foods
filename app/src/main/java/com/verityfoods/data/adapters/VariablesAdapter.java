package com.verityfoods.data.adapters;

import android.app.Activity;
import android.app.ProgressDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.verityfoods.R;
import com.verityfoods.data.model.Cart;
import com.verityfoods.data.model.Product;
import com.verityfoods.data.model.Variable;
import com.verityfoods.utils.AppUtils;
import com.verityfoods.viewholders.ProductViewHolder;
import com.verityfoods.viewholders.VariableViewHolder;

import java.util.List;

public class VariablesAdapter extends RecyclerView.Adapter<VariableViewHolder> {
    private static final String TAG = "VariablesAdapter";
    private List<Variable> variableList;
    private Activity activity;
    private int index = 0;
    private int modifiedAmount;
    private ProductViewHolder productViewHolder;
    private Product product;

    public VariablesAdapter(List<Variable> variableList, Activity activity, ProductViewHolder productViewHolder, Product product) {
        this.variableList = variableList;
        this.activity = activity;
        this.productViewHolder = productViewHolder;
        this.product = product;
    }

    @NonNull
    @Override
    public VariableViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_variable, parent, false);
        return new VariableViewHolder(view, activity);
    }

    @Override
    public void onBindViewHolder(@NonNull VariableViewHolder holder, int position) {
        Variable variable = variableList.get(position);

        holder.variableName.setOnClickListener(view -> {
            index = position;
            notifyDataSetChanged();
            calculatePrice(variable);
        });

        if (index == position) {
            Log.d(TAG, "Index is equal: "+index);
            holder.variableName.setBackground(activity.getResources().getDrawable(R.drawable.variable_filled_bg));
            holder.variableName.setTextColor(activity.getResources().getColor(R.color.white));
        } else {
            Log.d(TAG, "Index not equal: ");
            holder.variableName.setBackground(activity.getResources().getDrawable(R.drawable.varible_bg));
            holder.variableName.setTextColor(activity.getResources().getColor(R.color.black));
        }

        if (!product.isSimple()) {
            productViewHolder.addToCart.setOnClickListener(v -> {
                productViewHolder.loading.show();
                int mAmount = modifiedAmount * productViewHolder.value;
                Cart cart = new Cart(
                        product.getCategory_id(),
                        product.getCategory_name(),
                        product.getUuid(),
                        product.getName(),
                        product.getImage(),
                        product.getMrp(),
                        productViewHolder.value,
                        mAmount
                );
                productViewHolder.addProductCart(cart, product);
            });
        }

        if (variable != null) {
            holder.bindVariable(variable);
        }
    }

    @Override
    public int getItemCount() {
        return variableList == null ? 0 : variableList.size();
    }

    private void changeColor(VariableViewHolder holder, int position) {
        if (index == position) {
            Log.d(TAG, "Index is equal: "+index);
            holder.variableName.setBackground(activity.getResources().getDrawable(R.drawable.variable_filled_bg));
            holder.variableName.setTextColor(activity.getResources().getColor(R.color.white));
        } else {
            Log.d(TAG, "Index not equal: ");
            holder.variableName.setBackground(activity.getResources().getDrawable(R.drawable.varible_bg));
            holder.variableName.setTextColor(activity.getResources().getColor(R.color.black));
        }
    }

    private void calculatePrice(Variable model) {
        if (product.isOffer()) {
            int newMrp = model.getPrice() + 2000;
            productViewHolder.productMRP.setText(AppUtils.formatCurrency(newMrp));
            double discount = (product.getOffer_value() * newMrp) / 100;
            double actual = newMrp - discount;
            int m = (int) actual;
            //update the amount
            modifiedAmount = (int) actual;
            productViewHolder.productPrice.setText(AppUtils.formatCurrency(m));
        } else {
            productViewHolder.productPrice.setText(AppUtils.formatCurrency(model.getPrice()));
            modifiedAmount = model.getPrice();
        }
    }

}
