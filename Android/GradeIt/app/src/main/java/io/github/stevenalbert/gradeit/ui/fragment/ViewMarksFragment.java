package io.github.stevenalbert.gradeit.ui.fragment;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import io.github.stevenalbert.gradeit.R;
import io.github.stevenalbert.gradeit.model.AnswerKeyCode;
import io.github.stevenalbert.gradeit.model.AnswerSheetCode;
import io.github.stevenalbert.gradeit.ui.adapter.AnswerSheetListAdapter;
import io.github.stevenalbert.gradeit.viewmodel.AnswerKeyViewModel;
import io.github.stevenalbert.gradeit.viewmodel.AnswerSheetViewModel;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link OnSelectListener} interface
 * to handle interaction events.
 */
public class ViewMarksFragment extends Fragment implements AnswerSheetListAdapter.OnSelectListener, AdapterView.OnItemSelectedListener {

    private static final String TAG = ViewMarksFragment.class.getSimpleName();

    private OnSelectListener listener;

    private static final String ALL_MCODE = "All";

    // Views
    private RecyclerView marksRecyclerView;
    private Spinner mCodeSpinner;
    // Adapter
    private AnswerSheetListAdapter adapter;
    private ArrayAdapter<String> allMCodeAdapter;
    // ViewModel
    private AnswerSheetViewModel answerSheetViewModel;
    private AnswerKeyViewModel answerKeyViewModel;
    // List
    private LiveData<List<AnswerKeyCode>> answerKeyList;
    private LiveData<List<AnswerSheetCode>> answerSheetList;

    private Observer<List<AnswerKeyCode>> answerKeyObserver = new Observer<List<AnswerKeyCode>>() {
        @Override
        public void onChanged(@Nullable List<AnswerKeyCode> answerKeyCodes) {
            List<String> mCodeList = new ArrayList<>();
            List<AnswerKeyCode> answerKeyMetadata;
            answerKeyMetadata = answerKeyViewModel.getAnswerKeysMetadata().getValue();
            if(answerKeyMetadata != null) {
                mCodeList.add(ALL_MCODE);
                for (AnswerKeyCode answerKey : answerKeyMetadata) {
                    mCodeList.add(answerKey.getMCodeString());
                }
            }

            loadSpinnerData(mCodeList);
            adapter.setAnswerKeys(answerKeyCodes);
        }
    };

    private Observer<List<AnswerSheetCode>> answerSheetObserver = new Observer<List<AnswerSheetCode>>() {
        @Override
        public void onChanged(@Nullable List<AnswerSheetCode> answerSheetCodes) {
            adapter.setAnswerSheets(answerSheetCodes);
        }
    };

    @Override
    public void onSelectAnswerSheet(AnswerSheetCode answerSheetCode) {
        if (listener != null) {
            listener.onSelectAnswerSheet(answerSheetCode);
        }
    }

    @Override
    public void onSelectAnswerKey(AnswerKeyCode answerKeyCode) {
        if(listener != null) {
            listener.onSelectAnswerKey(answerKeyCode);
        }
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        if(answerSheetList != null) {
            answerSheetList.removeObserver(answerSheetObserver);
        }

        if(allMCodeAdapter.getItem(position).equals(ALL_MCODE)) {
            answerSheetList = answerSheetViewModel.getAnswerSheetsMetadata();
            adapter.setAnswerKeys(answerKeyList.getValue());
        } else {
            int mCode = Integer.valueOf(allMCodeAdapter.getItem(position));
            answerSheetList = answerSheetViewModel.getAnswerSheetsMetadataByMCode(mCode);
            adapter.setAnswerKeys(Collections.singletonList(new AnswerKeyCode(mCode)));
        }
        answerSheetList.observe(this, answerSheetObserver);

    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
        if(answerSheetList != null) {
            answerSheetList.removeObserver(answerSheetObserver);
        }

        answerSheetList = answerSheetViewModel.getAnswerSheetsMetadata();
        answerSheetList.observe(this, answerSheetObserver);

        adapter.setAnswerKeys(answerKeyList.getValue());
    }

    public interface OnSelectListener {
        void onSelectAnswerSheet(AnswerSheetCode answerSheetCode);
        void onSelectAnswerKey(AnswerKeyCode answerKeyCode);
    }

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
        mCodeSpinner = view.findViewById(R.id.m_code_spinner);

        adapter = new AnswerSheetListAdapter(getContext(), this);
        marksRecyclerView.setAdapter(adapter);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
        marksRecyclerView.setLayoutManager(layoutManager);

        answerSheetViewModel = ViewModelProviders.of(this).get(AnswerSheetViewModel.class);
        answerKeyViewModel = ViewModelProviders.of(this).get(AnswerKeyViewModel.class);

        answerKeyList = answerKeyViewModel.getAnswerKeysMetadata();
        answerSheetList = answerSheetViewModel.getAnswerSheetsMetadata();

        answerKeyList.observe(this, answerKeyObserver);
        answerSheetList.observe(this, answerSheetObserver);
    }

    @Override
    public void onResume() {
        super.onResume();
//        new LoadAnswersAsyncTask().execute(ALL_MCODE);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnSelectListener) {
            listener = (OnSelectListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement " + OnSelectListener.class.getSimpleName());
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        listener = null;
    }

    private void loadSpinnerData(List<String> mCodeList) {
        if(allMCodeAdapter == null) {
            allMCodeAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, mCodeList);
            allMCodeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            mCodeSpinner.setAdapter(allMCodeAdapter);
            mCodeSpinner.setOnItemSelectedListener(this);
        } else {
            allMCodeAdapter.clear();
            allMCodeAdapter.addAll(mCodeList);
        }
    }
}
