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

import com.facebook.shimmer.ShimmerFrameLayout;
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
import com.verityfoods.data.model.Variable;
import com.verityfoods.utils.AppUtils;
import com.verityfoods.utils.Globals;
import com.verityfoods.utils.Vars;
import com.verityfoods.viewholders.ProductViewHolder;
import com.verityfoods.viewholders.SubCategoryViewHolder;
import com.verityfoods.viewholders.VariableViewHolder;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;

public class SubCategoriesFragment extends Fragment {
    private static final String TAG = "SubCategoriesFragment";
    private PagedList.Config config;

    private Vars vars;
    private RecyclerView productRecycler;
    private FirestorePagingAdapter<Product, ProductViewHolder> adapter;
    private LinearLayoutManager productsLayoutManager;

    private SubCategory subCategory;
    private Product product;
    private String categoryID;
    private String categoryName;

    @BindView(R.id.product_shimmer_container)
    ShimmerFrameLayout productShimmerContainer;

    public SubCategoriesFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View root = inflater.inflate(R.layout.fragment_sub_categories, container, false);

        vars = new Vars(requireContext());
        ButterKnife.bind(this, root);

        Bundle bundle = getArguments();
        assert bundle != null;
        subCategory = (SubCategory) bundle.getSerializable(Globals.SUB_CATEGORY_OBJ);

        categoryID = Globals.CATEGORY_ID;
        categoryName = Globals.CATEGORY_NAME;
        getActionBar().setTitle(subCategory.getName());

        productsLayoutManager = new LinearLayoutManager(requireActivity());
        productRecycler = root.findViewById(R.id.products_recycler);
        productRecycler.setLayoutManager(productsLayoutManager);

        config = new PagedList.Config.Builder()
                .setEnablePlaceholders(false)
                .setPrefetchDistance(10)
                .setPageSize(20)
                .build();

        populateProducts();

        return root;
    }

    private ActionBar getActionBar() {
        return ((MainActivity) requireActivity()).getSupportActionBar();
    }

    private void populateProducts() {
        Log.d(TAG, "populateProducts called");

        Query categoryQuery = vars.verityApp.db
                .collection(Globals.CATEGORIES)
                .document(categoryID)
                .collection(Globals.SUB_CATEGORIES)
                .document(subCategory.getUuid())
                .collection(Globals.PRODUCTS);

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
                productShimmerContainer.setVisibility(View.GONE);
            }

            @NonNull
            @Override
            public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_products, parent, false);
                return new ProductViewHolder(view, vars, requireActivity());
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
                        productShimmerContainer.setVisibility(View.VISIBLE);
                        break;

                    case LOADED:
                        productShimmerContainer.setVisibility(View.GONE);
                        notifyDataSetChanged();
                        break;

                    case ERROR:
                        Toast.makeText(requireActivity(), "Error", Toast.LENGTH_SHORT).show();

                        productShimmerContainer.setVisibility(View.GONE);
                        break;

                    case FINISHED:
                        productShimmerContainer.setVisibility(View.GONE);
                        break;
                }
            }
        };
        productRecycler.setAdapter(adapter);
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onStart() {
        super.onStart();
        productShimmerContainer.startShimmer();
    }

    @Override
    public void onStop() {
        super.onStop();
        productShimmerContainer.stopShimmer();
    }

}