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
import androidx.recyclerview.widget.RecyclerView;

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
import com.verityfoods.data.adapters.DealSliderAdapter;
import com.verityfoods.data.model.Category;
import com.verityfoods.data.model.Deal;
import com.verityfoods.data.model.ProductSlider;
import com.verityfoods.ui.search.SearchActivity;
import com.verityfoods.utils.Globals;
import com.verityfoods.utils.Vars;
import com.verityfoods.viewholders.CategoryViewHolder;

import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;


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

    @BindView(R.id.dealSlider)
    SliderView dealSlider;

    private BannerSliderAdapter sliderAdapter;
    private DealSliderAdapter dealAdapter;


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

        dealAdapter = new DealSliderAdapter(vars, requireActivity());
        sliderAdapter = new BannerSliderAdapter(vars, requireActivity());

        categoryRecycler = root.findViewById(R.id.shop_category_recycler);
        gridLayoutManager = new GridLayoutManager(requireActivity(), 3);
        categoryRecycler.setLayoutManager(gridLayoutManager);

        searchEdittext.setOnClickListener(view -> {
            startActivity(new Intent(requireActivity(), SearchActivity.class));
            });

        shopButton.setOnClickListener(view -> navController.navigate(R.id.navigation_shop));

        populateCategories();
        populateSliders();
        populateDeals();

        return root;
    }

    private void populateDeals() {
        dealSlider.startAutoCycle();
        dealSlider.setIndicatorAnimation(IndicatorAnimations.WORM);
        dealSlider.setSliderTransformAnimation(SliderAnimations.SIMPLETRANSFORMATION);
        dealSlider.setScrollTimeInSec(8);

        vars.verityApp.db
                .collection(Globals.DEALS)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (DocumentSnapshot snapshot : Objects.requireNonNull(task.getResult())) {
                            Deal mDeal = snapshot.toObject(Deal.class);
                            dealAdapter.addItem(mDeal);
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "populateDeals: ", e);
                });

        dealSlider.setSliderAdapter(dealAdapter);
        dealAdapter.notifyDataSetChanged();
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
    }

    @Override
    public void onPause() {
        super.onPause();
    }
}