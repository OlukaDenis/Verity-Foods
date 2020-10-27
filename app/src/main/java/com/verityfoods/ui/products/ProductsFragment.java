package com.verityfoods.ui.products;

import android.app.ProgressDialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.paging.PagedList;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.facebook.shimmer.ShimmerFrameLayout;
import com.firebase.ui.firestore.paging.FirestorePagingAdapter;
import com.firebase.ui.firestore.paging.FirestorePagingOptions;
import com.firebase.ui.firestore.paging.LoadingState;
import com.google.firebase.firestore.Query;
import com.verityfoods.MainActivity;
import com.verityfoods.R;
import com.verityfoods.data.model.Category;
import com.verityfoods.data.model.Product;
import com.verityfoods.data.model.SubCategory;
import com.verityfoods.utils.Globals;
import com.verityfoods.utils.Vars;
import com.verityfoods.viewholders.ProductViewHolder;
import com.verityfoods.viewholders.SubCategoryViewHolder;

import java.util.HashMap;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;

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

    private NavController navController;
    private PagedList.Config config;

    private Map<String, Object> cartPath;

    private ImageView categoryBanner;

    @BindView(R.id.product_shimmer_container)
    ShimmerFrameLayout productShimmerContainer;

    public ProductsFragment() {
        // Required empty public constructor
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View root =  inflater.inflate(R.layout.fragment_products, container, false);
        ButterKnife.bind(this, root);

        navController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment);

        vars = new Vars(requireContext());

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

        config = new PagedList.Config.Builder()
                .setEnablePlaceholders(false)
                .setPrefetchDistance(10)
                .setPageSize(20)
                .build();

        categoryBanner = root.findViewById(R.id.category_banner);

        Glide.with(requireActivity())
                .load(category.getImage())
                .centerCrop()
                .error(R.drawable.ic_baseline_image_24)
                .placeholder(R.drawable.ic_baseline_image_24)
                .into(categoryBanner);

        cartPath = new HashMap<>();
        cartPath.put("name", "Cart");

        populateProducts();
        populateSubCategories();

        return root;
    }

    private ActionBar getActionBar() {
        return ((MainActivity) requireActivity()).getSupportActionBar();
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
                    subCategory.setUuid(snapshot.getId());
                    return subCategory;
                })
                .build();

        subAdapter = new FirestorePagingAdapter<SubCategory, SubCategoryViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull SubCategoryViewHolder holder, int position, @NonNull SubCategory model) {
                holder.bindSubCategory(model);

                holder.itemView.setOnClickListener(view -> {
                    Bundle bundle = new Bundle();
                    bundle.putSerializable(Globals.SUB_CATEGORY_OBJ, model);
                    navController.navigate(R.id.navigation_sub_category, bundle);
                    Globals.CATEGORY_ID = category.getUuid();
                    Globals.CATEGORY_NAME = category.getName();
                });

                productShimmerContainer.setVisibility(View.GONE);
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
        subCategoryRecycler.setAdapter(subAdapter);
        subAdapter.notifyDataSetChanged();
    }

    private void populateProducts() {

        Query categoryQuery = vars.verityApp.db
                .collectionGroup(Globals.PRODUCTS)
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