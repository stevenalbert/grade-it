package io.github.stevenalbert.answersheetscorer.ui.fragment;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

import io.github.stevenalbert.answersheetscorer.R;
import io.github.stevenalbert.answersheetscorer.model.AnswerSheet;
import io.github.stevenalbert.answersheetscorer.ui.adapter.AnswerSheetListAdapter;
import io.github.stevenalbert.answersheetscorer.ui.listener.OnFragmentInteractionListener;
import io.github.stevenalbert.answersheetscorer.viewmodel.AnswerSheetViewModel;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link OnFragmentInteractionListener} interface
 * to handle interaction events.
 */
public class ViewMarksFragment extends Fragment {

    private OnFragmentInteractionListener mListener;

    // Views
    private RecyclerView marksRecyclerView;
    // Adapter
    private AnswerSheetListAdapter adapter;
    // ViewModel
    private AnswerSheetViewModel answerSheetViewModel;

    public ViewMarksFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_view_marks, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        marksRecyclerView = view.findViewById(R.id.marks_recycler_view);
        adapter = new AnswerSheetListAdapter(getContext());
        marksRecyclerView.setAdapter(adapter);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
        marksRecyclerView.setLayoutManager(layoutManager);

        answerSheetViewModel = ViewModelProviders.of(this).get(AnswerSheetViewModel.class);
        answerSheetViewModel.getAnswerSheets().observe(this, new Observer<List<AnswerSheet>>() {
            @Override
            public void onChanged(@Nullable List<AnswerSheet> answerSheets) {
                adapter.setAnswerSheets(answerSheets);
            }
        });
    }

    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }
}
