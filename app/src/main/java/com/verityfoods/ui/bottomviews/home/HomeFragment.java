package com.verityfoods.ui.bottomviews.home;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;
import androidx.paging.PagedList;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.firestore.paging.FirestorePagingAdapter;
import com.firebase.ui.firestore.paging.FirestorePagingOptions;
import com.firebase.ui.firestore.paging.LoadingState;
import com.google.firebase.firestore.Query;
import com.verityfoods.R;
import com.verityfoods.data.model.Category;
import com.verityfoods.ui.ProductsActivity;
import com.verityfoods.utils.Globals;
import com.verityfoods.utils.Vars;
import com.verityfoods.viewholders.CategoryViewHolder;

import butterknife.BindView;

public class HomeFragment extends Fragment {
    private static final String TAG = "HomeFragment";
    private Vars vars;
    private FirestorePagingAdapter<Category, CategoryViewHolder> adapter;
    private GridLayoutManager gridLayoutManager;
    private Category category;

    private RecyclerView categoryRecycler;


    private HomeViewModel homeViewModel;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        homeViewModel = ViewModelProviders.of(this).get(HomeViewModel.class);
        View root = inflater.inflate(R.layout.fragment_home, container, false);
        vars = new Vars(requireActivity());

        categoryRecycler = root.findViewById(R.id.shop_category_recycler);
        gridLayoutManager = new GridLayoutManager(requireActivity(), 3);
        categoryRecycler.setLayoutManager(gridLayoutManager);
        populateCategories();
        return root;
    }

    private void populateCategories() {
        Log.d(TAG, "populateCategories called: ");
        Query catQuery = vars.verityApp.db
                .collection(Globals.CATEGORIES)
                .orderBy("name");

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
                    return category;
                })
                .build();

        adapter = new FirestorePagingAdapter<Category, CategoryViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull CategoryViewHolder holder, int position, @NonNull Category model) {
                holder.bindCategory(model);

                holder.itemView.setOnClickListener(v -> {
                    Intent productIntent = new Intent(requireActivity(), ProductsActivity.class);
//                    productIntent.putExtra(Globals.ACTIVE_CATEGORY_ID, model.getShop_id());

                    startActivity(productIntent);

                });
            }

            @NonNull
            @Override
            public CategoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_shop_category, parent, false);
                return new CategoryViewHolder(view);
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
}