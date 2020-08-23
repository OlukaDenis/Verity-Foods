package com.verityfoods.ui.orders;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.paging.PagedList;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.facebook.shimmer.ShimmerFrameLayout;
import com.firebase.ui.firestore.paging.FirestorePagingAdapter;
import com.firebase.ui.firestore.paging.FirestorePagingOptions;
import com.firebase.ui.firestore.paging.LoadingState;
import com.google.firebase.firestore.Query;
import com.verityfoods.R;
import com.verityfoods.data.model.Order;
import com.verityfoods.ui.auth.SignupActivity;
import com.verityfoods.utils.Globals;
import com.verityfoods.utils.Vars;
import com.verityfoods.viewholders.OrderViewHolder;

public class OrdersFragment extends Fragment {
    private static final String TAG = "OrdersFragment";

    private OrdersViewModel ordersViewModel;

    private RecyclerView orderRecycler;
    private Vars vars;
    private Order order;
    private FirestorePagingAdapter<Order, OrderViewHolder> adapter;
    private ShimmerFrameLayout mShimmerViewContainer;
    private LinearLayout emptyOrders;
    private String userUid;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        ordersViewModel =
                ViewModelProviders.of(this).get(OrdersViewModel.class);
        View root = inflater.inflate(R.layout.fragment_orders, container, false);

        vars = new Vars(requireActivity());

        orderRecycler = root.findViewById(R.id.orders_recyclerview);
        mShimmerViewContainer = root.findViewById(R.id.order_shimmer_container);
        emptyOrders = root.findViewById(R.id.empty_order_layout);

        LinearLayoutManager layoutManager = new LinearLayoutManager(requireActivity());
        orderRecycler.setLayoutManager(layoutManager);

        if (!vars.isLoggedIn()) {
            startActivity(new Intent(requireActivity(), SignupActivity.class));
            Toast.makeText(requireActivity(), "You need to login to continue", Toast.LENGTH_SHORT).show();
        } else {
            userUid = vars.verityApp.mAuth.getCurrentUser().getUid();
            getSavedItemsCount();
        }

        return root;
    }

    private void showEmptyLayout() {
        orderRecycler.setVisibility(View.GONE);
        emptyOrders.setVisibility(View.VISIBLE);
        mShimmerViewContainer.setVisibility(View.GONE);
    }

    public void getSavedItemsCount() {
        vars.verityApp.db.collection(Globals.ORDERS)
                .document(userUid)
                .collection(Globals.MY_ORDERS)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    int count = queryDocumentSnapshots.size();
                    if (count > 0) {
                        populateOrders();
                    } else {
                        showEmptyLayout();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "An error occurred while getting saved items count%s", e);
                });
    }

    private void populateOrders() {
        mShimmerViewContainer.setVisibility(View.VISIBLE);
        Query orderQuery = vars.verityApp.db.collection(Globals.ORDERS)
                .document(userUid)
                .collection(Globals.MY_ORDERS)
                .orderBy("dateAdded");

        PagedList.Config config = new PagedList.Config.Builder()
                .setEnablePlaceholders(false)
                .setPrefetchDistance(10)
                .setPageSize(20)
                .build();

        FirestorePagingOptions<Order> options = new FirestorePagingOptions.Builder<Order>()
                .setLifecycleOwner(this)
                .setQuery(orderQuery, config, snapshot -> {
                    order = snapshot.toObject(Order.class);

                    assert order != null;
                    order.setUuid(snapshot.getId());
                    return order;
                })
                .build();

        adapter = new FirestorePagingAdapter<Order, OrderViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull OrderViewHolder holder, int position, @NonNull Order model) {
                holder.bindOrderTo(model);
                mShimmerViewContainer.setVisibility(View.GONE);
            }

            @NonNull
            @Override
            public OrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_order, parent, false);
                return new OrderViewHolder(view);
            }

            @Override
            protected void onError(@NonNull Exception e) {
                super.onError(e);
                Log.e(TAG, "An error occurred while getting order items", e);
                retry();
            }

            @Override
            protected void onLoadingStateChanged(@NonNull LoadingState state) {
                switch (state) {
                    case LOADING_INITIAL:

                        break;

                    case LOADING_MORE:
                        mShimmerViewContainer.setVisibility(View.VISIBLE);
                        break;

                    case LOADED:
                        mShimmerViewContainer.setVisibility(View.GONE);
                        notifyDataSetChanged();
                        break;

                    case ERROR:
                        Toast.makeText(requireActivity(), "Error while fetching orders", Toast.LENGTH_SHORT).show();

                        mShimmerViewContainer.setVisibility(View.GONE);
                        break;

                    case FINISHED:
                        mShimmerViewContainer.setVisibility(View.GONE);
                        break;
                }
            }

        };

        orderRecycler.setAdapter(adapter);
        adapter.notifyDataSetChanged();

    }

    @Override
    public void onStart() {
        super.onStart();
        mShimmerViewContainer.startShimmer();
        if (!vars.isLoggedIn()) {
            startActivity(new Intent(requireActivity(), SignupActivity.class));
            Toast.makeText(requireActivity(), "You need to login to continue", Toast.LENGTH_SHORT).show();
        }
    }


    @Override
    public void onStop() {
        super.onStop();
        mShimmerViewContainer.stopShimmer();
    }
}