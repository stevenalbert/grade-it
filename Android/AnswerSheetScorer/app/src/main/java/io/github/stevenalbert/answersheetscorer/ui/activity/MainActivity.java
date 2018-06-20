package io.github.stevenalbert.answersheetscorer.ui.activity;

import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.MenuItem;
import android.widget.FrameLayout;

import io.github.stevenalbert.answersheetscorer.R;
import io.github.stevenalbert.answersheetscorer.ui.fragment.HomeFragment;

public class MainActivity extends DrawerActivity implements HomeFragment.OnFragmentInteractionListener {

    // TAG
    private static final String TAG = MainActivity.class.getSimpleName();

    // FrameLayout
    private FrameLayout frameLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Setup Toolbar
        setupToolbar();

        // Setup Drawer and Navigation
        setupDrawer();
        setupNavigationItemListener(new MainNavigationListener());

        setNavigationSelectedItem(R.id.nav_home);
    }

    private void addMainFragmentToFrame() {
        addFragment(new HomeFragment(), false);
    }

    private void addFragment(Fragment newFragment, boolean isAddToBackStack) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.add(R.id.frame_layout, newFragment);
        if(isAddToBackStack)
            fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();
    }

    @Override
    public void onFragmentInteraction(Uri uri) {

    }

    private class MainNavigationListener implements NavigationView.OnNavigationItemSelectedListener {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {

            switch (item.getItemId()) {
                case R.id.nav_home:
                    addMainFragmentToFrame();
                    break;

                default:
                    Snackbar.make(getCurrentFocus(), item.getTitle() + " is not yet implemented", Snackbar.LENGTH_SHORT).show();
                    return false;
            }

            item.setChecked(true);
            return true;
        }
    }
}
