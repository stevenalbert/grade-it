package io.github.stevenalbert.answersheetscorer.ui.fragment;


import android.arch.lifecycle.ViewModelProviders;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import io.github.stevenalbert.answersheetscorer.R;
import io.github.stevenalbert.answersheetscorer.database.AppDatabase;
import io.github.stevenalbert.answersheetscorer.model.Answer;
import io.github.stevenalbert.answersheetscorer.model.AnswerSheet;
import io.github.stevenalbert.answersheetscorer.model.Option;
import io.github.stevenalbert.answersheetscorer.viewmodel.AnswerSheetViewModel;

/**
 * A simple {@link Fragment} subclass.
 */
public class ProcessResultFragment extends Fragment {

    // TAG
    private static final String TAG = ProcessResultFragment.class.getSimpleName();

    // Views
    private TextView answerSheetResult;

    // Processed AnswerSheet
    private AnswerSheet answerSheet;

    // AppDatabase
    private AppDatabase database;

    public ProcessResultFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_process_result, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        answerSheetResult = view.findViewById(R.id.answer_sheet_result);

        answerSheetResult.setText(readAnswerSheet());

        database = AppDatabase.getInstance(getContext());

        AnswerSheetViewModel answerSheetViewModel = ViewModelProviders.of(this).get(AnswerSheetViewModel.class);
        answerSheetViewModel.insert(answerSheet);
    }

    public static ProcessResultFragment newInstance(AnswerSheet answerSheet) {
        ProcessResultFragment newFragment = new ProcessResultFragment();
        newFragment.answerSheet = answerSheet;
        return newFragment;
    }

    private String readAnswerSheet() {
        StringBuilder builder = new StringBuilder();

        // EXCODE
        builder.append("ExCode = ");
        builder.append(answerSheet.getExCode());
        builder.append('\n');

        // MCODE
        builder.append("MCode = ");
        builder.append(answerSheet.getMCode());
        builder.append('\n');

        // ANSWERS
        Option[] options = Option.values();
        for (int number = 1; number <= answerSheet.getTotalAnswer(); number++) {
            Answer answer = answerSheet.getAnswerOn(number);
            builder.append(number);
            builder.append(".    ");
            builder.append((number < 10 ? " " : ""));
            for (Option option : options) {
                builder.append((answer.isOptionChosen(option) ? 1 : 0));
                builder.append("  ");
            }
            builder.append('\n');
        }

        return builder.toString();
    }

    private void onFinishedInsertTask(List<Long> ids) {
        Toast.makeText(getContext(), "Finish inserting answer sheet with ids: " + ids.toString(), Toast.LENGTH_SHORT).show();
        Log.d(TAG, "Finish inserting answer sheet with ids: " + ids.toString());
    }

    private class AnswerSheetInsertTask extends AsyncTask<AnswerSheet, Void, List<Long>> {

        private AppDatabase database;

        public AnswerSheetInsertTask(AppDatabase database) {
            this.database = database;
        }

        @Override
        protected List<Long> doInBackground(AnswerSheet... answerSheets) {
            List<Long> ids = new ArrayList<>();
            for(AnswerSheet answerSheet : answerSheets) {
                ids.add(this.database.answerSheetDao().insert(answerSheet));
            }
            return ids;
        }

        @Override
        protected void onPostExecute(List<Long> longs) {
            super.onPostExecute(longs);
            onFinishedInsertTask(longs);
        }
    }
}
