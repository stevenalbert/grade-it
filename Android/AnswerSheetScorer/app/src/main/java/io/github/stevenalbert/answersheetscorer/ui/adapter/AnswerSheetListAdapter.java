package io.github.stevenalbert.answersheetscorer.ui.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.CardView;
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

    private OnSelectAnswerSheetListener listener;
    private LayoutInflater layoutInflater;
    private List<AnswerSheet> answerSheets;

    public interface OnSelectAnswerSheetListener {
        void onSelectAnswerSheet(AnswerSheet answerSheet);
    }

    public AnswerSheetListAdapter(Context context, OnSelectAnswerSheetListener listener) {
        layoutInflater = LayoutInflater.from(context);
        this.listener = listener;
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
            if(answerSheet.getMCode() != 0) {
                holder.cardView.setCardBackgroundColor(ContextCompat.getColor(layoutInflater.getContext(), R.color.colorAccent));
            }
            holder.exCodeTextView.setText(String.valueOf(answerSheet.getExCode()));
            holder.mCodeTextView.setText(String.valueOf(answerSheet.getMCode()));
        } else {
            holder.exCodeTextView.setText(R.string.none);
            holder.mCodeTextView.setText(R.string.none);
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

        private CardView cardView;
        private TextView exCodeTextView;
        private TextView mCodeTextView;

        private AnswerSheetViewHolder(View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.answer_sheet_info_card_view);
            exCodeTextView = itemView.findViewById(R.id.ex_code_text);
            mCodeTextView = itemView.findViewById(R.id.m_code_text);
            cardView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(listener != null) {
                        listener.onSelectAnswerSheet(answerSheets.get(getAdapterPosition()));
                    }
                }
            });
        }
    }
}
