package com.verityfoods.ui.search;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.database.MatrixCursor;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.ImageView;

import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.verityfoods.R;
import com.verityfoods.data.adapters.SearchAdapter;
import com.verityfoods.data.adapters.SuggestionsAdapter;
import com.verityfoods.data.model.Product;
import com.verityfoods.utils.Globals;
import com.verityfoods.utils.Vars;

import org.apache.commons.text.WordUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class SearchActivity extends AppCompatActivity {
    private static final String TAG = "SearchActivity";
    private Vars vars;
    private Product product;
    private List<Product> productList;
    private SearchAdapter adapter;
    private LinearLayoutManager layoutManager;
    private RecyclerView searchRecycler;

    private ArrayList<Product> mSearches;
    private MatrixCursor cursor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        vars = new Vars(this);

//        productList = new ArrayList<>();
        mSearches = new ArrayList<>();

        layoutManager = new LinearLayoutManager(this);
        searchRecycler = findViewById(R.id.products_recycler);
        searchRecycler.setLayoutManager(layoutManager);

        Toolbar toolbar = findViewById(R.id.search_toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitle("");
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.search_menu, menu);
        MenuItem searchItem = menu.findItem(R.id.action_searchview);
        SearchView searchView = (SearchView) searchItem.getActionView();
        ImageView searchIcon = searchView.findViewById(androidx.appcompat.R.id.search_mag_icon);
        searchIcon.setImageDrawable(null);
        searchView.setQueryHint("Search");
        searchView.setIconifiedByDefault(false);
        searchItem.expandActionView();

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                searchProducts(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
//                productSuggestion(newText, searchView);
//                searchProducts(newText);
                return false;
            }
        });

        return true;
    }

    private void productSuggestion(String newText, SearchView searchView) {
        Log.d(TAG, "productSuggestion called: ");
        vars.verityApp.db
                .collectionGroup(Globals.PRODUCTS)
                .whereArrayContains("name", newText)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : Objects.requireNonNull(task.getResult())) {
                            product = document.toObject(Product.class);
                            mSearches.clear();
                            mSearches.add(product);

                            Log.d(TAG, "productSuggestion: "+product.getName());
                            String a[] = new String[mSearches.size()];
                            for (int i = 0; i < a.length; i++) {
                                a[i] = mSearches.get(i).getName();
                            }

                            ArrayAdapter<String> adapter = new ArrayAdapter<String>(SearchActivity.this, R.layout.layout_search_list, a);
                            String[] columnNames = {"_id", "text"};
                            cursor = new MatrixCursor(columnNames);
                            String[] temp = new String[2];

                            int id = 0;
                            for (String item : a) {
                                temp[0] = Integer.toString(id++);
                                temp[1] = item;
                                cursor.addRow(temp);
                            }

                            SuggestionsAdapter searchAdapter=new SuggestionsAdapter(SearchActivity.this, cursor, true, searchView, mSearches);
                            searchView.setSuggestionsAdapter(searchAdapter);
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error searching products: ",e );
                    vars.verityApp.crashlytics.recordException(e);
                });

    }

    private void searchProducts(String text) {
//        String query = WordUtils.capitalize(text);

        if (text.length() > 0) {
            text = text.substring(0, 1).toUpperCase() + text.substring(1).toLowerCase();
            productList = new ArrayList<>();

            vars.verityApp.db
                    .collectionGroup(Globals.PRODUCTS)
                    .whereGreaterThanOrEqualTo("name", text)
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : Objects.requireNonNull(task.getResult())) {
                                product = document.toObject(Product.class);
                                Log.d(TAG, "Found product: " + product.getName());
                                productList.add(product);
                            }
                            adapter = new SearchAdapter(this, productList);
                            searchRecycler.setAdapter(adapter);
                        }
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Error searching products: ", e);
                        vars.verityApp.crashlytics.recordException(e);
                    });
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        return super.onOptionsItemSelected(item);
    }
}