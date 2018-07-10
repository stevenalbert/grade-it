package io.github.stevenalbert.answersheetscorer.ui.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.EnumMap;

import io.github.stevenalbert.answersheetscorer.R;
import io.github.stevenalbert.answersheetscorer.model.Answer;
import io.github.stevenalbert.answersheetscorer.model.AnswerKey;
import io.github.stevenalbert.answersheetscorer.model.AnswerSheet;
import io.github.stevenalbert.answersheetscorer.model.Option;

/**
 * Created by Steven Albert on 7/6/2018.
 */
public class AnswerNumberAdapter extends RecyclerView.Adapter<AnswerNumberAdapter.AnswerNumberViewHolder> {

    private LayoutInflater layoutInflater;
    private AnswerSheet answerSheet;
    private AnswerKey answerKey;

    public AnswerNumberAdapter(Context context) {
        layoutInflater = LayoutInflater.from(context);
    }

    @NonNull
    @Override
    public AnswerNumberViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new AnswerNumberViewHolder(
                layoutInflater.inflate(R.layout.answer_number_item, parent, false)
        );
    }

    @Override
    public void onBindViewHolder(@NonNull AnswerNumberViewHolder holder, int position) {
        holder.number.setText(Integer.toString(position + 1));
        if(answerSheet != null && answerKey != null) {
            Answer answer = answerSheet.getAnswerOn(position + 1);
            Option trueOption = answerKey.getAnswerKey(position + 1);
            Option[] options = Option.values();
            for(Option option : options) {
                if(answer.isOptionChosen(option)) {
                    holder.options.get(option).setBackgroundColor(
                            ContextCompat.getColor(layoutInflater.getContext(),
                                    option.equals(trueOption) ?
                                            android.R.color.holo_green_light :
                                            android.R.color.holo_red_light)
                    );
                } else {
                    holder.options.get(option).setBackgroundColor(
                            ContextCompat.getColor(layoutInflater.getContext(),
                                    option.equals(trueOption) ?
                                            android.R.color.holo_green_dark :
                                            android.R.color.white)
                    );
                }
            }
        }
    }

    @Override
    public int getItemCount() {
        if(answerSheet != null) return answerSheet.getTotalAnswer();
        else return 0;

    }

    public void setAnswerSheet(AnswerSheet answerSheet, AnswerKey answerKey) {
        this.answerSheet = answerSheet;
        this.answerKey = answerKey;
        notifyDataSetChanged();
    }

    public class AnswerNumberViewHolder extends RecyclerView.ViewHolder {

        private TextView number;
        private EnumMap<Option, TextView> options;

        public AnswerNumberViewHolder(View itemView) {
            super(itemView);

            number = itemView.findViewById(R.id.number);
            options = new EnumMap<>(Option.class);
            options.put(Option.A, itemView.findViewById(R.id.option_a));
            options.put(Option.B, itemView.findViewById(R.id.option_b));
            options.put(Option.C, itemView.findViewById(R.id.option_c));
            options.put(Option.D, itemView.findViewById(R.id.option_d));
            options.put(Option.E, itemView.findViewById(R.id.option_e));
        }
    }
}
