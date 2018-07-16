package io.github.stevenalbert.answersheetscorer.ui.activity;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.widget.Toast;

import io.github.stevenalbert.answersheetscorer.model.AnswerKey;
import io.github.stevenalbert.answersheetscorer.model.AnswerSheet;
import io.github.stevenalbert.answersheetscorer.model.AnswerSheetCode;
import io.github.stevenalbert.answersheetscorer.ui.fragment.AnswerSheetDetailFragment;
import io.github.stevenalbert.answersheetscorer.viewmodel.AnswerKeyViewModel;
import io.github.stevenalbert.answersheetscorer.viewmodel.AnswerSheetViewModel;

/**
 * Created by Steven Albert on 7/6/2018.
 */
public class AnswerSheetDetailActivity extends LayoutToolbarActivity {

    public static final String ANSWER_SHEET_CODE_INTENT_KEY = "answer_sheet";

    // Answer
    private AnswerSheet answerSheetRetrieved;
    private AnswerKey answerKeyRetrieved;

    // ViewModel
    private AnswerKeyViewModel answerKeyViewModel;
    private AnswerSheetViewModel answerSheetViewModel;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle bundle = getIntent().getExtras();
        if(bundle != null) {
            AnswerSheetCode answerSheetCode = bundle.getParcelable(ANSWER_SHEET_CODE_INTENT_KEY);

            if(answerSheetCode != null) {
                answerKeyViewModel = ViewModelProviders.of(this).get(AnswerKeyViewModel.class);
                answerSheetViewModel = ViewModelProviders.of(this).get(AnswerSheetViewModel.class);

                answerSheetViewModel.getAnswerSheet(answerSheetCode.exCode, answerSheetCode.mCode).observe(this, new Observer<AnswerSheet>() {
                    @Override
                    public void onChanged(@Nullable AnswerSheet answerSheet) {
                        answerSheetRetrieved = answerSheet;
                        fillAnswer();
                    }
                });

                answerKeyViewModel.getAnswerKeyByMCode(answerSheetCode.mCode).observe(this, new Observer<AnswerKey>() {
                    @Override
                    public void onChanged(@Nullable AnswerKey answerKey) {
                        answerKeyRetrieved = answerKey;
                        fillAnswer();
                    }
                });
            }
        } else {
            Toast.makeText(this, "No answer sheet is found", Toast.LENGTH_SHORT).show();
            finish();
        }

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private synchronized void fillAnswer() {
        if(answerSheetRetrieved != null && answerKeyRetrieved != null) {
            changeFragment(AnswerSheetDetailFragment.newInstance(answerSheetRetrieved, answerKeyRetrieved));
        }
    }
}
