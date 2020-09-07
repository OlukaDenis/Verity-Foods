package com.verityfoods.ui.bottomviews.offers;

import androidx.lifecycle.ViewModelProviders;

import android.app.ProgressDialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.paging.PagedList;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.LinearSnapHelper;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SnapHelper;

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
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.verityfoods.R;
import com.verityfoods.data.adapters.DealsAdapter;
import com.verityfoods.data.model.Cart;
import com.verityfoods.data.model.Category;
import com.verityfoods.data.model.Deal;
import com.verityfoods.data.model.Product;
import com.verityfoods.data.model.Variable;
import com.verityfoods.utils.AppUtils;
import com.verityfoods.utils.Globals;
import com.verityfoods.utils.Vars;
import com.verityfoods.viewholders.ProductViewHolder;
import com.verityfoods.viewholders.VariableViewHolder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;

import static com.verityfoods.utils.Globals.MAX_LIST_SIZE;

public class OffersFragment extends Fragment {
    private static final String TAG = "OffersFragment";

    private Vars vars;
    private Product product;
    private RecyclerView productRecycler;
    private FirestorePagingAdapter<Product, ProductViewHolder> adapter;
    private LinearLayoutManager layoutManager;

    private PagedList.Config config;

    private Map<String, Object> cartPath;

    private Timer dealsTimer;
    private TimerTask dealsTimerTask;
    private int dealsPosition;
    private LinearLayoutManager dealsLayoutManager;
    private RecyclerView dealsRecycler;
    private List<Deal> deals;
    private DealsAdapter dealsAdapter;

    public OffersFragment() {
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_offers, container, false);

        vars = new Vars(requireActivity());

        layoutManager = new LinearLayoutManager(requireActivity());
        productRecycler = root.findViewById(R.id.products_recycler);
        productRecycler.setLayoutManager(layoutManager);

        config = new PagedList.Config.Builder()
                .setEnablePlaceholders(false)
                .setPrefetchDistance(10)
                .setPageSize(20)
                .build();

        cartPath = new HashMap<>();
        cartPath.put("name", "Cart");

        deals = new ArrayList<>();
        //Deals slider
        dealsRecycler = root.findViewById(R.id.deals_slider);
        dealsLayoutManager = new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false);
        dealsRecycler.setLayoutManager(dealsLayoutManager);
        populateDeals();//populate sliders

        if (deals != null) {
            dealsPosition = MAX_LIST_SIZE / 2;
            dealsRecycler.scrollToPosition(dealsPosition);
        }

        SnapHelper dealHelper = new LinearSnapHelper();
        dealHelper.attachToRecyclerView(dealsRecycler);
        dealsRecycler.smoothScrollBy(5, 0);

        dealsRecycler.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);

                if (newState == 1) {
                    stopAutoScrollDeals();
                } else if (newState == 0) {
                    dealsPosition = dealsLayoutManager.findFirstCompletelyVisibleItemPosition();
                    runAutoScrollDeals();
                }
            }
        });

        populateProducts();
        return root;
    }

    private void populateDeals() {
        vars.verityApp.db
                .collection(Globals.DEALS)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (DocumentSnapshot snapshot : Objects.requireNonNull(task.getResult())) {
                            Deal mDeal = snapshot.toObject(Deal.class);
                            Log.d(TAG, "populateDeals: "+ mDeal.getImage());
                            deals.add(mDeal);
                        }

                        dealsAdapter = new DealsAdapter(requireActivity(), deals);
                        dealsRecycler.setAdapter(dealsAdapter);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "populateDeals: ", e);
                });
    }

    private void stopAutoScrollDeals() {
        if (dealsTimer != null && dealsTimerTask != null) {
            dealsTimerTask.cancel();
            dealsTimer.cancel();
            dealsTimer = null;
            dealsTimerTask = null;
            dealsPosition = dealsLayoutManager.findFirstCompletelyVisibleItemPosition();
        }
    }

    private void runAutoScrollDeals() {
        if (dealsTimer == null && dealsTimerTask == null) {
            dealsTimer = new Timer();
            dealsTimerTask = new TimerTask() {
                @Override
                public void run() {
                    if (dealsPosition == MAX_LIST_SIZE) {
                        dealsPosition = MAX_LIST_SIZE / 2;
                        dealsRecycler.scrollToPosition(dealsPosition);
                        dealsRecycler.smoothScrollBy(5, 0);
                    } else {
                        dealsPosition++;
                        dealsRecycler.smoothScrollToPosition(dealsPosition);
                    }
                }
            };
            dealsTimer.schedule(dealsTimerTask, 5000, 5000);
        }
    }

    private void populateProducts() {
        Log.d(TAG, "populateProducts called");

        Query catQuery = vars.verityApp.db
                .collectionGroup(Globals.PRODUCTS)
                .whereEqualTo("offer", true);

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


    @Override
    public void onResume() {
        super.onResume();
        runAutoScrollDeals();
    }

    @Override
    public void onPause() {
        super.onPause();
        stopAutoScrollDeals();
        MAX_LIST_SIZE = 10;
    }
}