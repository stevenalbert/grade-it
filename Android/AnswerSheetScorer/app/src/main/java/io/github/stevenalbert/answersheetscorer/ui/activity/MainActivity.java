package io.github.stevenalbert.answersheetscorer.ui.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.Menu;
import android.view.MenuItem;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import io.github.stevenalbert.answersheetscorer.R;
import io.github.stevenalbert.answersheetscorer.model.AnswerKey;
import io.github.stevenalbert.answersheetscorer.model.AnswerSheet;
import io.github.stevenalbert.answersheetscorer.ui.fragment.AnalysisFragment;
import io.github.stevenalbert.answersheetscorer.ui.fragment.AnswerKeyFragment;
import io.github.stevenalbert.answersheetscorer.ui.fragment.GetMarkFragment;
import io.github.stevenalbert.answersheetscorer.ui.fragment.ViewMarksFragment;
import io.github.stevenalbert.answersheetscorer.ui.listener.OnFragmentInteractionListener;

public class MainActivity extends TabActivity implements OnFragmentInteractionListener, GetMarkFragment.OnFragmentInteractionListener, ViewMarksFragment.OnSelectListener {

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

    @Override
    public void onProcessImageTaken(Uri uri) {
        Intent processIntent = new Intent(this, ProcessActivity.class);
        processIntent.setData(uri);
        startActivity(processIntent);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onSelectAnswerSheet(AnswerSheet answerSheet) {
        Intent answerSheetDetailIntent = new Intent(this, AnswerSheetDetailActivity.class);
        answerSheetDetailIntent.putExtra(AnswerSheetDetailActivity.ANSWER_SHEET_INTENT_KEY, answerSheet);
        startActivity(answerSheetDetailIntent);
    }

    @Override
    public void onSelectAnswerKey(AnswerKey answerKey) {
        Intent answerKeyDetailIntent = new Intent(this, AnswerKeyDetailActivity.class);
        answerKeyDetailIntent.putExtra(AnswerKeyDetailActivity.ANSWER_KEY_INTENT_KEY, answerKey);
        startActivity(answerKeyDetailIntent);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }
}
