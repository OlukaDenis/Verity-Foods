package com.verityfoods.viewholders;

import android.app.Activity;
import android.app.ProgressDialog;
import android.graphics.Paint;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.badge.BadgeDrawable;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.verityfoods.R;
import com.verityfoods.data.adapters.VariablesAdapter;
import com.verityfoods.data.model.Cart;
import com.verityfoods.data.model.Product;
import com.verityfoods.data.model.Variable;
import com.verityfoods.utils.AppUtils;
import com.verityfoods.utils.Globals;
import com.verityfoods.utils.Vars;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;

public class ProductViewHolder extends RecyclerView.ViewHolder {
    private static final String TAG = "ProductViewHolder";
    private Activity activity;
    private LinearLayout plusMinusButton;
    public TextView plusButton;
    public TextView minusButton;
    public TextView counterTotal;
    public int value;
    public int index = 0;
    public static int modifiedAmount;
    private Variable variable;
    public TextView productMRP;
    public TextView productPrice;
    public FrameLayout discountLayout;
    public TextView offerValue;
    public RecyclerView variableRecycler;
    public Cart cartProduct = new Cart();

    public Button addToCart;
    private Map<String, Object> cartPath;

    private BadgeDrawable badgeDrawable;
    private BottomNavigationView bottomNav;
    private BottomSheetDialog detailDialog;

    private Variable selectedVariable;

    private Vars vars;
    public ProgressDialog loading;

    @BindView(R.id.prduct_image)
    ImageView productImage;

    @BindView(R.id.product_name)
    TextView productName;

    @BindView(R.id.product_brand)
    TextView productBrand;

    @BindView(R.id.product_pack)
    TextView productPack;

    @BindView(R.id.not_in_stock_layout)
    RelativeLayout notInStock;

    @BindView(R.id.prd_lower_layout)
    RelativeLayout lowerLayout;

    public ProductViewHolder(@NonNull View itemView, Vars vars, Activity activity) {
        super(itemView);
        this.vars = vars;
        this.activity = activity;
        loading = new ProgressDialog(activity);
        loading.setMessage("Adding to cart ...");
        detailDialog = new BottomSheetDialog(activity);

        cartPath = new HashMap<>();
        cartPath.put("name", "Cart");
        selectedVariable = new Variable();

        bottomNav = activity.findViewById(R.id.bottom_navigation);
        if (bottomNav != null) {
            badgeDrawable = bottomNav.getBadge(R.id.navigation_cart);
        }

        ButterKnife.bind(this, itemView);
        plusButton = itemView.findViewById(R.id.plus_btn);
        minusButton = itemView.findViewById(R.id.minus_btn);
        counterTotal = itemView.findViewById(R.id.counter_total);
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

        value = Integer.parseInt(counterTotal.getText().toString());
        plusButton.setOnClickListener(view -> {
            int p = Integer.parseInt(counterTotal.getText().toString());
            counterTotal.setText(String.valueOf(p += 1));
            value = Integer.parseInt(counterTotal.getText().toString());
        });

        minusButton.setOnClickListener(view -> {
            int m = Integer.parseInt(counterTotal.getText().toString());
            if (m > 1) {
                counterTotal.setText(String.valueOf(m -= 1));
                value = Integer.parseInt(counterTotal.getText().toString());
            }
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

        if (product.getImage().isEmpty() || product.getImage() == null) {
            Glide.with(activity)
                    .load(R.drawable.ic_baseline_image_24)
                    .centerCrop()
                    .placeholder(R.drawable.ic_baseline_image_24)
                    .into(productImage);
        } else {
            Glide.with(activity)
                    .load(product.getImage())
                    .centerCrop()
                    .error(R.drawable.ic_baseline_image_24)
                    .placeholder(R.drawable.ic_baseline_image_24)
                    .into(productImage);
        }

        if (product.isSimple()) {
            variableRecycler.setVisibility(View.GONE);
            productPack.setVisibility(View.VISIBLE);
            productPack.setText(product.getPack());

            addToCart.setOnClickListener(view -> {
                loading.show();
                int amount;
                if (product.isOffer()) {
                    double discount = (product.getOffer_value() * product.getMrp()) / 100;
                    double m = product.getMrp() - discount;
                    int actual = (int) m;
                    amount = actual * value;
                } else {
                    amount = product.getSelling_price() * value;
                }

                cartProduct.setAmount(amount);
                cartProduct.setCategory_id(product.getCategory_id());
                cartProduct.setCategory_name(product.getCategory_name());
                cartProduct.setProduct_id(product.getUuid());
                cartProduct.setProduct_name(product.getName());
                cartProduct.setMrp(product.getMrp() * value);
                cartProduct.setQuantity(value);
                cartProduct.setProduct_image(product.getImage());
                cartProduct.setCompleted(false);
                cartProduct.setSimple(true);

                addProductCart(product);

            });
        } else {
            Variable defaultVariable = new Variable();
            defaultVariable.setMrp(product.getMrp());
            defaultVariable.setPrice(product.getSelling_price());
            defaultVariable.setQty(product.getPack());
            updateSelectedVariable(defaultVariable);

            productPack.setVisibility(View.GONE);
            variableRecycler.setVisibility(View.VISIBLE);

            populateVariables(product);
        }

        //Product detail
        this.itemView.setOnClickListener(v -> showProductDetail(product));
    }

    private void checkExistingProduct(Product product, String userId, String productID, Cart cart, int qty) {

        vars.verityApp.db.collection(Globals.CART + "/" + userId + "/" + Globals.MY_CART)
                .whereEqualTo("product_id", productID)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        if (Objects.requireNonNull(task.getResult()).size() > 0) {
                            for (QueryDocumentSnapshot document : Objects.requireNonNull(task.getResult())) {
                                Cart existingCart = document.toObject(Cart.class);
                                existingCart.setMrp((product.getMrp() * qty) + existingCart.getMrp());

                                if (product.isOffer()) {
                                    double discount = (product.getOffer_value() * product.getSelling_price()) / 100;
                                    double m = product.getSelling_price() - discount;
                                    int actual = (int) m;

                                    existingCart.setAmount((actual * qty + existingCart.getAmount()));
                                } else {
                                    existingCart.setAmount((product.getSelling_price()) * qty + existingCart.getAmount());
                                }

                                existingCart.setQuantity(qty + existingCart.getQuantity());

                                vars.verityApp.db.collection(Globals.CART)
                                        .document(userId)
                                        .collection(Globals.MY_CART)
                                        .document(document.getId())
                                        .set(existingCart);

                                Toast.makeText(activity, "Product added to Cart", Toast.LENGTH_SHORT).show();
                                loading.dismiss();
                            }
                        } else {
                            createNewCartProduct(userId, cart);
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    vars.verityApp.crashlytics.recordException(e);
                    Log.e(TAG, "Error while adding to cart: ",e );
                });
    }

    private void checkVariableExistingProduct(Product product, String userId, String productID, Cart cart, int qty) {
        vars.verityApp.db.collection(Globals.CART + "/" + userId + "/" + Globals.MY_CART)
                .whereEqualTo("product_id", productID)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {

                        if (Objects.requireNonNull(task.getResult()).size() > 0) {

                            for (QueryDocumentSnapshot document : Objects.requireNonNull(task.getResult())) {
                                Cart existingCart = document.toObject(Cart.class);
                                Log.d(TAG, "Found doc: "+document.getId());
                                existingCart.setMrp((product.getMrp() * qty) + existingCart.getMrp());
//
                                if (existingCart.getPack().equals(product.getPack())) {
                                    if (product.isOffer()) {
                                        double discount = (product.getOffer_value() * product.getSelling_price()) / 100;
                                        double m = product.getSelling_price() - discount;
                                        int actual = (int) m;

                                        existingCart.setAmount((actual * qty + existingCart.getAmount()));
                                    } else {
                                        existingCart.setAmount((product.getSelling_price() * qty + existingCart.getAmount()));
                                    }

                                    existingCart.setQuantity(qty + existingCart.getQuantity());
//
                                    updateExistingVariable(userId, document.getId(), existingCart);
                                } else {
                                    createNewVariableCartProduct(userId, cart);
                                }
                            }
                        } else {
                            createNewCartProduct(userId, cart);
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    vars.verityApp.crashlytics.recordException(e);
                    Log.e(TAG, "Error while adding to cart: ",e );
                });
    }

    private void updateExistingVariable(String userId, String docId, Cart existingCart) {
        Log.d(TAG, "Product pack to update: "+existingCart.getPack());
        Log.d(TAG, "Doc ID: "+docId);
        vars.verityApp.db.collection(Globals.CART + "/" + userId + "/" + Globals.MY_CART)
                .whereEqualTo("pack", existingCart.getPack())
                .get()
                .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                if (Objects.requireNonNull(task.getResult()).size() > 0) {

                                    vars.verityApp.db.collection(Globals.CART)
                                            .document(userId)
                                            .collection(Globals.MY_CART)
                                            .document(docId)
                                            .set(existingCart)
                                            .addOnSuccessListener(aVoid -> {
                                                Toast.makeText(activity, "Cart product updated", Toast.LENGTH_SHORT).show();
                                                loading.dismiss();
                                            });
                                } else {
                                    Log.d(TAG, "no update: ");
                                }
                            }
                        });
    }

    private void createNewVariableCartProduct(String userId, Cart cart) {
        Log.d(TAG, "Pack to create is: "+cart.getPack());
        vars.verityApp.db.collection(Globals.CART + "/" + userId + "/" + Globals.MY_CART)
                .whereEqualTo("pack", cart.getPack())
                .get()
                .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                if (Objects.requireNonNull(task.getResult()).size() == 0) {
                                    Log.d(TAG, "Add here: "+task.getResult().size());
                                    createNewCartProduct(userId, cart);
                                }
                            }
                        });
    }

    private void createNewCartProduct(String userId, Cart cart) {
        
        vars.verityApp.db.collection(Globals.CART)
                .document(userId)
                .collection(Globals.MY_CART)
                .add(cart)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(activity, "Product added to Cart", Toast.LENGTH_SHORT).show();
                    updateCartCount();
                    loading.dismiss();
                })
                .addOnFailureListener(e -> {
                    vars.verityApp.crashlytics.recordException(e);
                    Log.e(TAG, "Error while adding to cart:: ",e );
                    loading.dismiss();
                });
    }


    public void updateCartCount() {
        if (bottomNav != null) {
            vars.verityApp.db.collection(Globals.CART)
                    .document(vars.getShoppingID())
                    .collection(Globals.MY_CART)
                    .get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        int count = queryDocumentSnapshots.size();
                        badgeDrawable.setNumber(count);
                    })
                    .addOnFailureListener(e -> {
                        vars.verityApp.crashlytics.recordException(e);
                        Log.e(TAG, "Error while getting cart count: ", e);
                    });
        }
    }

    public void addProductCart(Product product) {
        vars.verityApp.db.collection(Globals.CART)
                .document(vars.getShoppingID())
                .set(cartPath)
                .addOnSuccessListener(aVoid -> {
                    if (product.isSimple()) {
                        cartProduct.setPack(product.getPack());
                        checkExistingProduct(product, vars.getShoppingID(), cartProduct.getProduct_id(), cartProduct, value);
                    } else {
                        cartProduct.setPack(selectedVariable.getQty());
                        checkVariableExistingProduct(product, vars.getShoppingID(), cartProduct.getProduct_id(), cartProduct, value);
                    }
                });
    }

    private void populateVariables(Product productModel) {
        VariablesAdapter variableAdapter = new VariablesAdapter(productModel.getVariables(), activity, this, productModel);
        variableRecycler.setAdapter(variableAdapter);
        variableAdapter.notifyDataSetChanged();
    }

    public void updateSelectedVariable(Variable variable) {
        selectedVariable.setQty(variable.getQty());
        selectedVariable.setPrice(variable.getPrice());
        selectedVariable.setMrp(variable.getMrp());
    }

    private void showProductDetail(Product product) {
        final View view = activity.getLayoutInflater().inflate(R.layout.layout_product_detail, null);

        TextView name = view.findViewById(R.id.product_detail_name);
        TextView price = view.findViewById(R.id.product_detail_price);
        TextView desc = view.findViewById(R.id.product_detail_desc);
        TextView qty = view.findViewById(R.id.product_detail_quantity);
        ImageView image = view.findViewById(R.id.product_detail_image);

        name.setText(product.getName());
        price.setText(AppUtils.formatCurrency(product.getSelling_price()));
        qty.setText(product.getPack());

        if (product.getDescription() == null) {
            desc.setText("Product description");
        } else {
            desc.setText(product.getDescription());
        }


        if (product.getImage().isEmpty()) {
            Glide.with(activity)
                    .load(R.drawable.ic_baseline_image_24)
                    .centerCrop()
                    .placeholder(R.drawable.ic_baseline_image_24)
                    .into(image);
        } else {
            Glide.with(activity)
                    .load(product.getImage())
                    .centerCrop()
                    .error(R.drawable.ic_baseline_image_24)
                    .placeholder(R.drawable.ic_baseline_image_24)
                    .into(image);
        }

        detailDialog.setContentView(view);
        detailDialog.show();
    }
}
