package io.github.stevenalbert.answersheetscorer.ui.activity;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;

import io.github.stevenalbert.answersheetscorer.R;

/**
 * Created by Steven Albert on 6/19/2018.
 */
public class DrawerActivity extends ToolbarActivity {

    private DrawerLayout mDrawerLayout;
    private NavigationView mNavigationView;

    protected void setupDrawer() {
        mDrawerLayout = findViewById(R.id.drawer_layout);
        mDrawerLayout = findViewById(R.id.navigation_view);
    }
}
