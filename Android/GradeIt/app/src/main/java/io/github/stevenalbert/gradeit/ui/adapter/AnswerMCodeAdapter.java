package io.github.stevenalbert.gradeit.ui.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import io.github.stevenalbert.gradeit.R;
import io.github.stevenalbert.gradeit.model.AnswerKeyCode;

/**
 * Created by Steven Albert on 7/16/2018.
 */
public class AnswerMCodeAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int EMPTY_VIEW_TYPE = 0;
    private static final int OTHER_TYPE = 1;

    public interface OnSelectMCodeListener {
        void onSelectMCode(int mCode);
    }

    private OnSelectMCodeListener listener;
    private LayoutInflater layoutInflater;

    private List<AnswerKeyCode> answerKeyCodes;

    public AnswerMCodeAdapter(Context context, OnSelectMCodeListener listener) {
        layoutInflater = LayoutInflater.from(context);
        this.listener = listener;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        switch (viewType) {
            case EMPTY_VIEW_TYPE:
                return new EmptyViewHolder(layoutInflater.inflate(R.layout.empty_list, parent, false));
            case OTHER_TYPE:
            default:
                return new ViewHolder(layoutInflater.inflate(R.layout.analysis_m_code_item, parent, false));
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        switch (getItemViewType(position)) {
            case OTHER_TYPE:
                AnswerKeyCode answerKeyCode = answerKeyCodes.get(position);
                ((ViewHolder) holder).setMCodeText(answerKeyCode);
                break;
            case EMPTY_VIEW_TYPE:
                break;
        }
    }

    @Override
    public int getItemCount() {
        int count = (answerKeyCodes == null ? 0 : answerKeyCodes.size());
        return count == 0 ? 1 : count;
    }

    @Override
    public int getItemViewType(int position) {
        if(answerKeyCodes == null || answerKeyCodes.size() == 0) return EMPTY_VIEW_TYPE;
        return OTHER_TYPE;
    }

    public void setAnswerKeyCodes(List<AnswerKeyCode> answerKeyCodes) {
        this.answerKeyCodes = answerKeyCodes;
        notifyDataSetChanged();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private TextView mCodeText;

        public ViewHolder(View itemView) {
            super(itemView);

            mCodeText = itemView.findViewById(R.id.m_code_text);

            itemView.setOnClickListener((v) -> {
                if(listener != null) {
                    listener.onSelectMCode(Integer.valueOf(mCodeText.getText().toString()));
                }
            });
        }

        private void setMCodeText(AnswerKeyCode answerKeyCode) {
            this.mCodeText.setText(answerKeyCode.getMCodeString());
        }
    }

    public class EmptyViewHolder extends RecyclerView.ViewHolder {

        public EmptyViewHolder(View itemView) {
            super(itemView);
        }
    }
}
