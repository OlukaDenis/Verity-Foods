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
import android.widget.Toast;

import com.firebase.ui.firestore.paging.FirestorePagingAdapter;
import com.firebase.ui.firestore.paging.FirestorePagingOptions;
import com.firebase.ui.firestore.paging.LoadingState;
import com.google.android.material.badge.BadgeDrawable;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.squareup.picasso.Picasso;
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

public class ProductsFragment extends Fragment {
    private static final String TAG = "ProductsFragment";
    private Vars vars;
    private RecyclerView productRecycler;
    private RecyclerView subCategoryRecycler;

    private LinearLayoutManager productsLayoutManager;
    private LinearLayoutManager subCategoriesLayoutManager;

    private FirestorePagingAdapter<Product, ProductViewHolder> adapter;
    private FirestorePagingAdapter<SubCategory, SubCategoryViewHolder> subAdapter;
    private FirestorePagingAdapter<Variable, VariableViewHolder> variableAdapter;

    private SubCategory subCategory;
    private Category category;
    private Product product;
    private Variable variable;
    private int currentProductPrice;

    private ProgressDialog loading;
    private String userUid;

    private NavController navController;
    BadgeDrawable badgeDrawable;
    BottomNavigationView bottomNav;
    private PagedList.Config config;

    private int modifiedAmount;
    private Map<String, Object> cartPath;

    private ImageView categoryBanner;
    private static int index = 0;

    public ProductsFragment() {
        // Required empty public constructor
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View root =  inflater.inflate(R.layout.fragment_products, container, false);
        ButterKnife.bind(requireActivity());
        bottomNav = requireActivity().findViewById(R.id.bottom_navigation);
        badgeDrawable = bottomNav.getBadge(R.id.navigation_cart);

        navController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment);

        vars = new Vars(requireContext());
        loading = new ProgressDialog(requireActivity());
        loading.setMessage("Adding to cart ...");

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

        if (vars.isLoggedIn()) {
            userUid = vars.verityApp.mAuth.getCurrentUser().getUid();
        }

        config = new PagedList.Config.Builder()
                .setEnablePlaceholders(false)
                .setPrefetchDistance(10)
                .setPageSize(20)
                .build();

        categoryBanner = root.findViewById(R.id.category_banner);
        Picasso.get()
                .load(category.getImage())
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

    private void checkExistingProduct(String userId, String productID, Cart cart, int qty) {

        vars.verityApp.db.collection(Globals.CART + "/" + userId + "/" + Globals.MY_CART)
                .whereEqualTo("product_id", productID)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        if (Objects.requireNonNull(task.getResult()).size() > 0) {

                            for (QueryDocumentSnapshot document : Objects.requireNonNull(task.getResult())) {
                                Cart cartProduct = document.toObject(Cart.class);

                                if (product.isOffer()) {
                                    double discount = (product.getOffer_value() * product.getSelling_price()) / 100;
                                    double m = product.getSelling_price() - discount;
                                    int actual = (int) m;

                                    cartProduct.setAmount((actual * qty + cartProduct.getAmount()));
                                } else {
                                    cartProduct.setAmount((product.getSelling_price() * qty + cartProduct.getAmount()));
                                }
                                cartProduct.setQuantity(qty + cartProduct.getQuantity());

                                vars.verityApp.db.collection(Globals.CART)
                                        .document(userId)
                                        .collection(Globals.MY_CART)
                                        .document(document.getId())
                                        .set(cartProduct);

                                Toast.makeText(requireActivity(), "Product added to Cart", Toast.LENGTH_SHORT).show();
                                loading.dismiss();
                            }
                        } else {
                            vars.verityApp.db.collection(Globals.CART)
                                    .document(userId)
                                    .collection(Globals.MY_CART)
                                    .add(cart)
                                    .addOnSuccessListener(documentReference -> {
                                        Toast.makeText(requireActivity(), "Product added to Cart", Toast.LENGTH_SHORT).show();
                                        updateCartCount();
//                                        viewModel.cartTotalCount(userId);
                                        loading.dismiss();
                                    })
                                    .addOnFailureListener(e -> {
                                        vars.verityApp.crashlytics.recordException(e);
                                        Log.e(TAG, "Error while adding to cart:: ",e );
                                        loading.dismiss();
                                    });
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    vars.verityApp.crashlytics.recordException(e);
                    Log.e(TAG, "Error while adding to cart: ",e );
                });
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
        subCategoryRecycler.setAdapter(subAdapter);
        subAdapter.notifyDataSetChanged();
    }

    private void populateProducts() {
        Log.d(TAG, "populateProducts called");

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

                if (model.isSimple()) {
                    holder.addToCart.setOnClickListener(view -> {

                        loading.show();
                            int amount;
                            if (model.isOffer()) {
                                double discount = (model.getOffer_value() * model.getMrp()) / 100;
                                double m = model.getMrp() - discount;
                                int actual = (int) m;
                                amount = actual * holder.value;
                            } else {
                                amount = model.getSelling_price() * holder.value;
                            }

                            Cart cartProduct = new Cart(
                                    category.getUuid(),
                                    category.getName(),
                                    model.getUuid(),
                                    model.getName(),
                                    model.getImage(),
                                    model.getMrp(),
                                    holder.value,
                                    amount
                            );

                        addProductCart(cartProduct, holder);
                    });

                } else {
                    populateVariables(holder, model);
                }
            }

            @NonNull
            @Override
            public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_products, parent, false);
                return new ProductViewHolder(view, vars);
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

    private void addProductCart(Cart cartProduct, ProductViewHolder holder) {
        vars.verityApp.db.collection(Globals.CART)
                .document(vars.getShoppingID())
                .set(cartPath)
                .addOnSuccessListener(aVoid -> checkExistingProduct(vars.getShoppingID(), cartProduct.getProduct_id(), cartProduct, holder.value));
    }

    private void populateVariables(ProductViewHolder productViewHolder, Product productModel) {
        Query variableQuery = vars.verityApp.db
                .collection(Globals.CATEGORIES)
                .document(category.getUuid())
                .collection(Globals.SUB_CATEGORIES)
                .document(productModel.getSub_category_id())
                .collection(Globals.PRODUCTS)
                .document(productModel.getUuid())
                .collection(Globals.VARIABLE);

        FirestorePagingOptions<Variable> variableOptions = new FirestorePagingOptions.Builder<Variable>()
                .setLifecycleOwner(this)
                .setQuery(variableQuery, config, snapshot -> {
                    variable = snapshot.toObject(Variable.class);
                    assert variable != null;
                    variable.setUuid(snapshot.getId());
                    return variable;
                })
                .build();

        variableAdapter = new FirestorePagingAdapter<Variable, VariableViewHolder>(variableOptions) {
            @Override
            protected void onBindViewHolder(@NonNull VariableViewHolder holder, int position, @NonNull Variable model) {
                holder.bindVariable(model);

                holder.itemView.setOnClickListener(view -> {
                    calculatePrice(productViewHolder, productModel, model);
                    index = position;
                    changeVariableColor(holder, position);
                });
            }

            @NonNull
            @Override
            public VariableViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_variable, parent, false);
                return new VariableViewHolder(view, getContext());
            }

            @Override
            protected void onError(@NonNull Exception e) {
                super.onError(e);
                Toast.makeText(requireActivity(), "Error", Toast.LENGTH_SHORT).show();
            }
        };
        productViewHolder.variableRecycler.setAdapter(variableAdapter);
        variableAdapter.notifyDataSetChanged();

        //handle add to cart
        productViewHolder.addToCart.setOnClickListener(view -> {
            loading.show();
            int mAmount = modifiedAmount * productViewHolder.value;
            Log.d(TAG, "populateVariables: "+productViewHolder.value);
            Cart cartProduct = new Cart(
                    category.getUuid(),
                    category.getName(),
                    productModel.getUuid(),
                    productModel.getName(),
                    productModel.getImage(),
                    productModel.getMrp(),
                    productViewHolder.value,
                    mAmount
            );

            addProductCart(cartProduct, productViewHolder);
        });
    }

    private void calculatePrice(ProductViewHolder holder, Product product, Variable model) {
        if (product.isOffer()) {
            int newMrp = model.getPrice() + 2000;
            holder.productMRP.setText(AppUtils.formatCurrency(newMrp));
            double discount = (product.getOffer_value() * newMrp) / 100;
            double actual = newMrp - discount;
            int m = (int) actual;
            //update the amount
            modifiedAmount = (int) actual;
            holder.productPrice.setText(AppUtils.formatCurrency(m));
        } else {
            holder.productPrice.setText(AppUtils.formatCurrency(model.getPrice()));
            modifiedAmount = model.getPrice();
        }
    }

    private void changeVariableColor(VariableViewHolder holder, int position) {
        Log.d(TAG, "Position: "+position);
        Log.d(TAG, "Index: "+index);
        if (index == position) {
            holder.variableName.setBackground(requireActivity().getResources().getDrawable(R.drawable.variable_filled_bg));
            holder.variableName.setTextColor(requireActivity().getResources().getColor(R.color.white));
        } else {
            holder.variableName.setBackground(requireActivity().getResources().getDrawable(R.drawable.varible_bg));
            holder.variableName.setTextColor(requireActivity().getResources().getColor(R.color.black));
        }
    }
}