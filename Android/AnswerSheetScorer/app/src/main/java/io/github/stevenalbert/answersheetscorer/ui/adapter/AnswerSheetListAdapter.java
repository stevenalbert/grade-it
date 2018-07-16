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
import io.github.stevenalbert.answersheetscorer.model.AnswerKey;
import io.github.stevenalbert.answersheetscorer.model.AnswerKeyCode;
import io.github.stevenalbert.answersheetscorer.model.AnswerSheet;
import io.github.stevenalbert.answersheetscorer.model.AnswerSheetCode;

/**
 * Created by Steven Albert on 7/5/2018.
 */
public class AnswerSheetListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int ANSWER_KEY_VIEW_TYPE = 1;
    private static final int ANSWER_SHEET_VIEW_TYPE = 2;

    private OnSelectListener listener;
    private LayoutInflater layoutInflater;
    private List<AnswerSheetCode> answerSheets;
    private List<AnswerKeyCode> answerKeys;

    public interface OnSelectListener {
        void onSelectAnswerSheet(AnswerSheetCode answerSheetCode);
        void onSelectAnswerKey(AnswerKeyCode answerKeyCode);
    }

    public AnswerSheetListAdapter(Context context, OnSelectListener listener) {
        layoutInflater = LayoutInflater.from(context);
        this.listener = listener;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return (viewType == ANSWER_KEY_VIEW_TYPE ?
                new AnswerKeyViewHolder(
                        layoutInflater.inflate(R.layout.answer_key_list_item, parent, false))
                : new AnswerSheetViewHolder(
                        layoutInflater.inflate(R.layout.answer_sheet_list_item, parent, false)));
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if(answerKeys != null) {
            if(position < answerKeys.size()) {
                AnswerKeyCode answerKey = answerKeys.get(position);
                AnswerKeyViewHolder viewHolder = (AnswerKeyViewHolder) holder;

                viewHolder.cardView.setCardBackgroundColor(ContextCompat.getColor(layoutInflater.getContext(), R.color.main_menu_item));
                viewHolder.mCodeTextView.setText(answerKey.getMCodeString());
            } else {
                position -= answerKeys.size();

                AnswerSheetViewHolder viewHolder = (AnswerSheetViewHolder) holder;
                AnswerSheetCode answerSheet = answerSheets.get(position);

                viewHolder.cardView.setCardBackgroundColor(ContextCompat.getColor(layoutInflater.getContext(), R.color.colorAccent));
                viewHolder.exCodeTextView.setText(answerSheet.getExCodeString());
                viewHolder.mCodeTextView.setText(answerSheet.getMCodeString());
            }
        }
    }

    public void setAnswerSheets(List<AnswerSheetCode> answerSheets) {
        this.answerSheets = answerSheets;
        notifyDataSetChanged();
    }

    public void setAnswerKeys(List<AnswerKeyCode> answerKeys) {
        this.answerKeys = answerKeys;
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        int count = 0;
        if(answerKeys != null) count += answerKeys.size();
        if(answerSheets != null) count += answerSheets.size();
        return count;
    }

    @Override
    public int getItemViewType(int position) {
        if(answerKeys != null && position < answerKeys.size()) return ANSWER_KEY_VIEW_TYPE;
        else return ANSWER_SHEET_VIEW_TYPE;
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
                        int adapterPosition = getAdapterPosition() - (answerKeys != null ? answerKeys.size() : 0);
                        listener.onSelectAnswerSheet(answerSheets.get(adapterPosition));
                    }
                }
            });
        }
    }

    public class AnswerKeyViewHolder extends RecyclerView.ViewHolder {

        private CardView cardView;
        private TextView mCodeTextView;

        private AnswerKeyViewHolder(View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.answer_sheet_info_card_view);
            mCodeTextView = itemView.findViewById(R.id.m_code_text);
            cardView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(listener != null) {
                        int adapterPosition = getAdapterPosition();
                        listener.onSelectAnswerKey(answerKeys.get(adapterPosition));
                    }
                }
            });
        }
    }
}
