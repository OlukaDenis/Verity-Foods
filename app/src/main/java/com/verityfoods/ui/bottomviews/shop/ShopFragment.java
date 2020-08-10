package com.verityfoods.ui.bottomviews.shop;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.navigation.NavController;
import androidx.paging.PagedList;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.firestore.paging.FirestorePagingAdapter;
import com.firebase.ui.firestore.paging.FirestorePagingOptions;
import com.firebase.ui.firestore.paging.LoadingState;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.badge.BadgeDrawable;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.verityfoods.R;
import com.verityfoods.data.model.Cart;
import com.verityfoods.data.model.Category;
import com.verityfoods.data.model.Product;
import com.verityfoods.utils.Globals;
import com.verityfoods.utils.Vars;
import com.verityfoods.viewholders.ProductViewHolder;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class ShopFragment extends Fragment {
    private static final String TAG = "ShopFragment";
    private Vars vars;
    private Product product;
    private RecyclerView productRecycler;
    private FirestorePagingAdapter<Product, ProductViewHolder> adapter;
    private LinearLayoutManager layoutManager;
    private Category category;
    private ProgressDialog loading;
    int quantity;

    private NavController navController;
    BadgeDrawable badgeDrawable;
    BottomNavigationView bottomNav;

    private ShopViewModel shopViewModel;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        shopViewModel =  ViewModelProviders.of(this).get(ShopViewModel.class);
        View root = inflater.inflate(R.layout.fragment_shop, container, false);
        vars = new Vars(requireActivity());
        loading = new ProgressDialog(requireActivity());

        layoutManager = new LinearLayoutManager(requireActivity());
        productRecycler = root.findViewById(R.id.products_recycler);
        productRecycler.setLayoutManager(layoutManager);

        populateCategories();
        return root;
    }

    private void populateCategories() {
        Log.d(TAG, "populateCategories called: ");
        vars.verityApp.db
                .collection(Globals.CATEGORIES)
                .get()
                .addOnCompleteListener(task -> {
                    Log.d(TAG, "populateCategories: "+task.getResult().toString());
                    Log.d(TAG, "Category size: " +task.getResult().size());
                    if (task.isSuccessful()) {

                        if (Objects.requireNonNull(task.getResult()).size() > 0) {
                            Log.d(TAG, "Category size: " +task.getResult().size());

                            for (QueryDocumentSnapshot document : Objects.requireNonNull(task.getResult())) {
                                Category category = document.toObject(Category.class);

//                                    populateProducts(category.getUuid());
                                Log.d(TAG, "Categories: " + category.getName());
//                                vars.verityApp.db
//                                        .collection(Globals.CATEGORIES)
//                                        .document(category.getUuid())
//                                        .collection(Globals.PRODUCTS)
//                                        .get()
//                                        .addOnCompleteListener(task1 -> {
//                                            if (task.isSuccessful()) {
//                                                if (Objects.requireNonNull(task1.getResult()).size() > 0) {
//                                                    for (QueryDocumentSnapshot snapshot : Objects.requireNonNull(task1.getResult())) {
//                                                        Product product = snapshot.toObject(Product.class);
//                                                        Log.d(TAG, "All products: " + product.getName());
//                                                    }
//                                                }
//                                            }
//                                        })
//                                        .addOnFailureListener(e -> Log.e(TAG, "Error: ", e));
                            }
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    vars.verityApp.crashlytics.log("Error while fetching categories");
                    Log.e(TAG, "Error occured: ", e);
                    vars.verityApp.crashlytics.recordException(e);
                });
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
                                cartProduct.setAmount((product.getSelling_price() * qty + cartProduct.getAmount()));
                                cartProduct.setQuantity(qty + cartProduct.getQuantity());
                                vars.verityApp.db.collection(Globals.CART)
                                        .document(userId)
                                        .collection(Globals.MY_CART)
                                        .document(document.getId())
                                        .set(cartProduct);

                                Toast.makeText(requireActivity(), "Product added to Cart", Toast.LENGTH_SHORT).show();
                                loading.dismiss();
                            }
                        } else {
                            vars.verityApp.db.collection(Globals.CART)
                                    .document(userId)
                                    .collection(Globals.MY_CART)
                                    .add(cart)
                                    .addOnSuccessListener(documentReference -> {
                                        Toast.makeText(requireActivity(), "Product added to Cart", Toast.LENGTH_SHORT).show();
                                        updateCartCount();
//                                        viewModel.cartTotalCount(userId);
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

    public void updateCartCount() {
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
                    Log.e(TAG, "Error while getting cart count: ",e );
                });
    }

    private void populateProducts(String categoryID) {
        Log.d(TAG, "populateProducts called");

        Query catQuery = vars.verityApp.db
                .collection(Globals.CATEGORIES)
                .document(categoryID)
                .collection(Globals.PRODUCTS)
                .orderBy("name");

        PagedList.Config config = new PagedList.Config.Builder()
                .setEnablePlaceholders(false)
                .setPrefetchDistance(10)
                .setPageSize(20)
                .build();

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

                holder.plusButton.setOnClickListener(view -> {
                    int p = Integer.parseInt(holder.total.getText().toString());
                    holder.total.setText(String.valueOf(p += 1));
                    quantity = Integer.parseInt(holder.total.getText().toString());
                });
                holder.minusButton.setOnClickListener(view -> {
                    int m = Integer.parseInt(holder.total.getText().toString());
                    if (m > 1) {
                        holder.total.setText(String.valueOf(m -= 1));
                        quantity = Integer.parseInt(holder.total.getText().toString());
                    }
                });

                holder.addToCart.setOnClickListener(view -> {
                    Log.d(TAG, "Quantity: "+ quantity);
                    loading.setMessage("Adding to cart ...");
                    loading.show();
                    Map<String, Object> cart = new HashMap<>();
                    cart.put("name", "Cart");

                    int amount = model.getSelling_price() * quantity;
                    Cart cartProduct = new Cart(
                            category.getUuid(),
                            category.getName(),
                            model.getUuid(),
                            model.getName(),
                            model.getImage(),
                            quantity,
                            amount
                    );

                    vars.verityApp.db.collection(Globals.CART)
                            .document(vars.getShoppingID())
                            .set(cart)
                            .addOnSuccessListener(aVoid -> checkExistingProduct(vars.getShoppingID(), model.getUuid(), cartProduct, quantity));
                });
            }

            @NonNull
            @Override
            public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_products, parent, false);
                return new ProductViewHolder(view);
            }

            @Override
            protected void onError(@NonNull Exception e) {
                super.onError(e);
                Toast.makeText(requireActivity(), "Error", Toast.LENGTH_SHORT).show();
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
                        Toast.makeText(requireActivity(), "Error", Toast.LENGTH_SHORT).show();

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
}