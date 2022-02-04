package org.asdtm.fas.activity;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.TaskStackBuilder;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import org.asdtm.fas.DonationDialog;
import org.asdtm.fas.InventumContextWrapper;
import org.asdtm.fas.R;
import org.asdtm.fas.util.AppUtils;
import org.asdtm.fas.util.PrefUtils;

public class BaseActivity extends AppCompatActivity {

    private final static String TAG = BaseActivity.class.getSimpleName();

    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mDrawerToggle;
    private Toolbar mToolbar;

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(InventumContextWrapper.wrap(base));
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        /* Lets the user know if they have internet connection */
        if (!AppUtils.isNetworkAvailableAndConnected(this)) {
            Snackbar.make(findViewById(android.R.id.content), getString(R.string.network_error), Snackbar.LENGTH_LONG).show();
        }
    }

    @Override
    public void setContentView(@LayoutRes int layoutResID) {
        super.setContentView(layoutResID);
        getToolbar();
    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        setupNavDrawer();
    }

    /* Configures the toolbar to be an action bar and sets an onclick listener to open navigation menu */
    private void setupNavDrawer() {
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (mDrawerLayout == null) {
            return;
        }
        mDrawerToggle = new ActionBarDrawerToggle(this,
                mDrawerLayout,
                mToolbar,
                R.string.navigation_drawer_open,
                R.string.navigation_drawer_close);
        mDrawerLayout.addDrawerListener(mDrawerToggle);
        mDrawerToggle.syncState();

        if (mToolbar != null) {
            mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mDrawerLayout.openDrawer(GravityCompat.START);
                }
            });
        }

        /* Sets up the listener on the navigation menu */
        configureNavView();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    /* Finds the navigation_view and adds a listener to it */
    private void configureNavView() {
        NavigationView navigationView = (NavigationView) findViewById(R.id.navigation_view);
        navigationView.setNavigationItemSelectedListener(navigationViewListener);
    }

    /* The listener to the navigation_view */
    private NavigationView.OnNavigationItemSelectedListener navigationViewListener =
            new NavigationView.OnNavigationItemSelectedListener() {
                /*  Opens activities based on the option selected */
                @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                    switch (item.getItemId()) {
                        case R.id.nav_menu_home:
                            startActivity(new Intent(BaseActivity.this, HomeActivity.class));
                            break;
                        case R.id.nav_menu_movies:
                            createBackStack(new Intent(BaseActivity.this, MoviesActivity.class));
                            break;
                        case R.id.nav_menu_tv:
                            createBackStack(new Intent(BaseActivity.this, TvActivity.class));
                            break;
                        case R.id.nav_menu_music:
                            createBackStack(new Intent(BaseActivity.this, MusicActivity.class));
                            break;
                        case R.id.nav_menu_favourites:
                            createBackStack(new Intent(BaseActivity.this, FavouritesActivity.class));
                            break;
                        case R.id.nav_menu_discover:
                            createBackStack(new Intent(BaseActivity.this, DiscoverActivity.class));
                            break;
                        case R.id.nav_menu_feedback:
                            Intent sendMessage = new Intent(Intent.ACTION_SEND);
                            sendMessage.setType("message/rfc822");
                            sendMessage.putExtra(Intent.EXTRA_EMAIL, new String[]{
                                    getResources().getString(R.string.feedback_email)});
                            try {
                                startActivity(Intent.createChooser(sendMessage, "Send feedback"));
                            } catch (android.content.ActivityNotFoundException e) {
                                Toast.makeText(BaseActivity.this, "Communication app not found",
                                        Toast.LENGTH_SHORT).show();
                            }
                            break;
                        case R.id.nav_menu_settings:
                            createBackStack(new Intent(BaseActivity.this, SettingsActivity.class));
                            break;
                        case R.id.nav_menu_donation:
                            DonationDialog dialog = DonationDialog.newInstance();
                            dialog.show(getFragmentManager(), "bitcoinDonationDialog");
                            break;
                    }

                    /* Closes the navigation menu after opening the new activity */
                    mDrawerLayout.closeDrawer(GravityCompat.START);
                    return true;
                }
            };

    /* Configures the toolbar to be an action bar and then returns the toolbar */
    protected Toolbar getToolbar() {
        if (mToolbar == null) {
            mToolbar = (Toolbar) findViewById(R.id.toolbar);
            if (mToolbar != null) {
                setSupportActionBar(mToolbar);
            }
        }

        return mToolbar;
    }

    private void createBackStack(Intent intent) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            TaskStackBuilder builder = TaskStackBuilder.create(this);
            builder.addNextIntentWithParentStack(intent);
            builder.startActivities();
        } else {
            startActivity(intent);
            finish();
        }
    }

    /* Closes the navigation menu with back button if its open */
    @Override
    public void onBackPressed() {
        if (isNavDrawerOpen()) {
            closeNavDraw();
        } else {
            super.onBackPressed();
        }
    }

    /* Checks if navigation drawer is open */
    protected boolean isNavDrawerOpen() {
        return mDrawerLayout != null && mDrawerLayout.isDrawerOpen(GravityCompat.START);
    }

    /* Closes navigation drawer */
    protected void closeNavDraw() {
        if (mDrawerLayout != null) {
            mDrawerLayout.closeDrawer(GravityCompat.START);
        }
    }


    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "Movies lang: " + PrefUtils.getFormatLocale(this));
    }
}
