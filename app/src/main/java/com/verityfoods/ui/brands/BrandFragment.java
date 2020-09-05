package com.verityfoods.ui.brands;

import android.app.ProgressDialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.paging.PagedList;
import androidx.recyclerview.widget.GridLayoutManager;
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
import com.verityfoods.R;
import com.verityfoods.data.model.Cart;
import com.verityfoods.data.model.Category;
import com.verityfoods.data.model.Product;
import com.verityfoods.ui.bottomviews.shop.ShopViewModel;
import com.verityfoods.utils.Globals;
import com.verityfoods.utils.Vars;
import com.verityfoods.viewholders.BrandViewHolder;
import com.verityfoods.viewholders.ProductViewHolder;

import java.util.HashMap;
import java.util.Map;

public class BrandFragment extends Fragment {
    private static final String TAG = "BrandFragment";
    private Vars vars;
    private Product product;
    private RecyclerView brandRecycler;
    private FirestorePagingAdapter<Product, BrandViewHolder> adapter;
    private GridLayoutManager layoutManager;
    private ProgressDialog loading;

    private NavController navController;
    private BadgeDrawable badgeDrawable;
    private BottomNavigationView bottomNav;

    private PagedList.Config config;

    public BrandFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View root = inflater.inflate(R.layout.fragment_brand, container, false);

        bottomNav = requireActivity().findViewById(R.id.bottom_navigation);
        badgeDrawable = bottomNav.getBadge(R.id.navigation_cart);

        vars = new Vars(requireActivity());
        loading = new ProgressDialog(requireActivity());

        navController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment);

        layoutManager = new GridLayoutManager(requireActivity(), 3);
        brandRecycler = root.findViewById(R.id.brand_recycler);
        brandRecycler.setLayoutManager(layoutManager);

        config = new PagedList.Config.Builder()
                .setEnablePlaceholders(false)
                .setPrefetchDistance(10)
                .setPageSize(20)
                .build();

        populateProducts();

        return root;
    }

    private void populateProducts() {
        Query catQuery = vars.verityApp.db
                .collectionGroup(Globals.PRODUCTS);

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

        adapter = new FirestorePagingAdapter<Product, BrandViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull BrandViewHolder holder, int position, @NonNull Product model) {
                holder.bindBrand(model);


                holder.itemView.setOnClickListener( v -> {
                    Bundle bundle = new Bundle();
                    bundle.putString(Globals.SELECTED_BRAND_OBJ, model.getBrand());
                    navController.navigate(R.id.navigation_brand_products, bundle);
                });
            }

            @NonNull
            @Override
            public BrandViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_brand, parent, false);
                return new BrandViewHolder(view, getContext());
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
        brandRecycler.setAdapter(adapter);
        adapter.notifyDataSetChanged();
    }
}