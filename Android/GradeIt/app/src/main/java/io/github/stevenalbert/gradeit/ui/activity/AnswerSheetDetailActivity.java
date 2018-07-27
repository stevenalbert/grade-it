package io.github.stevenalbert.gradeit.ui.activity;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.DialogInterface;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v7.app.AlertDialog;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import io.github.stevenalbert.gradeit.R;
import io.github.stevenalbert.gradeit.model.AnswerKey;
import io.github.stevenalbert.gradeit.model.AnswerSheet;
import io.github.stevenalbert.gradeit.model.AnswerSheetCode;
import io.github.stevenalbert.gradeit.ui.fragment.AnswerSheetDetailFragment;
import io.github.stevenalbert.gradeit.viewmodel.AnswerKeyViewModel;
import io.github.stevenalbert.gradeit.viewmodel.AnswerSheetViewModel;

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


    public void onDeleteAnswerSheet(AnswerSheet answerSheet) {
        ViewModelProviders.of(this).get(AnswerSheetViewModel.class).delete(answerSheet);
        finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.answer_menu, menu);

        Drawable drawable = menu.findItem(R.id.delete).getIcon();

        drawable = DrawableCompat.wrap(drawable);
        DrawableCompat.setTint(drawable, ContextCompat.getColor(this, android.R.color.white));
        menu.findItem(R.id.delete).setIcon(drawable);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.delete:
                AlertDialog alertDialog = new AlertDialog.Builder(this).setCancelable(true)
                        .setTitle("Delete answer key")
                        .setMessage("Are you sure?")
                        .setNegativeButton("No", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                            }
                        })
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                onDeleteAnswerSheet(answerSheetRetrieved);
                            }
                        }).create();
                alertDialog.show();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
