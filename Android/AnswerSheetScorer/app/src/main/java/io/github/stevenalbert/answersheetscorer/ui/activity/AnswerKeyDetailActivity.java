package io.github.stevenalbert.answersheetscorer.ui.activity;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.DialogInterface;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v7.app.AlertDialog;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import io.github.stevenalbert.answersheetscorer.R;
import io.github.stevenalbert.answersheetscorer.model.AnswerKey;
import io.github.stevenalbert.answersheetscorer.model.AnswerKeyCode;
import io.github.stevenalbert.answersheetscorer.model.AnswerSheet;
import io.github.stevenalbert.answersheetscorer.ui.fragment.AnswerKeyFragment;
import io.github.stevenalbert.answersheetscorer.viewmodel.AnswerKeyViewModel;

/**
 * Created by Steven Albert on 7/8/2018.
 */
public class AnswerKeyDetailActivity extends LayoutToolbarActivity {

    public static final String ANSWER_KEY_INTENT_KEY = "answer_key";

    private AnswerKeyCode answerKeyCode;
    private AnswerKey answerKey;

    private AnswerKeyViewModel answerKeyViewModel;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle bundle = getIntent().getExtras();
        if(bundle != null) {
            answerKeyCode = bundle.getParcelable(ANSWER_KEY_INTENT_KEY);

            if(answerKeyCode != null) {
                answerKeyViewModel = ViewModelProviders.of(this).get(AnswerKeyViewModel.class);
                answerKeyViewModel.getAnswerKeyByMCode(answerKeyCode.mCode).observe(this, new Observer<AnswerKey>() {
                    @Override
                    public void onChanged(@Nullable AnswerKey answerKey) {
                        fillAnswerKey(answerKey);
                    }
                });
            }
        } else {
            Toast.makeText(this, "No answer key is found", Toast.LENGTH_SHORT).show();
        }

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void fillAnswerKey(AnswerKey answerKey) {
        this.answerKey = answerKey;
        changeFragment(AnswerKeyFragment.newInstance(answerKey));
    }

    @Override
    protected void onResume() {
        super.onResume();
        setTitle(R.string.answer_key_detail_activity_title);
    }
}
