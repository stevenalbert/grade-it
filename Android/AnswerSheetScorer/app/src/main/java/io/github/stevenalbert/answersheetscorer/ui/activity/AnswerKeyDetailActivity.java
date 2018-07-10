package io.github.stevenalbert.answersheetscorer.ui.activity;

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

import io.github.stevenalbert.answersheetscorer.R;
import io.github.stevenalbert.answersheetscorer.model.AnswerKey;
import io.github.stevenalbert.answersheetscorer.model.AnswerSheet;
import io.github.stevenalbert.answersheetscorer.ui.fragment.AnswerKeyFragment;
import io.github.stevenalbert.answersheetscorer.viewmodel.AnswerKeyViewModel;

/**
 * Created by Steven Albert on 7/8/2018.
 */
public class AnswerKeyDetailActivity extends LayoutToolbarActivity {

    public static final String ANSWER_KEY_INTENT_KEY = "answer_key";

    private AnswerKey answerKey;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle bundle = getIntent().getExtras();
        if(bundle != null) {
            answerKey = bundle.getParcelable(ANSWER_KEY_INTENT_KEY);
            changeFragment(AnswerKeyFragment.newInstance(answerKey));
        } else {
            Toast.makeText(this, "No answer key is found", Toast.LENGTH_SHORT).show();
        }

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    protected void onResume() {
        super.onResume();
        setTitle(R.string.answer_key_detail_activity_title);
    }

    public void onDeleteAnswerKey(AnswerKey answerKey) {
        ViewModelProviders.of(this).get(AnswerKeyViewModel.class).delete(answerKey);
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
                                onDeleteAnswerKey(answerKey);
                            }
                        }).create();
                alertDialog.show();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

}
