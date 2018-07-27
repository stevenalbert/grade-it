package io.github.stevenalbert.gradeit.ui.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.view.View;
import android.webkit.MimeTypeMap;

import java.io.File;

import io.github.stevenalbert.gradeit.R;
import io.github.stevenalbert.gradeit.ui.fragment.AnalysisProcessFragment;

/**
 * Created by Steven Albert on 7/16/2018.
 */
public class AnalysisProcessActivity extends LayoutToolbarActivity implements AnalysisProcessFragment.OnAnalysisListener {

    public static final String M_CODE_KEY = "m_code";

    private CoordinatorLayout parentLayout;
    private File currentFile;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle bundle = getIntent().getExtras();

        if(bundle != null) {
            int mCode = bundle.getInt(M_CODE_KEY);
            changeFragment(AnalysisProcessFragment.newInstance(mCode));
        }

        parentLayout = findViewById(R.id.parent_layout);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    protected void onResume() {
        super.onResume();
        setTitle(R.string.answer_analysis_title);
    }

    @Override
    public void onFinishSaveAnalysis(File file) {
        this.currentFile = file;
        Snackbar snackbar = Snackbar.make(parentLayout, "Saved in " + file.getAbsolutePath(), Snackbar.LENGTH_LONG);
        snackbar.setAction("Open", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
                String mimeString = mimeTypeMap.getMimeTypeFromExtension(currentFile.getName().substring(currentFile.getName().lastIndexOf('.') + 1));
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.setDataAndType(Uri.fromFile(currentFile), mimeString);
                startActivity(intent);
            }
        });
        snackbar.show();
    }
}
