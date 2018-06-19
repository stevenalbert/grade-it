package io.github.stevenalbert.answersheetscorer.ui.activity;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.Toolbar;
import android.view.View;

import io.github.stevenalbert.answersheetscorer.R;

/**
 * Created by Steven Albert on 6/19/2018.
 */
public class ToolbarActivity extends BaseActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    /**
     * Add custom toolbar on top of activity
     * Called on {@link #onCreate} after {@link #setContentView}
     */
    protected void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
    }
}
