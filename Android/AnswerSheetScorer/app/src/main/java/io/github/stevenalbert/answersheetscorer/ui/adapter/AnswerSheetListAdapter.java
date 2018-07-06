package io.github.stevenalbert.answersheetscorer.ui.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import io.github.stevenalbert.answersheetscorer.R;
import io.github.stevenalbert.answersheetscorer.model.AnswerSheet;

/**
 * Created by Steven Albert on 7/5/2018.
 */
public class AnswerSheetListAdapter extends RecyclerView.Adapter<AnswerSheetListAdapter.AnswerSheetViewHolder> {

    private LayoutInflater layoutInflater;
    private List<AnswerSheet> answerSheets;

    public AnswerSheetListAdapter(Context context) {
        layoutInflater = LayoutInflater.from(context);
    }

    @NonNull
    @Override
    public AnswerSheetViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new AnswerSheetViewHolder(
                layoutInflater.inflate(R.layout.answer_sheet_list_item, parent, false)
        );
    }

    @Override
    public void onBindViewHolder(@NonNull AnswerSheetViewHolder holder, int position) {
        if(answerSheets != null) {
            AnswerSheet answerSheet = answerSheets.get(position);
            holder.exCodeTextView.setText(String.valueOf(answerSheet.getExCode()));
            holder.mCodeTextView.setText(String.valueOf(answerSheet.getMCode()));
        } else {
            holder.exCodeTextView.setText("No ExCode");
            holder.mCodeTextView.setText("No MCode");
        }
    }

    public void setAnswerSheets(List<AnswerSheet> answerSheets) {
        this.answerSheets = answerSheets;
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        if(answerSheets != null) return answerSheets.size();
        else return 0;
    }

    public class AnswerSheetViewHolder extends RecyclerView.ViewHolder {

        private TextView exCodeTextView;
        private TextView mCodeTextView;

        public AnswerSheetViewHolder(View itemView) {
            super(itemView);
            exCodeTextView = itemView.findViewById(R.id.ex_code_text);
            mCodeTextView = itemView.findViewById(R.id.m_code_text);
        }
    }
}
