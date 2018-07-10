package io.github.stevenalbert.answersheetscorer.ui.activity;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.widget.Toast;

import java.util.List;

import io.github.stevenalbert.answersheetscorer.model.AnswerKey;
import io.github.stevenalbert.answersheetscorer.model.AnswerSheet;
import io.github.stevenalbert.answersheetscorer.ui.fragment.AnswerSheetDetailFragment;
import io.github.stevenalbert.answersheetscorer.viewmodel.AnswerKeyViewModel;

/**
 * Created by Steven Albert on 7/6/2018.
 */
public class AnswerSheetDetailActivity extends LayoutToolbarActivity {

    public static final String ANSWER_SHEET_INTENT_KEY = "answer_sheet";

    private AnswerSheet answerSheet;
    private AnswerKey answerKey;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle bundle = getIntent().getExtras();
        if(bundle != null) {
            answerSheet = bundle.getParcelable(ANSWER_SHEET_INTENT_KEY);
            ViewModelProviders.of(this).get(AnswerKeyViewModel.class).getAnswerKeyByMCode(answerSheet.getMCode())
                    .observe(this, new Observer<List<AnswerKey>>() {
                        @Override
                        public void onChanged(@Nullable List<AnswerKey> answerKeys) {
                            if(answerKeys.size() > 0) {
                                answerKey = answerKeys.get(0);
                                changeFragment(AnswerSheetDetailFragment.newInstance(answerSheet, answerKey));
                            }
                        }
                    });
        } else {
            Toast.makeText(this, "No answer sheet is found", Toast.LENGTH_SHORT).show();
        }

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }
}
