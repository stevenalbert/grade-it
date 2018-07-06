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
import io.github.stevenalbert.answersheetscorer.model.Option;

/**
 * Created by Steven Albert on 7/6/2018.
 */
public class AnswerNumberAdapter extends RecyclerView.Adapter<AnswerNumberAdapter.AnswerNumberViewHolder> {

    private LayoutInflater layoutInflater;
    private Answer[] answers;

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
        if(answers != null) {
            Answer answer = answers[position];
            Option[] options = Option.values();
            for(Option option : options) {
                if(answer.isOptionChosen(option)) {
                    holder.options.get(option).setBackgroundColor(
                            ContextCompat.getColor(layoutInflater.getContext(), android.R.color.holo_green_light)
                    );
                }
            }
        }
    }

    @Override
    public int getItemCount() {
        return 0;
    }

    public void setAnswers(Answer[] answers) {
        this.answers = answers;
        notifyDataSetChanged();
    }

    public void setAnswers(Option[] answersOption) {
        this.answers = new Answer[answersOption.length];
        for(int i = 0; i < answersOption.length; i++) {
            this.answers[i].setOptionChosen(answersOption[i], true);
        }
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
