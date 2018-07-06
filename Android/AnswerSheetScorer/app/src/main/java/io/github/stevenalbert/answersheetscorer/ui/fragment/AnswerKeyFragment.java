package io.github.stevenalbert.answersheetscorer.ui.fragment;


import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import io.github.stevenalbert.answersheetscorer.R;
import io.github.stevenalbert.answersheetscorer.model.AnswerKey;
import io.github.stevenalbert.answersheetscorer.ui.adapter.AnswerNumberAdapter;

/**
 * A simple {@link Fragment} subclass.
 */
public class AnswerKeyFragment extends Fragment {

    private RecyclerView answerRecyclerView;
    private AnswerKey answerKey;

    public AnswerKeyFragment() {
        // Required empty public constructor
    }

    public static AnswerKeyFragment newInstance(AnswerKey answerKey) {
        AnswerKeyFragment answerKeyFragment = new AnswerKeyFragment();
        answerKeyFragment.answerKey = answerKey;
        return answerKeyFragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_answer_key, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        answerRecyclerView = view.findViewById(R.id.answer_recycler_view);
        AnswerNumberAdapter adapter = new AnswerNumberAdapter(getContext());
        answerRecyclerView.setAdapter(adapter);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
        answerRecyclerView.setLayoutManager(layoutManager);

        adapter.setAnswers(answerKey.getAnswerKeys());
    }
}
