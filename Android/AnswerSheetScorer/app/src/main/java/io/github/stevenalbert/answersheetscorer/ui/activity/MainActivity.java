package io.github.stevenalbert.answersheetscorer.ui.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.FrameLayout;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import io.github.stevenalbert.answersheetscorer.R;
import io.github.stevenalbert.answersheetscorer.ui.fragment.AnalysisFragment;
import io.github.stevenalbert.answersheetscorer.ui.fragment.GetMarkFragment;
import io.github.stevenalbert.answersheetscorer.ui.fragment.HomeFragment;
import io.github.stevenalbert.answersheetscorer.ui.fragment.ViewMarksFragment;
import io.github.stevenalbert.answersheetscorer.ui.listener.OnFragmentInteractionListener;

public class MainActivity extends TabActivity implements OnFragmentInteractionListener {

    // TAG
    private static final String TAG = MainActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Setup Toolbar
        setupToolbar();

        // Setup Tab Layout and View
        setupTabActivity(getAllTabFragments());
    }

    private ArrayList<TabFragment> getAllTabFragments() {
        final List<TabFragment> TAB_FRAGMENTS = Arrays.asList(
                new TabFragment(new GetMarkFragment(), this.getString(R.string.mark_answer_title)),
                new TabFragment(new ViewMarksFragment(), this.getString(R.string.view_marks_title)),
                new TabFragment(new AnalysisFragment(), this.getString(R.string.answer_analysis_title))
        );

        return new ArrayList<>(TAB_FRAGMENTS);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.popupmenu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.help) {
            return true;
        }
        else if (id == R.id.about_us) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onFragmentInteraction(Uri uri) {

    }
}
