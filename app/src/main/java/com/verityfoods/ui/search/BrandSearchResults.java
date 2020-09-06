package com.verityfoods.ui.search;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.navigation.NavController;
import androidx.paging.PagedList;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.firebase.ui.firestore.paging.FirestorePagingAdapter;
import com.firebase.ui.firestore.paging.FirestorePagingOptions;
import com.firebase.ui.firestore.paging.LoadingState;
import com.google.android.material.badge.BadgeDrawable;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.verityfoods.R;
import com.verityfoods.data.model.Cart;
import com.verityfoods.data.model.Product;
import com.verityfoods.data.model.Variable;
import com.verityfoods.ui.bottomviews.shop.ShopViewModel;
import com.verityfoods.utils.AppUtils;
import com.verityfoods.utils.Globals;
import com.verityfoods.utils.Vars;
import com.verityfoods.viewholders.ProductViewHolder;
import com.verityfoods.viewholders.VariableViewHolder;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class BrandSearchResults extends AppCompatActivity {
    private static final String TAG = "BrandSearchResults";
    private Vars vars;
    private Product product;
    private RecyclerView productRecycler;
    private FirestorePagingAdapter<Product, ProductViewHolder> adapter;
    private FirestorePagingAdapter<Variable, VariableViewHolder> variableAdapter;
    private LinearLayoutManager layoutManager;
    private ProgressDialog loading;

    private ShopViewModel shopViewModel;

    private PagedList.Config config;
    private Variable variable;

    private int modifiedAmount;
    private Map<String, Object> cartPath;
    private String brandName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_brand_search_results);

        Toolbar toolbar = findViewById(R.id.search_toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitle("");
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        vars = new Vars(this);
        loading = new ProgressDialog(this);
        loading.setMessage("Adding to cart ...");

        layoutManager = new LinearLayoutManager(this);
        productRecycler = findViewById(R.id.products_recycler);
        productRecycler.setLayoutManager(layoutManager);

       brandName = Objects.requireNonNull(getIntent().getExtras()).getString(Globals.BRAND_SEARCH_RESULT);
        if (brandName != null) {
            setTitle(brandName);
        }

        config = new PagedList.Config.Builder()
                .setEnablePlaceholders(false)
                .setPrefetchDistance(10)
                .setPageSize(20)
                .build();

        cartPath = new HashMap<>();
        cartPath.put("name", "Cart");

        populateProducts();
    }

    private void checkExistingProduct(String userId, String productID, Cart cart, int qty) {

        vars.verityApp.db.collection(Globals.CART + "/" + userId + "/" + Globals.MY_CART)
                .whereEqualTo("product_id", productID)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        if (Objects.requireNonNull(task.getResult()).size() > 0) {

                            for (QueryDocumentSnapshot document : Objects.requireNonNull(task.getResult())) {
                                Cart cartProduct = document.toObject(Cart.class);

                                if (product.isOffer()) {
                                    double discount = (product.getOffer_value() * product.getSelling_price()) / 100;
                                    double m = product.getSelling_price() - discount;
                                    int actual = (int) m;

                                    cartProduct.setAmount((actual * qty + cartProduct.getAmount()));
                                } else {
                                    cartProduct.setAmount((product.getSelling_price() * qty + cartProduct.getAmount()));
                                }

                                cartProduct.setQuantity(qty + cartProduct.getQuantity());
                                vars.verityApp.db.collection(Globals.CART)
                                        .document(userId)
                                        .collection(Globals.MY_CART)
                                        .document(document.getId())
                                        .set(cartProduct);

                                Toast.makeText(this, "Product added to Cart", Toast.LENGTH_SHORT).show();
                                loading.dismiss();
                            }
                        } else {
                            vars.verityApp.db.collection(Globals.CART)
                                    .document(userId)
                                    .collection(Globals.MY_CART)
                                    .add(cart)
                                    .addOnSuccessListener(documentReference -> {
                                        Toast.makeText(this, "Product added to Cart", Toast.LENGTH_SHORT).show();
                                        loading.dismiss();
                                    })
                                    .addOnFailureListener(e -> {
                                        vars.verityApp.crashlytics.recordException(e);
                                        Log.e(TAG, "Error while adding to cart:: ",e );
                                        loading.dismiss();
                                    });
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    vars.verityApp.crashlytics.recordException(e);
                    Log.e(TAG, "Error while adding to cart: ",e );
                });
    }

    private void populateProducts() {

        Query catQuery = vars.verityApp.db
                .collectionGroup(Globals.PRODUCTS)
                .whereEqualTo("brand", brandName);

        FirestorePagingOptions<Product> options = new FirestorePagingOptions.Builder<Product>()
                .setLifecycleOwner(this)
                .setQuery(catQuery, config, snapshot -> {
                    product = snapshot.toObject(Product.class);
                    assert product != null;
                    product.setUuid(snapshot.getId());
                    return product;
                })
                .build();

        adapter = new FirestorePagingAdapter<Product, ProductViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull ProductViewHolder holder, int position, @NonNull Product model) {
                holder.bindProduct(model);

                if (model.isSimple()) {
                    holder.addToCart.setOnClickListener(view -> {
                        loading.show();

                        int amount;
                        if (model.isOffer()) {
                            double discount = (model.getOffer_value() * model.getMrp()) / 100;
                            double m = model.getMrp() - discount;
                            int actual = (int) m;
                            amount = actual * holder.value;
                        } else {
                            amount = model.getSelling_price() * holder.value;
                        }

                        Cart cartProduct = new Cart(
                                model.getCategory_id(),
                                model.getCategory_name(),
                                model.getUuid(),
                                model.getName(),
                                model.getImage(),
                                model.getMrp(),
                                holder.value,
                                amount
                        );

                        addProductCart(cartProduct, holder);

                    });
                } else {
                    populateVariables(holder, model);
                }
            }

            @NonNull
            @Override
            public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_products, parent, false);
                return new ProductViewHolder(view, vars);
            }

            @Override
            protected void onError(@NonNull Exception e) {
                super.onError(e);
                Toast.makeText(getApplicationContext(), "Error", Toast.LENGTH_SHORT).show();
            }

            @Override
            protected void onLoadingStateChanged(@NonNull LoadingState state) {
                switch (state) {
                    case LOADING_INITIAL:

                        break;

                    case LOADING_MORE:
//                        mShimmerViewContainer.setVisibility(View.VISIBLE);
                        break;

                    case LOADED:
//                        mShimmerViewContainer.setVisibility(View.GONE);
                        notifyDataSetChanged();
                        break;

                    case ERROR:
                        Toast.makeText(getApplicationContext(), "Error", Toast.LENGTH_SHORT).show();

//                        mShimmerViewContainer.setVisibility(View.GONE);
                        break;

                    case FINISHED:
//                        mShimmerViewContainer.setVisibility(View.GONE);
                        break;
                }
            }
        };
        productRecycler.setAdapter(adapter);
        adapter.notifyDataSetChanged();
    }

    private void addProductCart(Cart cartProduct, ProductViewHolder holder) {
        vars.verityApp.db.collection(Globals.CART)
                .document(vars.getShoppingID())
                .set(cartPath)
                .addOnSuccessListener(aVoid -> checkExistingProduct(vars.getShoppingID(), cartProduct.getProduct_id(), cartProduct, holder.value));
    }

    private void populateVariables(ProductViewHolder productViewHolder, Product productModel) {
        Query variableQuery = vars.verityApp.db
                .collection(Globals.CATEGORIES)
                .document(productModel.getCategory_id())
                .collection(Globals.SUB_CATEGORIES)
                .document(productModel.getSub_category_id())
                .collection(Globals.PRODUCTS)
                .document(productModel.getUuid())
                .collection(Globals.VARIABLE);

        FirestorePagingOptions<Variable> variableOptions = new FirestorePagingOptions.Builder<Variable>()
                .setLifecycleOwner(this)
                .setQuery(variableQuery, config, snapshot -> {
                    variable = snapshot.toObject(Variable.class);
                    assert variable != null;
                    variable.setUuid(snapshot.getId());
                    return variable;
                })
                .build();

        variableAdapter = new FirestorePagingAdapter<Variable, VariableViewHolder>(variableOptions) {
            @Override
            protected void onBindViewHolder(@NonNull VariableViewHolder holder, int position, @NonNull Variable model) {
                holder.bindVariable(model);

                holder.itemView.setOnClickListener(view -> {
                    calculatePrice(productViewHolder, productModel, model);
                });
            }

            @NonNull
            @Override
            public VariableViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_variable, parent, false);
                return new VariableViewHolder(view, getApplicationContext());
            }

            @Override
            protected void onError(@NonNull Exception e) {
                super.onError(e);
                Toast.makeText(getApplicationContext(), "Error", Toast.LENGTH_SHORT).show();
            }
        };
        productViewHolder.variableRecycler.setAdapter(variableAdapter);
        variableAdapter.notifyDataSetChanged();

        //handle add to cart
        productViewHolder.addToCart.setOnClickListener(view -> {
            loading.show();
            int mAmount = modifiedAmount * productViewHolder.value;
            Log.d(TAG, "populateVariables: "+productViewHolder.value);
            Cart cartProduct = new Cart(
                    productModel.getCategory_id(),
                    productModel.getCategory_name(),
                    productModel.getUuid(),
                    productModel.getName(),
                    productModel.getImage(),
                    productModel.getMrp(),
                    productViewHolder.value,
                    mAmount
            );

            addProductCart(cartProduct, productViewHolder);
        });
    }

    private void calculatePrice(ProductViewHolder holder, Product product, Variable model) {
        if (product.isOffer()) {
            int newMrp = model.getPrice() + 2000;
            holder.productMRP.setText(AppUtils.formatCurrency(newMrp));
            double discount = (product.getOffer_value() * newMrp) / 100;
            double actual = newMrp - discount;
            int m = (int) actual;
            //update the amount
            modifiedAmount = (int) actual;
            holder.productPrice.setText(AppUtils.formatCurrency(m));
        } else {
            holder.productPrice.setText(AppUtils.formatCurrency(model.getPrice()));
            modifiedAmount = model.getPrice();
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == android.R.id.home) {
            finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}