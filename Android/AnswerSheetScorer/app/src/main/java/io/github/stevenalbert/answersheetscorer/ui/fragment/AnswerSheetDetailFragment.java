package io.github.stevenalbert.answersheetscorer.ui.fragment;


import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import io.github.stevenalbert.answersheetscorer.R;
import io.github.stevenalbert.answersheetscorer.model.AnswerKey;
import io.github.stevenalbert.answersheetscorer.model.AnswerSheet;
import io.github.stevenalbert.answersheetscorer.ui.adapter.AnswerNumberAdapter;

/**
 * A simple {@link Fragment} subclass.
 */
public class AnswerSheetDetailFragment extends Fragment {

    // TAG
    private static final String TAG = AnswerSheetDetailFragment.class.getSimpleName();

    private static final String ANSWER_SHEET_BUNDLE_KEY = "answer_sheet";
    private static final String ANSWER_KEY_BUNDLE_KEY = "answer_key";

    // Views
    private RecyclerView answerRecyclerView;
    private TextView exCodeText;
    private TextView mCodeText;

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
        AnswerSheet answerSheet = null;
        AnswerKey answerKey = null;
        Bundle bundle = getArguments();

        if(bundle != null) {
            answerSheet = bundle.getParcelable(ANSWER_SHEET_BUNDLE_KEY);
            answerKey = bundle.getParcelable(ANSWER_KEY_BUNDLE_KEY);
        }

        answerRecyclerView = view.findViewById(R.id.answer_recycler_view);
        AnswerNumberAdapter adapter = new AnswerNumberAdapter(getContext());
        adapter.setAnswerSheet(answerSheet, answerKey);

        answerRecyclerView.setAdapter(adapter);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
        answerRecyclerView.setLayoutManager(layoutManager);

        DividerItemDecoration divider = new DividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL);
        answerRecyclerView.addItemDecoration(divider);

        mCodeText = view.findViewById(R.id.m_code_text);
        mCodeText.setText(getString(R.string.m_code, answerSheet.getMCodeString()));

        exCodeText = view.findViewById(R.id.ex_code_text);
        exCodeText.setText(getString(R.string.ex_code, answerSheet.getExCodeString()));
    }

    public static AnswerSheetDetailFragment newInstance(AnswerSheet answerSheet, AnswerKey answerKey) {
        if(answerSheet == null || answerKey == null) {
            return null;
        }

        AnswerSheetDetailFragment newFragment = new AnswerSheetDetailFragment();
        Bundle bundle = new Bundle();
        bundle.putParcelable(ANSWER_SHEET_BUNDLE_KEY, answerSheet);
        bundle.putParcelable(ANSWER_KEY_BUNDLE_KEY, answerKey);
        newFragment.setArguments(bundle);
        return newFragment;
    }
}
