package com.verityfoods.ui.bottomviews.home;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.paging.PagedList;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.LinearSnapHelper;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SnapHelper;

import com.firebase.ui.firestore.paging.FirestorePagingAdapter;
import com.firebase.ui.firestore.paging.FirestorePagingOptions;
import com.firebase.ui.firestore.paging.LoadingState;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.Query;
import com.smarteist.autoimageslider.IndicatorAnimations;
import com.smarteist.autoimageslider.SliderAnimations;
import com.smarteist.autoimageslider.SliderView;
import com.verityfoods.R;
import com.verityfoods.data.adapters.BannerSliderAdapter;
import com.verityfoods.data.adapters.DealsAdapter;
import com.verityfoods.data.model.Category;
import com.verityfoods.data.model.Deal;
import com.verityfoods.data.model.ProductSlider;
import com.verityfoods.ui.search.SearchActivity;
import com.verityfoods.utils.Globals;
import com.verityfoods.utils.Vars;
import com.verityfoods.viewholders.CategoryViewHolder;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.verityfoods.utils.Globals.MAX_LIST_SIZE;

public class HomeFragment extends Fragment {
    private static final String TAG = "HomeFragment";
    private Vars vars;
    private FirestorePagingAdapter<Category, CategoryViewHolder> adapter;
    private GridLayoutManager gridLayoutManager;
    private Category category;

    private NavController navController;
    private RecyclerView categoryRecycler;
    private TextView searchEdittext;
    private LinearLayout searchLayout;
    private MaterialButton shopButton;

    @BindView(R.id.bannerSlider)
    SliderView bannerSlider;

    private BannerSliderAdapter sliderAdapter;

    private Timer bannerTimer;
    private TimerTask bannerTimerTask;
    private int bannerPosition;

    private Timer dealsTimer;
    private TimerTask dealsTimerTask;
    private int dealsPosition;
    private LinearLayoutManager dealsLayoutManager;
    private RecyclerView dealsRecycler;

    private List<Deal> deals;
    private DealsAdapter dealsAdapter;
    private HomeViewModel homeViewModel;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        homeViewModel = ViewModelProviders.of(this).get(HomeViewModel.class);
        View root = inflater.inflate(R.layout.fragment_home, container, false);
        vars = new Vars(requireActivity());
        ButterKnife.bind(this, root);

        navController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment);

        searchEdittext = root.findViewById(R.id.search_here);
        searchLayout = root.findViewById(R.id.search_linearLayout);
        shopButton = root.findViewById(R.id.shop_now_btn);

        deals = new ArrayList<>();
        sliderAdapter = new BannerSliderAdapter(vars, requireActivity());

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

        categoryRecycler = root.findViewById(R.id.shop_category_recycler);
        gridLayoutManager = new GridLayoutManager(requireActivity(), 3);
        categoryRecycler.setLayoutManager(gridLayoutManager);

        searchEdittext.setOnClickListener(view -> {
            startActivity(new Intent(requireActivity(), SearchActivity.class));
            });

        shopButton.setOnClickListener(view -> navController.navigate(R.id.navigation_shop));

        populateCategories();
        populateSliders();

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

    private void populateSliders() {
        bannerSlider.startAutoCycle();
        bannerSlider.setIndicatorAnimation(IndicatorAnimations.WORM);
        bannerSlider.setSliderTransformAnimation(SliderAnimations.SIMPLETRANSFORMATION);
        bannerSlider.setScrollTimeInSec(4);

        Log.d(TAG, "populateSliders called: ");
        vars.verityApp.db
                .collection(Globals.SLIDERS)
                .orderBy("brand")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (DocumentSnapshot snapshot : Objects.requireNonNull(task.getResult())) {
                           ProductSlider productSlider = snapshot.toObject(ProductSlider.class);
                            sliderAdapter.addItem(productSlider);
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "populateSliders: ", e);
                });
        bannerSlider.setSliderAdapter(sliderAdapter);
        sliderAdapter.notifyDataSetChanged();
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

    private void populateCategories() {
        Query catQuery = vars.verityApp.db
                .collection(Globals.CATEGORIES)
                .orderBy("name")
                .limit(12);

        PagedList.Config config = new PagedList.Config.Builder()
                .setEnablePlaceholders(false)
                .setPrefetchDistance(10)
                .setPageSize(20)
                .build();

        FirestorePagingOptions<Category> options = new FirestorePagingOptions.Builder<Category>()
                .setLifecycleOwner(this)
                .setQuery(catQuery, config, snapshot -> {
                    category = snapshot.toObject(Category.class);
                    assert category != null;
                    category.setUuid(snapshot.getId());
                    return category;
                })
                .build();

        adapter = new FirestorePagingAdapter<Category, CategoryViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull CategoryViewHolder holder, int position, @NonNull Category model) {
                holder.bindCategory(model);

                holder.itemView.setOnClickListener( v -> {

                    Bundle bundle = new Bundle();
                    bundle.putSerializable(Globals.CATEGORY_OBJ, model);
                    navController.navigate(R.id.navigation_products, bundle);
                });
            }

            @NonNull
            @Override
            public CategoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_shop_category, parent, false);
                return new CategoryViewHolder(view, getContext());
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
        categoryRecycler.setAdapter(adapter);
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