package com.verityfoods.ui.bottomviews.basket;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.paging.PagedList;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.firestore.paging.FirestorePagingAdapter;
import com.firebase.ui.firestore.paging.FirestorePagingOptions;
import com.firebase.ui.firestore.paging.LoadingState;
import com.google.android.material.badge.BadgeDrawable;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.verityfoods.R;
import com.verityfoods.data.model.Cart;
import com.verityfoods.data.model.Category;
import com.verityfoods.data.model.Product;
import com.verityfoods.ui.auth.AuthChooser;
import com.verityfoods.ui.auth.SignupActivity;
import com.verityfoods.ui.checkout.CheckoutActivity;
import com.verityfoods.utils.AppUtils;
import com.verityfoods.utils.Globals;
import com.verityfoods.utils.Vars;
import com.verityfoods.viewholders.CartViewHolder;
import com.verityfoods.viewholders.ProductViewHolder;

import java.util.Objects;

public class BasketFragment extends Fragment {
    private static final String TAG = "BasketFragment";
    private Vars vars;
    private Cart cart;
    private int total;
    private RecyclerView cartRecycler;
    private FirestorePagingAdapter<Cart, CartViewHolder> adapter;
    private LinearLayoutManager layoutManager;
    private Category category;

    private LinearLayout checkoutLayout;
    private ProgressBar totalLoading;

    private LinearLayout emptyCart;
    private ProgressDialog loading;
    private TextView totalCartSum;
    private TextView totalSavings;
    private Button checkoutBtn;

    private NavController navController;
    BadgeDrawable badgeDrawable;
    BottomNavigationView bottomNav;
    private String userUid;
    private int totalNumberOfCart;


    public BasketFragment() {
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_basket, container, false);


        bottomNav = requireActivity().findViewById(R.id.bottom_navigation);
        badgeDrawable = bottomNav.getBadge(R.id.navigation_cart);

        emptyCart = root.findViewById(R.id.empty_cart_layout);
        checkoutLayout = root.findViewById(R.id.checkout_layout);
        checkoutBtn = root.findViewById(R.id.btn_checkout);
        totalLoading = root.findViewById(R.id.total_loading);
        loading = new ProgressDialog(requireActivity());
        loading.setMessage("Please wait ...");
        totalCartSum = root.findViewById(R.id.txt_total);
        totalSavings = root.findViewById(R.id.txt_saved);

        vars = new Vars(requireContext());

        if (vars.isLoggedIn()) {
            userUid = vars.verityApp.mAuth.getCurrentUser().getUid();
        }

        layoutManager = new LinearLayoutManager(requireActivity());
        cartRecycler = root.findViewById(R.id.cart_recycler);
        cartRecycler.setLayoutManager(layoutManager);

        checkoutBtn.setOnClickListener(view -> proceedToCheckout());

        getCartItemsCount();
        getTotalSum();
        return root;
    }

    private void proceedToCheckout() {
        if (vars.isLoggedIn()) {
            Intent checkoutIntent = new Intent(requireActivity(), CheckoutActivity.class);
            checkoutIntent.putExtra(Globals.ORDER_TOTAL, total);
            startActivity(checkoutIntent);
        }else {
            startActivity(new Intent(requireActivity(), SignupActivity.class));
            Toast.makeText(requireActivity(), "You need to login to continue", Toast.LENGTH_SHORT).show();

        }
    }

    public void getCartItemsCount() {
        vars.verityApp.db.collection(Globals.CART)
                .document(vars.getShoppingID())
                .collection(Globals.MY_CART)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    int count = queryDocumentSnapshots.size();
                    if (count > 0) {
                        populateCart();
                    } else {
                        showEmptyLayout();
                    }
                })
                .addOnFailureListener(e -> {
                    vars.verityApp.crashlytics.log("An error occurred while getting cart items count" + e.getMessage());
                });
    }

    private void showEmptyLayout() {
        if(totalNumberOfCart == 0) {
            cartRecycler.setVisibility(View.GONE);
            emptyCart.setVisibility(View.VISIBLE);
            checkoutLayout.setVisibility(View.GONE);
//            mShimmerViewContainer.setVisibility(View.GONE);

        }
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


    public void updateCartQuantity(Cart model, int newValue ) {
        Log.d(TAG, "newvalue: "+newValue);
            totalLoading.setVisibility(View.VISIBLE);
            totalCartSum.setVisibility(View.GONE);
            vars.verityApp.db.collection(Globals.CART + "/" + vars.getShoppingID() + "/" + Globals.MY_CART)
                    .whereEqualTo("product_id", model.getProduct_id())
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            if (Objects.requireNonNull(task.getResult()).size() > 0) {

                                for (QueryDocumentSnapshot document : Objects.requireNonNull(task.getResult())) {
                                    Cart mCart = document.toObject(Cart.class);
                                    int price = mCart.getAmount() / mCart.getQuantity();
                                    mCart.setAmount((price * newValue));
                                    mCart.setQuantity(newValue);
                                    vars.verityApp.db.collection(Globals.CART)
                                            .document(vars.getShoppingID())
                                            .collection(Globals.MY_CART)
                                            .document(document.getId())
                                            .set(mCart);
                                    getTotalSum();
                                    populateCart();
                                }
                            }
                        }
                    })
                    .addOnFailureListener(e -> {
                        vars.verityApp.crashlytics.log("An error occurred while updating cart" + e.getMessage());
                    });

    }


    public void populateCart() {
//        mShimmerViewContainer.setVisibility(View.VISIBLE);
        Query cartQuery = vars.verityApp.db.collection(Globals.CART)
                .document(vars.getShoppingID())
                .collection(Globals.MY_CART);

        PagedList.Config config = new PagedList.Config.Builder()
                .setEnablePlaceholders(false)
                .setPrefetchDistance(10)
                .setPageSize(20)
                .build();

        FirestorePagingOptions<Cart> options = new FirestorePagingOptions.Builder<Cart>()
                .setLifecycleOwner(this)
                .setQuery(cartQuery, config, snapshot -> {
                    cart = snapshot.toObject(Cart.class);
                    assert cart != null;
                    return cart;
                })
                .build();

        adapter = new FirestorePagingAdapter<Cart, CartViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull CartViewHolder holder, int position, @NonNull Cart model) {

                holder.bindCart(model);

                String val = holder.total.getText().toString();
                holder.plusButton.setOnClickListener(view -> {
                    int p = Integer.parseInt(val);
                    holder.total.setText(String.valueOf(p += 1));
                    updateCartQuantity(model, Integer.parseInt(holder.total.getText().toString()));
                });

                holder.minusButton.setOnClickListener(v -> {
                    int m = Integer.parseInt(val);
                    if (m > 1) {
                        holder.total.setText(String.valueOf(m -= 1));
                        updateCartQuantity(model, Integer.parseInt(holder.total.getText().toString()));
                    }
                });


                //Deleting cart
                holder.removeCart.setOnClickListener(v -> {
                    loading.show();
                    totalLoading.setVisibility(View.VISIBLE);
                    totalCartSum.setVisibility(View.GONE);
                    vars.verityApp.db.collection(Globals.CART + "/" + vars.getShoppingID() + "/" + Globals.MY_CART)
                            .whereEqualTo("product_id", cart.getProduct_id())
                            .get()
                            .addOnCompleteListener(task -> {
                                if (task.isSuccessful()) {
                                    if (Objects.requireNonNull(task.getResult()).size() > 0) {

                                        for (QueryDocumentSnapshot document : Objects.requireNonNull(task.getResult())) {
                                            vars.verityApp.db.collection(Globals.CART)
                                                    .document(vars.getShoppingID())
                                                    .collection(Globals.MY_CART)
                                                    .document(document.getId())
                                                    .delete()
                                                    .addOnSuccessListener(aVoid -> {
                                                        updateCartCount();
                                                        getTotalSum();
                                                        getCartItemsCount();
                                                        Toast.makeText(requireActivity(), "Product successfully removed from Cart", Toast.LENGTH_SHORT).show();
                                                        loading.dismiss();
                                                    })
                                                    .addOnFailureListener(e -> {
                                                        vars.verityApp.crashlytics.log("An error occurred while deleting cart" + e.getMessage());
                                                        loading.dismiss();
                                                    });
                                        }
                                    }
                                }
                            });
                });



//                holder..setOnClickListener(v -> {
//                    Intent productIntent = new Intent(getApplicationContext(), ProductDetailActivity.class);
//                    productIntent.putExtra(Globals.ACTIVE_PRODUCT_ID, model.getProduct_id());
//                    productIntent.putExtra(Globals.ACTIVE_SHOP_ID, model.getShop_id());
//
//                    startActivity(productIntent);
//
//                });

//                mShimmerViewContainer.setVisibility(View.GONE);
            }

            @NonNull
            @Override
            public CartViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_cart, parent, false);
                return new CartViewHolder(view);
            }

            @Override
            protected void onError(@NonNull Exception e) {
                super.onError(e);
                vars.verityApp.crashlytics.log("An error occurred while getting cart items" + e.getMessage());
                retry();
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
                        Toast.makeText(requireActivity(), "Error while fetching cart", Toast.LENGTH_SHORT).show();
//                        mShimmerViewContainer.setVisibility(View.GONE);
                        break;

                    case FINISHED:
//                        mShimmerViewContainer.setVisibility(View.GONE);
                        break;
                }
            }

        };

        cartRecycler.setAdapter(adapter);
        adapter.notifyDataSetChanged();
    }

    public void getTotalSum() {
        vars.verityApp.db.collection(Globals.CART)
                .document(vars.getShoppingID())
                .collection(Globals.MY_CART)
                .get()
                .addOnCompleteListener(task -> {

                    if (task.isSuccessful()) {
                        int sum = 0;
                        for (QueryDocumentSnapshot document : Objects.requireNonNull(task.getResult())) {
                            Cart cartProduct = document.toObject(Cart.class);
                            sum += cartProduct.getAmount();
                        }
                        displaySum(sum);
                    }
                })
                .addOnFailureListener(e -> {
                    vars.verityApp.crashlytics.recordException(e);
                    vars.verityApp.crashlytics.log("An error occurred while getting cart items sum%s");
                });

    }

    private void displaySum(int sum) {
        total = sum;
        String mTotal = "Total: " + AppUtils.formatCurrency(sum);
        totalLoading.setVisibility(View.GONE);
        totalCartSum.setVisibility(View.VISIBLE);
        totalCartSum.setText(mTotal);
    }

}