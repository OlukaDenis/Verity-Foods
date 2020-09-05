package com.verityfoods.ui.search;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.paging.PagedList;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.firebase.ui.firestore.paging.FirestorePagingAdapter;
import com.firebase.ui.firestore.paging.FirestorePagingOptions;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.verityfoods.R;
import com.verityfoods.data.model.Cart;
import com.verityfoods.data.model.Product;
import com.verityfoods.data.model.Variable;
import com.verityfoods.utils.AppUtils;
import com.verityfoods.utils.Globals;
import com.verityfoods.utils.Vars;
import com.verityfoods.viewholders.ProductViewHolder;
import com.verityfoods.viewholders.VariableViewHolder;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class SearchActivity extends AppCompatActivity {
    private static final String TAG = "SearchActivity";
    private Vars vars;
    private Product product;

    private LinearLayoutManager layoutManager;
    private RecyclerView searchRecycler;

    private LinearLayoutManager brandLayoutManager;
    private RecyclerView brandSearchRecycler;

    private FirestoreRecyclerAdapter<Product, ProductViewHolder> searchAdapter;
    private FirestoreRecyclerAdapter<Product, ProductViewHolder> brandSearchAdapter;
    private FirestorePagingAdapter<Variable, VariableViewHolder> variableAdapter;

    private PagedList.Config config;
    private Variable variable;
    private ProgressDialog loading;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        vars = new Vars(this);
        loading = new ProgressDialog(this);

        layoutManager = new LinearLayoutManager(this);
        searchRecycler = findViewById(R.id.product_search_recycler);
        searchRecycler.setLayoutManager(layoutManager);

        brandSearchRecycler = findViewById(R.id.brand_searh_recycler);
        brandLayoutManager = new LinearLayoutManager(this);
        brandSearchRecycler.setLayoutManager(brandLayoutManager);

        Toolbar toolbar = findViewById(R.id.search_toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitle("");
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        config = new PagedList.Config.Builder()
                .setEnablePlaceholders(false)
                .setPrefetchDistance(10)
                .setPageSize(20)
                .build();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.search_menu, menu);
        MenuItem searchItem = menu.findItem(R.id.action_searchview);
        SearchView searchView = (SearchView) searchItem.getActionView();
        ImageView searchIcon = searchView.findViewById(androidx.appcompat.R.id.search_mag_icon);
        searchIcon.setImageDrawable(null);
        searchView.setQueryHint("Search");
        searchView.setIconifiedByDefault(false);
        searchItem.expandActionView();

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                searchProducts(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                searchProducts(newText);
                return false;
            }
        });

        return true;
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

    private void searchProducts(String text) {

        if (text.length() > 0) {
            text = text.substring(0, 1).toUpperCase() + text.substring(1).toLowerCase();
            searchByName(text);
            searchByBrand(text);
        }
    }

    private void searchByBrand(String brand) {
        Query searchQuery = vars.verityApp.db
                .collectionGroup(Globals.PRODUCTS)
                .whereEqualTo("brand", brand)
                .orderBy("brand")
                .startAt(brand)
                .endAt(brand+"\uf8ff");

        FirestoreRecyclerOptions<Product> options = new FirestoreRecyclerOptions.Builder<Product>()
                .setQuery(searchQuery, snapshot -> {
                    product = snapshot.toObject(Product.class);
                    assert product != null;
                    product.setUuid(snapshot.getId());
                    return product;
                })
                .build();

        brandSearchAdapter = new FirestoreRecyclerAdapter<Product, ProductViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull ProductViewHolder holder, int position, @NonNull Product model) {
                holder.bindProduct(model);

                holder.addToCart.setOnClickListener(view -> {
                    Log.d(TAG, "Quantity: "+ holder.value);
                    loading.setMessage("Adding to cart ...");
                    loading.show();
                    Map<String, Object> cart = new HashMap<>();
                    cart.put("name", "Cart");

                    int amount;
                    if (model.isOffer()) {
                        double discount = (model.getOffer_value() * model.getSelling_price()) / 100;
                        double m = model.getSelling_price() - discount;
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

                    vars.verityApp.db.collection(Globals.CART)
                            .document(vars.getShoppingID())
                            .set(cart)
                            .addOnSuccessListener(aVoid -> checkExistingProduct(vars.getShoppingID(), model.getUuid(), cartProduct, holder.value));
                });


                if (!model.isSimple()) {
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
            public void onError(@NonNull FirebaseFirestoreException e) {
                super.onError(e);
                Log.e(TAG, "onError: ", e);
                vars.verityApp.crashlytics.recordException(e);
            }
        };
        brandSearchRecycler.setAdapter(searchAdapter);
        brandSearchAdapter.startListening(); //connects to firebase collection
        brandSearchAdapter.notifyDataSetChanged();
    }

    private void searchByName(String name) {
        Query searchQuery = vars.verityApp.db
                .collectionGroup(Globals.PRODUCTS)
                .orderBy("name")
                .startAt(name)
                .endAt(name+"\uf8ff");

        FirestoreRecyclerOptions<Product> options = new FirestoreRecyclerOptions.Builder<Product>()
                .setQuery(searchQuery, snapshot -> {
                    product = snapshot.toObject(Product.class);
                    assert product != null;
                    product.setUuid(snapshot.getId());
                    return product;
                })
                .build();

        searchAdapter = new FirestoreRecyclerAdapter<Product, ProductViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull ProductViewHolder holder, int position, @NonNull Product model) {
                holder.bindProduct(model);

                holder.addToCart.setOnClickListener(view -> {
                    Log.d(TAG, "Quantity: "+ holder.value);
                    loading.setMessage("Adding to cart ...");
                    loading.show();
                    Map<String, Object> cart = new HashMap<>();
                    cart.put("name", "Cart");

                    int amount;
                    if (model.isOffer()) {
                        double discount = (model.getOffer_value() * model.getSelling_price()) / 100;
                        double m = model.getSelling_price() - discount;
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

                    vars.verityApp.db.collection(Globals.CART)
                            .document(vars.getShoppingID())
                            .set(cart)
                            .addOnSuccessListener(aVoid -> checkExistingProduct(vars.getShoppingID(), model.getUuid(), cartProduct, holder.value));
                });


                if (!model.isSimple()) {
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
            public void onError(@NonNull FirebaseFirestoreException e) {
                super.onError(e);
                Log.e(TAG, "onError: ", e);
                vars.verityApp.crashlytics.recordException(e);
            }
        };
        searchRecycler.setAdapter(searchAdapter);
        searchAdapter.startListening(); //connects to firebase collection
        searchAdapter.notifyDataSetChanged();
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
    }

    private void calculatePrice(ProductViewHolder holder, Product product, Variable model) {
        if (product.isOffer()) {
            int newMrp = model.getPrice() + 2000;
            holder.productMRP.setText(AppUtils.formatCurrency(newMrp));
            double discount = (product.getOffer_value() * newMrp) / 100;
            double actual = newMrp - discount;
            int m = (int) actual;
            holder.productPrice.setText(AppUtils.formatCurrency(m));
        } else {
            holder.productPrice.setText(AppUtils.formatCurrency(model.getPrice()));
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        return super.onOptionsItemSelected(item);
    }
}