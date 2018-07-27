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
public class AnswerMCodeAdapter extends RecyclerView.Adapter<AnswerMCodeAdapter.ViewHolder> {

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
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(layoutInflater.inflate(R.layout.analysis_m_code_item, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        if(answerKeyCodes != null) {
            AnswerKeyCode answerKeyCode = answerKeyCodes.get(position);
            holder.setMCodeText(answerKeyCode);
        }
    }

    @Override
    public int getItemCount() {
        return (answerKeyCodes == null ? 0 : answerKeyCodes.size());
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

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(listener != null) {
                        listener.onSelectMCode(Integer.valueOf(mCodeText.getText().toString()));
                    }
                }
            });
        }

        private void setMCodeText(AnswerKeyCode answerKeyCode) {
            this.mCodeText.setText(answerKeyCode.getMCodeString());
        }
    }
}
