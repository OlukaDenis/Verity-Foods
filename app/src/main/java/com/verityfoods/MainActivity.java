package com.verityfoods;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.badge.BadgeDrawable;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.navigation.NavigationView;
import com.squareup.picasso.Picasso;
import com.verityfoods.data.model.User;
import com.verityfoods.ui.auth.SignupActivity;
import com.verityfoods.ui.search.SearchActivity;
import com.verityfoods.utils.Globals;
import com.verityfoods.utils.Vars;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.navigation.NavController;
import androidx.navigation.NavDestination;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

public class MainActivity extends AppCompatActivity implements
        NavigationView.OnNavigationItemSelectedListener,
        NavController.OnDestinationChangedListener {
    private static final String TAG = "MainActivity";

    private AppBarConfiguration mAppBarConfiguration;
    private NavController navController;
    private DrawerLayout drawer;
    private BadgeDrawable badgeDrawable;
    private BottomNavigationView bottomNav;
    private Vars vars;
    private User user;
    private NavigationView navigationView;
    private String userUid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        
        vars = new Vars(this);

        drawer = findViewById(R.id.drawer_layout);
         navController = Navigation.findNavController(this, R.id.nav_host_fragment);

        bottomNav = findViewById(R.id.bottom_navigation);
        NavigationUI.setupWithNavController(bottomNav, navController);

        navigationView = findViewById(R.id.nav_view);
        mAppBarConfiguration = new AppBarConfiguration.Builder(
              R.id.nav_home, R.id.nav_account, R.id.nav_orders, R.id.nav_notifications,
                R.id.nav_faq, R.id.nav_terms, R.id.nav_about, R.id.nav_privacy)
                .setDrawerLayout(drawer)
                .build();

        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);

        //share app
        MenuItem shareItem = navigationView.getMenu().findItem(R.id.nav_share);
        shareItem.setOnMenuItemClickListener(item -> {
            drawer.closeDrawers();
            shareApp();
            return true;
        });

        MenuItem logoutItem = navigationView.getMenu().findItem(R.id.nav_logout);
        logoutItem.setOnMenuItemClickListener(menuItem -> {
            drawer.closeDrawers();
            logoutUser();
            return true;
        });

        if (vars.isLoggedIn()) {
            userUid = vars.verityApp.mAuth.getCurrentUser().getUid();
        }

        getCurrentUserDetails();
    }

    private void logoutUser() {
        if (vars.isLoggedIn()) {
            new MaterialAlertDialogBuilder(this)
                    .setTitle(this.getString(R.string.logout))
                    .setMessage(R.string.log_out_message)
                    .setPositiveButton(R.string.logout, (dialog, which) -> {
                        vars.verityApp.mAuth.signOut();
                        vars.verityApp.mAuth.signInAnonymously();
                        startActivity(new Intent(getApplicationContext(), MainActivity.class));
                        Toast.makeText(this, "You have logged out.", Toast.LENGTH_SHORT).show();
                    })
                    .setNegativeButton(R.string.cancel, (dialog, which) -> {
                        navController.navigate(R.id.nav_home);
                        dialog.dismiss();
                    }).create()
                    .show();
        } else {
            navController.navigate(R.id.nav_home);
            Toast.makeText(this, "You are not logged in", Toast.LENGTH_SHORT).show();
        }
    }

    private void shareApp() {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, "http://www.verityfoods.com");
        startActivity(Intent.createChooser(shareIntent, "Share with"));
    }


    public void getCurrentUserDetails() {
        View headerView = navigationView.getHeaderView(0);
        LinearLayout loggedInDrawer = headerView.findViewById(R.id.logged_user_drawer);
        LinearLayout notLoggedInDrawer = headerView.findViewById(R.id.not_logged_user_drawer);
        MaterialButton loginNow = headerView.findViewById(R.id.login_now);
        TextView currentUserName = headerView.findViewById(R.id.current_user_name);
        TextView currentUserAddress = headerView.findViewById(R.id.current_user_address);
        ImageView currentUserImage = headerView.findViewById(R.id.user_pic);

        if (vars.isLoggedIn()) {
            loggedInDrawer.setVisibility(View.VISIBLE);
            notLoggedInDrawer.setVisibility(View.GONE);

            vars.verityApp.db.collection(Globals.USERS)
                    .document(userUid)
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            user = documentSnapshot.toObject(User.class);
                            if (user != null) {
                                currentUserAddress.setText(user.getAddress());
                                currentUserName.setText(user.getName());

                                if (user.getImage() != null) {
                                    Picasso.get()
                                            .load(user.getImage())
                                            .placeholder(R.drawable.avatar)
                                            .error(R.drawable.avatar)
                                            .into(currentUserImage);
                                } else {
                                    currentUserImage.setBackgroundResource(R.drawable.avatar);
                                }
                            }
                        }
                    });

        } else {
            loggedInDrawer.setVisibility(View.GONE);
            notLoggedInDrawer.setVisibility(View.VISIBLE);
            loginNow.setOnClickListener(view -> {
                startActivity(new Intent(getApplicationContext(), SignupActivity.class));
                finish();
            });
        }
    }

    public void getCartCount() {
        badgeDrawable = bottomNav.getBadge(R.id.navigation_cart);
        vars.verityApp.db.collection(Globals.CART)
                .document(vars.getShoppingID())
                .collection(Globals.MY_CART)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    int count = queryDocumentSnapshots.size();
                    if (badgeDrawable == null) {
                        bottomNav.getOrCreateBadge(R.id.navigation_cart).setNumber(count);
                    } else {
                        badgeDrawable.setNumber(count);
                    }

                })
                .addOnFailureListener(e -> {
                    vars.verityApp.crashlytics.recordException(e);
                    Log.e(TAG, "Error while getting cart count: ",e );
                });
    }


    @Override
    public void onDestinationChanged(@NonNull NavController controller, @NonNull NavDestination destination, @Nullable Bundle arguments) {
        if(destination.getId()== R.id.nav_logout) {
            bottomNav.setVisibility(View.GONE);
        } else {
            bottomNav.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_search) {
            startActivity(new Intent(getApplicationContext(), SearchActivity.class));
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        drawer.closeDrawers();
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