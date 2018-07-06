package io.github.stevenalbert.answersheetscorer.ui.activity;

import android.os.Bundle;
import android.util.Log;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.OpenCVLoader;

import io.github.stevenalbert.answersheetscorer.R;
import io.github.stevenalbert.answersheetscorer.model.AnswerKey;
import io.github.stevenalbert.answersheetscorer.model.AnswerSheet;
import io.github.stevenalbert.answersheetscorer.ui.fragment.ProcessFragment;
import io.github.stevenalbert.answersheetscorer.ui.fragment.AnswerSheetDetailFragment;

public class ProcessActivity extends LayoutToolbarActivity implements ProcessFragment.OnProcessFinishListener {

    // TAG
    private static final String TAG = ProcessActivity.class.getSimpleName();

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
        OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION, this, mLoaderCallback);
    }

    @Override
    public void onFinish(AnswerSheet answerSheet) {
        if(AnswerKey.isAnswerKey(answerSheet)) {
            AnswerKey answerKey = AnswerKey.fromAnswerSheet(answerSheet);
        } else {
            changeFragment(AnswerSheetDetailFragment.newInstance(answerSheet));
        }
    }
}
