package com.verityfoods;

import android.os.Bundle;
import android.view.Menu;

import com.google.android.material.badge.BadgeDrawable;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;
import com.verityfoods.utils.Globals;

import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

public class MainActivity extends AppCompatActivity {

    private AppBarConfiguration mAppBarConfiguration;
    private NavController navController;
    BadgeDrawable badgeDrawable;
    BottomNavigationView bottomNav;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
         navController = Navigation.findNavController(this, R.id.nav_host_fragment);

        bottomNav = findViewById(R.id.bottom_navigation);
        NavigationUI.setupWithNavController(bottomNav, navController);

        NavigationView navigationView = findViewById(R.id.nav_view);
        mAppBarConfiguration = new AppBarConfiguration.Builder(
              R.id.nav_home, R.id.nav_account, R.id.nav_orders, R.id.nav_settings)
                .setDrawerLayout(drawer)
                .build();

        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);
    }

    public void getCartCount() {
        badgeDrawable = bottomNav.getBadge(R.id.navigation_cart);
        if (badgeDrawable == null) {
            bottomNav.getOrCreateBadge(R.id.navigation_cart).setNumber(Globals.CART_COUNT);
        } else {
            badgeDrawable.setNumber(Globals.CART_COUNT);
        }
//        vars.tehecaApp.myDatabse.collection(Globals.cart)
//                .document(vars.getShoppingID())
//                .collection(Globals.cartProducts)
//                .get()
//                .addOnSuccessListener(queryDocumentSnapshots -> {
//                    int count = queryDocumentSnapshots.size();
//                    cartCounter.setText(String.valueOf(count));
//                })
//                .addOnFailureListener(e -> Timber.i("An error occurred while getting cart item count%s", e.getMessage()));

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }

    @Override
    protected void onResume() {
        super.onResume();
        getCartCount();
    }
}