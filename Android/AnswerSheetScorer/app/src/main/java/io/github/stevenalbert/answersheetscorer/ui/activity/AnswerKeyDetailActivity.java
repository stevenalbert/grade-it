package io.github.stevenalbert.answersheetscorer.ui.activity;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.widget.Toast;

import io.github.stevenalbert.answersheetscorer.model.AnswerKey;
import io.github.stevenalbert.answersheetscorer.model.AnswerSheet;
import io.github.stevenalbert.answersheetscorer.ui.fragment.AnswerKeyFragment;

/**
 * Created by Steven Albert on 7/8/2018.
 */
public class AnswerKeyDetailActivity extends LayoutToolbarActivity {

    public static final String ANSWER_KEY_INTENT_KEY = "answer_key";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle bundle = getIntent().getExtras();
        if(bundle != null) {
            AnswerKey answerKey = bundle.getParcelable(ANSWER_KEY_INTENT_KEY);
            changeFragment(AnswerKeyFragment.newInstance(answerKey));
        } else {
            Toast.makeText(this, "No answer key is found", Toast.LENGTH_SHORT).show();
        }

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }
}
