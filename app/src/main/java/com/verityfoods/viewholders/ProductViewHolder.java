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

    @BindView(R.id.product_mrp)
    TextView productMRP;

    @BindView(R.id.product_price)
    TextView productPrice;

    @BindView(R.id.discount_layout)
    FrameLayout discountLayout;

    @BindView(R.id.offer_value)
    TextView offerValue;

    @BindView(R.id.product_pack)
    TextView productPack;

    @BindView(R.id.not_in_stock_layout)
    RelativeLayout notInStock;

    @BindView(R.id.prd_lower_layout)
    RelativeLayout lowerLayout;

    @BindView(R.id.product_variable)
    Spinner productVariable;

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
    }

    private void getProductVariables(Product product) {
        if (product.getUuid() != null) {
            Query query = vars.verityApp.db
                    .collection(Globals.CATEGORIES)
                    .document(product.getCategory_id())
                    .collection(Globals.SUB_CATEGORIES)
                    .document(product.getSub_category_id())
                    .collection(Globals.PRODUCTS)
                    .document(product.getUuid())
                    .collection(Globals.VARIABLE);

            query.get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot snapshot : Objects.requireNonNull(task.getResult())) {
                                variable = snapshot.toObject(Variable.class);
                                String m = AppUtils.formatVariable(variable.getQty(), variable.getPrice());
                                Log.d(TAG, "getProductVariables: " + m);
                                variableList.add(m);
                            }
                        }
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "bindProduct: ", e);
                        vars.verityApp.crashlytics.recordException(e);
                    });
        }
    }

    public void bindProduct(Product product) {
        getProductVariables(product);

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

            double discount = (product.getOffer_value() * product.getSelling_price()) / 100;
            double actual = product.getSelling_price() - discount;
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

        productPack.setText(product.getPack());
        productName.setText(product.getName());
        productBrand.setText(product.getBrand());
        productMRP.setText(AppUtils.formatCurrency(product.getMrp()));
        productMRP.setPaintFlags(productMRP.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
        Log.d(TAG, "It is an offer: "+product.isOffer());

        Picasso.get()
                .load(product.getImage())
                .error(R.drawable.ic_baseline_image_24)
                .placeholder(R.drawable.ic_baseline_image_24)
                .into(productImage);

        if (product.isSimple()) {

        }
//            variableArray = new String[variableList.size()];
//            variableArray = variableList.toArray(variableArray);
//
//            productVariable.setVisibility(View.VISIBLE);
//            variableAdapter = new ArrayAdapter<>(vars.context.getApplicationContext(), android.R.layout.simple_spinner_item, variableArray);
//            variableAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
//            productVariable.setAdapter(variableAdapter);
//            productVariable.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
//                @Override
//                public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
//                    Toast.makeText(vars.context.getApplicationContext(), variableList.get(i), Toast.LENGTH_SHORT).show();
//                }
//
//                @Override
//                public void onNothingSelected(AdapterView<?> adapterView) {
//                    Toast.makeText(vars.context.getApplicationContext(), "Nothing", Toast.LENGTH_SHORT).show();
//                }
//            });
//        } else {
//            productVariable.setVisibility(View.GONE);
//        }
    }
}
