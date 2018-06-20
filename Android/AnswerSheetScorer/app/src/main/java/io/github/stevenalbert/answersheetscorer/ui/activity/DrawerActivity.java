package io.github.stevenalbert.answersheetscorer.ui.activity;

import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.support.design.widget.NavigationView;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.util.Log;

import io.github.stevenalbert.answersheetscorer.R;

/**
 * Created by Steven Albert on 6/19/2018.
 */
public class DrawerActivity extends ToolbarActivity {

    private static final String TAG = DrawerActivity.class.getSimpleName();

    private DrawerLayout mDrawerLayout;
    private NavigationView mNavigationView;
    private NavigationView.OnNavigationItemSelectedListener navigationListener;

    protected void setupDrawer() {
        mDrawerLayout = findViewById(R.id.drawer_layout);
        mNavigationView = findViewById(R.id.navigation_view);

        ActionBarDrawerToggle mDrawerToggle;
        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, toolbar, R.string.drawer_open, R.string.drawer_close);
        mDrawerLayout.addDrawerListener(mDrawerToggle);
        mDrawerToggle.syncState();

        ActionBar actionBar = getSupportActionBar();

        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);

            Drawable menuDrawable = ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_menu_black);
            if (menuDrawable != null) {
                menuDrawable.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_IN);
                actionBar.setHomeAsUpIndicator(menuDrawable);
            }
        }
    }

    protected void setupNavigationItemListener(NavigationView.OnNavigationItemSelectedListener listener) {
        mNavigationView.setNavigationItemSelectedListener(listener);
        navigationListener = listener;
    }

    protected void setNavigationSelectedItem(int resId) {
        navigationListener.onNavigationItemSelected(mNavigationView.getMenu().findItem(resId));
    }
}
