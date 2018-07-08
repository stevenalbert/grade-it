package io.github.stevenalbert.answersheetscorer.ui.activity;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.widget.Toast;

import io.github.stevenalbert.answersheetscorer.model.AnswerSheet;
import io.github.stevenalbert.answersheetscorer.ui.fragment.AnswerSheetDetailFragment;

/**
 * Created by Steven Albert on 7/6/2018.
 */
public class AnswerSheetDetailActivity extends LayoutToolbarActivity {

    public static final String ANSWER_SHEET_INTENT_KEY = "answer_sheet";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle bundle = getIntent().getExtras();
        if(bundle != null) {
            AnswerSheet answerSheet = bundle.getParcelable(ANSWER_SHEET_INTENT_KEY);
//            changeFragment(AnswerSheetDetailFragment.newInstance(answerSheet));
        } else {
            Toast.makeText(this, "No answer sheet is found", Toast.LENGTH_SHORT).show();
        }

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }
}
