package com.verityfoods.viewholders;

import android.graphics.Paint;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.squareup.picasso.Picasso;
import com.verityfoods.R;
import com.verityfoods.data.model.Product;
import com.verityfoods.data.model.Variable;
import com.verityfoods.utils.AppUtils;
import com.verityfoods.utils.Globals;
import com.verityfoods.utils.Vars;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;

public class ProductViewHolder extends RecyclerView.ViewHolder {
    private static final String TAG = "ProductViewHolder";
    private LinearLayout plusMinusButton;
    public TextView plusButton;
    public TextView minusButton;
    public TextView total;
    public int value;
    private Variable variable;

    private Vars vars;
    @BindView(R.id.prduct_image)
    ImageView productImage;

    @BindView(R.id.product_name)
    TextView productName;

    @BindView(R.id.product_brand)
    TextView productBrand;

    public TextView productMRP;
    public TextView productPrice;
    public FrameLayout discountLayout;
    public TextView offerValue;

    @BindView(R.id.product_pack)
    TextView productPack;

    @BindView(R.id.not_in_stock_layout)
    RelativeLayout notInStock;

    @BindView(R.id.prd_lower_layout)
    RelativeLayout lowerLayout;

    public RecyclerView variableRecycler;

    public Button addToCart;
    private ArrayAdapter<String> variableAdapter;
    private List<String> variableList;
    private String[] variableArray;

    public ProductViewHolder(@NonNull View itemView, Vars vars) {
        super(itemView);
        this.vars = vars;
        variableList = new ArrayList<>();

        ButterKnife.bind(this, itemView);
        plusButton = itemView.findViewById(R.id.plus_btn);
        minusButton = itemView.findViewById(R.id.minus_btn);
        total = itemView.findViewById(R.id.counter_total);
        plusMinusButton = itemView.findViewById(R.id.plus_minus_button);
        addToCart = itemView.findViewById(R.id.add_to_cart);
        productPrice = itemView.findViewById(R.id.product_price);
        discountLayout = itemView.findViewById(R.id.discount_layout);
        offerValue = itemView.findViewById(R.id.offer_value);
        productMRP = itemView.findViewById(R.id.product_mrp);

        LinearLayoutManager layoutManager = new LinearLayoutManager(vars.context.getApplicationContext(), LinearLayoutManager.HORIZONTAL, false);
        variableRecycler = itemView.findViewById(R.id.variable_recycler);
        variableRecycler.setLayoutManager(layoutManager);
    }

    public void bindProduct(Product product) {

        value = Integer.parseInt(total.getText().toString());
        plusButton.setOnClickListener(view -> {
            int p = Integer.parseInt(total.getText().toString());
            total.setText(String.valueOf(p += 1));
            value = Integer.parseInt(total.getText().toString());
            Log.d(TAG, "on Plus: "+value);
        });

        minusButton.setOnClickListener(view -> {
            int m = Integer.parseInt(total.getText().toString());
            if (m > 1) {
                total.setText(String.valueOf(m -= 1));
                value = Integer.parseInt(total.getText().toString());
            }
            Log.d(TAG, "on Minus: "+value);
        });

        if (product.isOffer()) {
            discountLayout.setVisibility(View.VISIBLE);
            offerValue.setText(AppUtils.formatOffer(product.getOffer_value()));

            double discount = (product.getOffer_value() * product.getMrp()) / 100;
            double actual = product.getMrp() - discount;
            int m = (int) actual;
            productPrice.setText(AppUtils.formatCurrency(m));
        } else {
            discountLayout.setVisibility(View.GONE);
            productPrice.setText(AppUtils.formatCurrency(product.getSelling_price()));
        }

        if (!product.isStock()) {
            notInStock.setVisibility(View.VISIBLE);
            lowerLayout.setVisibility(View.GONE);
            discountLayout.setVisibility(View.GONE);
        } else {
            notInStock.setVisibility(View.GONE);
            lowerLayout.setVisibility(View.VISIBLE);
        }

        productName.setText(product.getName());
        productBrand.setText(product.getBrand());
        productMRP.setText(AppUtils.formatCurrency(product.getMrp()));
        productMRP.setPaintFlags(productMRP.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);

        Picasso.get()
                .load(product.getImage())
                .error(R.drawable.ic_baseline_image_24)
                .placeholder(R.drawable.ic_baseline_image_24)
                .into(productImage);

        if (product.isSimple()) {
            variableRecycler.setVisibility(View.GONE);
            productPack.setVisibility(View.VISIBLE);
            productPack.setText(product.getPack());
        } else {
            productPack.setVisibility(View.GONE);
            variableRecycler.setVisibility(View.VISIBLE);
        }
    }
}
