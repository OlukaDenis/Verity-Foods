package com.verityfoods.ui.brands;

import android.app.ProgressDialog;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.paging.PagedList;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.facebook.shimmer.ShimmerFrameLayout;
import com.google.android.material.badge.BadgeDrawable;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.verityfoods.R;
import com.verityfoods.data.adapters.BrandsAdapter;
import com.verityfoods.data.model.Product;
import com.verityfoods.utils.Globals;
import com.verityfoods.utils.Vars;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import butterknife.BindView;

public class BrandFragment extends Fragment {
    private static final String TAG = "BrandFragment";
    private Vars vars;
    private Product product;
    private RecyclerView brandRecycler;
    private GridLayoutManager layoutManager;
    private ProgressDialog loading;

    private NavController navController;
    private BadgeDrawable badgeDrawable;
    private BottomNavigationView bottomNav;

    private PagedList.Config config;
    private List<String> brands;

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
        brands = new ArrayList<>();

        navController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment);

        layoutManager = new GridLayoutManager(requireActivity(), 3);
        brandRecycler = root.findViewById(R.id.brand_recycler);
        brandRecycler.setLayoutManager(layoutManager);

        fetchBrands();
        return root;
    }

    private void fetchBrands() {
        Query catQuery = vars.verityApp.db
                .collectionGroup(Globals.PRODUCTS);

        catQuery.get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : Objects.requireNonNull(task.getResult())) {
                            Product prd = document.toObject(Product.class);
                            if (!brands.contains(prd.getBrand())) {
                                brands.add(prd.getBrand());
                            }
                        }

                        populateBrands();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "fetchBrands: ",e );
                    vars.verityApp.crashlytics.recordException(e);
                });
    }

    private void populateBrands() {
        BrandsAdapter adapter = new BrandsAdapter(brands, requireActivity());
        brandRecycler.setAdapter(adapter);
        adapter.notifyDataSetChanged();
    }
}