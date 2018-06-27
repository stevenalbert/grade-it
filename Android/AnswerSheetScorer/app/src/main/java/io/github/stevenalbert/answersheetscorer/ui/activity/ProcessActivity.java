package io.github.stevenalbert.answersheetscorer.ui.activity;

import android.os.Bundle;

import io.github.stevenalbert.answersheetscorer.R;
import io.github.stevenalbert.answersheetscorer.ui.fragment.ProcessFragment;

public class ProcessActivity extends LayoutToolbarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setTitle(R.string.process_title);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        changeFragment(ProcessFragment.newInstance(getIntent().getData()));
    }
}
