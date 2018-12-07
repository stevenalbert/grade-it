package io.github.stevenalbert.gradeit.ui.activity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import io.github.stevenalbert.gradeit.R;
import io.github.stevenalbert.gradeit.model.AnswerKeyCode;
import io.github.stevenalbert.gradeit.model.AnswerSheetCode;
import io.github.stevenalbert.gradeit.ui.fragment.AnalysisFragment;
import io.github.stevenalbert.gradeit.ui.fragment.GetMarkFragment;
import io.github.stevenalbert.gradeit.ui.fragment.ViewMarksFragment;
import io.github.stevenalbert.gradeit.util.AppSharedPreference;
import io.github.stevenalbert.gradeit.util.FileUtils;
import io.github.stevenalbert.gradeit.util.MetadataUtils;

public class MainActivity extends TabActivity implements
        GetMarkFragment.GetMarkListener,
        ViewMarksFragment.OnSelectListener,
        AnalysisFragment.OnSelectItemMCodeListener {

    // TAG
    private static final String TAG = MainActivity.class.getSimpleName();
    private static final int WRITE_EXTERNAL_STORAGE_PERMISSION_CODE = 1100;
    // Parent Layout
    private CoordinatorLayout parentLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initApps();
    }

    private void initApps() {
        if(!AppSharedPreference.isInit(this)) {
            if(ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[] {
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                }, WRITE_EXTERNAL_STORAGE_PERMISSION_CODE);
                return;
            }
            File metadataDirectory = MetadataUtils.getMetadataDirectory();
            Log.d(TAG, metadataDirectory.getAbsolutePath());
            metadataDirectory.mkdirs();

            try {
                final String directory = getString(R.string.metadata_directory_name);
                String[] filenames = getAssets().list(directory);
                for(String filename : filenames) {
                    File file = new File(metadataDirectory, filename);
                    FileUtils.copyFromAsset(this, directory + "/" + filename, file);
                }
                AppSharedPreference.saveMetadataString(this, filenames[0]);
            } catch (IOException e) {
                e.printStackTrace();
            }
            AppSharedPreference.setInit(this);
        }

        setContentView(R.layout.activity_main);

        // Setup Toolbar
        setupToolbar();

        // Setup Tab Layout and View
        setupTabActivity(getAllTabFragments());

        parentLayout = findViewById(R.id.parent_layout);
    }

    @Override
    protected void onResume() {
        super.onResume();
        setTitle(R.string.app_name);
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
        // getMenuInflater().inflate(R.menu.popupmenu, menu);
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
/*
        else if (id == R.id.about_us) {
            return true;
        }
*/

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onProcessImageTaken(Uri uri) {
        Intent processIntent = new Intent(this, ProcessActivity.class);
        processIntent.setData(uri);
        startActivity(processIntent);
    }

    @Override
    public void onFinishDownloadAnswerSheet(File downloadFolder) {
        Snackbar snackbar = Snackbar.make(parentLayout, getString(R.string.success_download_answer_sheet, downloadFolder.getPath()), Snackbar.LENGTH_LONG);
        snackbar.show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onSelectAnswerSheet(AnswerSheetCode answerSheetCode) {
        Intent answerSheetDetailIntent = new Intent(this, AnswerSheetDetailActivity.class);
        answerSheetDetailIntent.putExtra(AnswerSheetDetailActivity.ANSWER_SHEET_CODE_INTENT_KEY, answerSheetCode);
        startActivity(answerSheetDetailIntent);
    }

    @Override
    public void onSelectAnswerKey(AnswerKeyCode answerKeyCode) {
        Intent answerKeyDetailIntent = new Intent(this, AnswerKeyDetailActivity.class);
        answerKeyDetailIntent.putExtra(AnswerKeyDetailActivity.ANSWER_KEY_INTENT_KEY, answerKeyCode);
        startActivity(answerKeyDetailIntent);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == WRITE_EXTERNAL_STORAGE_PERMISSION_CODE) {
            if(grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                initApps();
            } else {
                finish();
            }
        }
    }

    @Override
    public void onSelectItemMCode(int mCode) {
        Intent analysisProcessIntent = new Intent(this, AnalysisProcessActivity.class);
        analysisProcessIntent.putExtra(AnalysisProcessActivity.M_CODE_KEY, mCode);
        startActivity(analysisProcessIntent);
    }
}
