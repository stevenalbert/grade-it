package io.github.stevenalbert.gradeit.ui.activity;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.util.Log;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;

import io.github.stevenalbert.gradeit.R;
import io.github.stevenalbert.gradeit.model.AnswerKey;
import io.github.stevenalbert.gradeit.model.AnswerSheet;
import io.github.stevenalbert.gradeit.process.AnswerSheetScorer;
import io.github.stevenalbert.gradeit.ui.fragment.AnswerKeyFragment;
import io.github.stevenalbert.gradeit.ui.fragment.AnswerSheetDetailFragment;
import io.github.stevenalbert.gradeit.ui.fragment.ProcessFragment;
import io.github.stevenalbert.gradeit.viewmodel.AnswerKeyViewModel;
import io.github.stevenalbert.gradeit.viewmodel.AnswerSheetViewModel;

public class ProcessActivity extends LayoutToolbarActivity implements ProcessFragment.OnProcessFinishListener {

    // TAG
    private static final String TAG = ProcessActivity.class.getSimpleName();

    // AnswerSheet from recognition
    private AnswerSheet answerSheet = null;
    private AnswerKey answerKey = null;

    // ViewModel
    private AnswerSheetViewModel answerSheetViewModel;
    private AnswerKeyViewModel answerKeyViewModel;

    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case BaseLoaderCallback.SUCCESS:
                    Log.i(TAG, "OpenCV Loaded successfully");
                    changeFragment(ProcessFragment.newInstance(getIntent().getData()));
                    break;
                default:
                    super.onManagerConnected(status);
            }
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setTitle(R.string.process_title);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!OpenCVLoader.initDebug()) {
            Log.d(TAG, "Internal OpenCV library not found. Using OpenCV Manager for initialization");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION, this, mLoaderCallback);
        } else {
            Log.d(TAG, "OpenCV library found inside package. Using it!");
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }
    }

    @Override
    public void onFinish(AnswerSheet answerSheet) {
        if(answerSheet == null) {
            AlertDialog alertDialog = new AlertDialog.Builder(this)
                    .setMessage(getString(R.string.no_answer_sheet_error_message))
                    .setOnDismissListener(new DialogInterface.OnDismissListener() {
                        @Override
                        public void onDismiss(DialogInterface dialog) {finish();}
                    })
                    .create();
            alertDialog.show();
        } else {
            if(AnswerKey.isAnswerKey(answerSheet)) {
                AnswerKey key = AnswerKey.fromAnswerSheet(answerSheet);
                ViewModelProviders.of(this).get(AnswerKeyViewModel.class)
                        .insert(key);
                changeFragment(AnswerKeyFragment.newInstance(key));
            } else {
                this.answerSheet = answerSheet;
                answerKeyViewModel = ViewModelProviders.of(this).get(AnswerKeyViewModel.class);

                answerKeyViewModel.getAnswerKeyByMCode(answerSheet.getMCode()).observe(this, new Observer<AnswerKey>() {
                    @Override
                    public void onChanged(@Nullable AnswerKey answerKey) {
                        scoreAnswerSheet(answerKey);
                    }
                });
            }
        }
    }

    private void scoreAnswerSheet(AnswerKey answerKey) {
        Log.d(TAG, "AnswerKey = " + answerKey);
        if(answerKey == null) {
            AlertDialog alertDialog = new AlertDialog.Builder(this)
                    .setMessage(getString(R.string.no_answer_key_error_message, answerSheet.getMCodeString()))
                    .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            finish();
                        }
                    })
                    .create();

            alertDialog.show();
        } else {
            this.answerKey = answerKey;
            new ScoreAsyncTask(answerSheet, answerKey).execute();
        }
    }

    private void onFinishScoredAnswerSheet() {
        ViewModelProviders.of(this).get(AnswerSheetViewModel.class)
                .insert(answerSheet);
        changeFragment(AnswerSheetDetailFragment.newInstance(answerSheet, answerKey));
    }

    private class ScoreAsyncTask extends AsyncTask<Void, Void, Void> {

        private AnswerSheet answerSheet;
        private AnswerKey answerKey;

        private ScoreAsyncTask(AnswerSheet answerSheet, AnswerKey answerKey) {
            this.answerSheet = answerSheet;
            this.answerKey = answerKey;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            AnswerSheetScorer.scoreAnswerSheet(answerSheet, answerKey);
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            onFinishScoredAnswerSheet();
        }
    }
}
