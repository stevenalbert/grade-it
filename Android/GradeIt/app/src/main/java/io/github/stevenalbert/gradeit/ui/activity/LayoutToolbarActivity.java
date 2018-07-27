package io.github.stevenalbert.gradeit.ui.activity;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;

import io.github.stevenalbert.gradeit.R;

/**
 * Created by Steven Albert on 6/25/2018.
 */
public class LayoutToolbarActivity extends ToolbarActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_layout_toolbar);

        setupToolbar();
    }

    protected void changeFragment(Fragment newFragment) {
        FragmentManager manager = getSupportFragmentManager();
        FragmentTransaction transaction = manager.beginTransaction();
        transaction.replace(R.id.frame_layout, newFragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }

    @Override
    public void onBackPressed() {
        if(getSupportFragmentManager().getFragments().size() == 1)
            finish();
        else
            super.onBackPressed();
    }
}
