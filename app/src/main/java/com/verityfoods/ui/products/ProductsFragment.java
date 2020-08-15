package com.verityfoods.ui.products;

import android.app.ProgressDialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.paging.PagedList;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
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
import com.verityfoods.MainActivity;
import com.verityfoods.R;
import com.verityfoods.data.model.Cart;
import com.verityfoods.data.model.Category;
import com.verityfoods.data.model.Product;
import com.verityfoods.data.model.SubCategory;
import com.verityfoods.utils.Globals;
import com.verityfoods.utils.Vars;
import com.verityfoods.viewholders.ProductViewHolder;
import com.verityfoods.viewholders.SubCategoryViewHolder;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class ProductsFragment extends Fragment {
    private static final String TAG = "ProductsFragment";
    private Vars vars;
    private RecyclerView productRecycler;
    private RecyclerView subCategoryRecycler;

    private LinearLayoutManager productsLayoutManager;
    private LinearLayoutManager subCategoriesLayoutManager;

    private FirestorePagingAdapter<Product, ProductViewHolder> adapter;
    private FirestorePagingAdapter<SubCategory, SubCategoryViewHolder> subAdapter;

    private SubCategory subCategory;
    private Category category;
    private Product product;

    private ProgressDialog loading;
    int quantity;
    private String userUid;

    private NavController navController;
    BadgeDrawable badgeDrawable;
    BottomNavigationView bottomNav;
    private PagedList.Config config;

    public ProductsFragment() {
        // Required empty public constructor
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View root =  inflater.inflate(R.layout.fragment_products, container, false);

        bottomNav = requireActivity().findViewById(R.id.bottom_navigation);
        badgeDrawable = bottomNav.getBadge(R.id.navigation_cart);

        vars = new Vars(requireContext());
        loading = new ProgressDialog(requireActivity());

        Bundle bundle = getArguments();
        assert bundle != null;
        category = (Category) bundle.getSerializable(Globals.CATEGORY_OBJ);
       getActionBar().setTitle(category.getName());

        productsLayoutManager = new LinearLayoutManager(requireActivity());
        productRecycler = root.findViewById(R.id.products_recycler);
        productRecycler.setLayoutManager(productsLayoutManager);

        subCategoriesLayoutManager = new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false);
        subCategoryRecycler = root.findViewById(R.id.recycler_sub_categories);
        subCategoryRecycler.setLayoutManager(subCategoriesLayoutManager);

        if (vars.isLoggedIn()) {
            userUid = vars.verityApp.mAuth.getCurrentUser().getUid();
        }

        config = new PagedList.Config.Builder()
                .setEnablePlaceholders(false)
                .setPrefetchDistance(10)
                .setPageSize(20)
                .build();

        populateProducts();
        populateSubCategories();

        return root;
    }

    private ActionBar getActionBar() {
        return ((MainActivity) requireActivity()).getSupportActionBar();
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

    private void populateSubCategories() {

        Query subQuery = vars.verityApp.db
                .collection(Globals.CATEGORIES)
                .document(category.getUuid())
                .collection(Globals.SUB_CATEGORIES)
                .orderBy("name");

        FirestorePagingOptions<SubCategory> options = new FirestorePagingOptions.Builder<SubCategory>()
                .setLifecycleOwner(this)
                .setQuery(subQuery, config, snapshot -> {
                    subCategory = snapshot.toObject(SubCategory.class);
                    assert subCategory != null;
                    return subCategory;
                })
                .build();

        subAdapter = new FirestorePagingAdapter<SubCategory, SubCategoryViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull SubCategoryViewHolder holder, int position, @NonNull SubCategory model) {
                holder.bindSubCategory(model);
            }

            @NonNull
            @Override
            public SubCategoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_sub_categories, parent, false);
                return new SubCategoryViewHolder(view);
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
        subCategoryRecycler.setAdapter(subAdapter);
        subAdapter.notifyDataSetChanged();
    }

    private void populateProducts() {
        Log.d(TAG, "populateProducts called");

        Query categoryQuery = vars.verityApp.db
                .collection(Globals.PRODUCTS)
                .whereEqualTo("category_id", category.getUuid())
                .orderBy("name");

        FirestorePagingOptions<Product> options = new FirestorePagingOptions.Builder<Product>()
                .setLifecycleOwner(this)
                .setQuery(categoryQuery, config, snapshot -> {
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

                String val = holder.total.getText().toString();
                quantity = Integer.parseInt(val);

                holder.plusButton.setOnClickListener(view -> {
                    int p = Integer.parseInt(val);
                    holder.total.setText(String.valueOf(p += 1));
                    quantity = Integer.parseInt(holder.total.getText().toString());
                });

                holder.minusButton.setOnClickListener(view -> {
                    int m = Integer.parseInt(val);
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