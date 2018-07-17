package io.github.stevenalbert.answersheetscorer.ui.fragment;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.TableLayout;
import android.widget.TableRow;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

import io.github.stevenalbert.answersheetscorer.R;
import io.github.stevenalbert.answersheetscorer.model.AnswerKey;
import io.github.stevenalbert.answersheetscorer.model.AnswerKeyCode;
import io.github.stevenalbert.answersheetscorer.model.AnswerSheet;
import io.github.stevenalbert.answersheetscorer.process.AnalysisProcess;
import io.github.stevenalbert.answersheetscorer.ui.adapter.AnswerMCodeAdapter;
import io.github.stevenalbert.answersheetscorer.ui.listener.OnFragmentInteractionListener;
import io.github.stevenalbert.answersheetscorer.viewmodel.AnswerKeyViewModel;
import io.github.stevenalbert.answersheetscorer.viewmodel.AnswerSheetViewModel;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link OnFragmentInteractionListener} interface
 * to handle interaction events.
 */
public class AnalysisFragment extends Fragment implements AnswerMCodeAdapter.OnSelectMCodeListener {

    private RecyclerView mCodeRecyclerView;
    private AnswerMCodeAdapter adapter;

    private AnswerKeyViewModel answerKeyViewModel;
    private LiveData<List<AnswerKeyCode>> answerKeyList;

    private OnSelectItemMCodeListener listener;

    public interface OnSelectItemMCodeListener {
        void onSelectItemMCode(int mCode);
    }

    @Override
    public void onSelectMCode(int mCode) {
        if(listener != null) {
            listener.onSelectItemMCode(mCode);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_analysis, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mCodeRecyclerView = view.findViewById(R.id.m_code_recycler_view);

        adapter = new AnswerMCodeAdapter(getContext(), this);
        mCodeRecyclerView.setAdapter(adapter);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
        mCodeRecyclerView.setLayoutManager(layoutManager);

        answerKeyViewModel = ViewModelProviders.of(this).get(AnswerKeyViewModel.class);

        answerKeyList = answerKeyViewModel.getAnswerKeysMetadata();

        answerKeyList.observe(this, new Observer<List<AnswerKeyCode>>() {
            @Override
            public void onChanged(@Nullable List<AnswerKeyCode> answerKeyCodes) {
                adapter.setAnswerKeyCodes(answerKeyCodes);
            }
        });
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnSelectItemMCodeListener) {
            listener = (OnSelectItemMCodeListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement " + OnSelectItemMCodeListener.class.getSimpleName());
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        listener = null;
    }
}
