package io.github.stevenalbert.answersheetscorer.ui.fragment;


import android.arch.lifecycle.ViewModelProviders;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
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
import io.github.stevenalbert.answersheetscorer.model.AnswerKey;
import io.github.stevenalbert.answersheetscorer.model.AnswerSheet;
import io.github.stevenalbert.answersheetscorer.model.Option;
import io.github.stevenalbert.answersheetscorer.ui.adapter.AnswerNumberAdapter;
import io.github.stevenalbert.answersheetscorer.viewmodel.AnswerKeyViewModel;
import io.github.stevenalbert.answersheetscorer.viewmodel.AnswerSheetViewModel;

/**
 * A simple {@link Fragment} subclass.
 */
public class AnswerSheetDetailFragment extends Fragment {

    // TAG
    private static final String TAG = AnswerSheetDetailFragment.class.getSimpleName();

    // Views
    private RecyclerView answerRecyclerView;
    private TextView exCodeText;
    private TextView mCodeText;

    // AnswerSheet
    private AnswerSheet answerSheet;

    public AnswerSheetDetailFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_answer_sheet_detail, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        answerRecyclerView = view.findViewById(R.id.answer_recycler_view);
        AnswerNumberAdapter adapter = new AnswerNumberAdapter(getContext());
        adapter.setAnswers(answerSheet.getAnswers());

        answerRecyclerView.setAdapter(adapter);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
        answerRecyclerView.setLayoutManager(layoutManager);

        DividerItemDecoration divider = new DividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL);
        answerRecyclerView.addItemDecoration(divider);

        mCodeText = view.findViewById(R.id.m_code_text);
        StringBuilder mCodeTextBuilder = new StringBuilder("000");
        String mCodeTextString = Integer.toString(answerSheet.getMCode());
        mCodeTextBuilder.replace(mCodeTextBuilder.length() - mCodeTextString.length(), mCodeTextBuilder.length(), mCodeTextString);
        mCodeText.setText("MCode = " + mCodeTextBuilder.toString());

        exCodeText = view.findViewById(R.id.ex_code_text);
        StringBuilder exCodeTextBuilder = new StringBuilder("000");
        String exCodeTextString = Integer.toString(answerSheet.getExCode());
        exCodeTextBuilder.replace(exCodeTextBuilder.length() - exCodeTextString.length(), exCodeTextBuilder.length(), exCodeTextString);
        exCodeText.setText("ExCode = " + exCodeTextBuilder.toString());
    }

    public static AnswerSheetDetailFragment newInstance(AnswerSheet answerSheet) {
        AnswerSheetDetailFragment newFragment = new AnswerSheetDetailFragment();
        newFragment.answerSheet = answerSheet;
        return newFragment;
    }
}
