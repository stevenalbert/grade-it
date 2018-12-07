package io.github.stevenalbert.gradeit.ui.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.EnumMap;

import io.github.stevenalbert.gradeit.R;
import io.github.stevenalbert.gradeit.model.AnswerKey;
import io.github.stevenalbert.gradeit.model.Option;

/**
 * Created by Steven Albert on 7/7/2018.
 */
public class AnswerKeyNumberAdapter extends RecyclerView.Adapter<AnswerKeyNumberAdapter.AnswerKeyNumberViewHolder> {

    private LayoutInflater layoutInflater;
    private AnswerKey answerKey;

    public AnswerKeyNumberAdapter(Context context) {
        layoutInflater = LayoutInflater.from(context);
    }

    @NonNull
    @Override
    public AnswerKeyNumberViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new AnswerKeyNumberViewHolder(
                layoutInflater.inflate(R.layout.answer_number_item, parent, false)
        );
    }

    @Override
    public void onBindViewHolder(@NonNull AnswerKeyNumberViewHolder holder, int position) {
        holder.number.setText(Integer.toString(position + 1));
        if(answerKey != null) {
            Option trueOption = answerKey.getAnswerKey(position + 1);
            Option[] options = Option.values();
            for(Option option : options) {
                if(option.equals(trueOption)) {
                    holder.options.get(option).setBackgroundColor(
                            ContextCompat.getColor(layoutInflater.getContext(), android.R.color.holo_green_light)
                    );
                } else {
                    holder.options.get(option).setBackgroundColor(
                            ContextCompat.getColor(layoutInflater.getContext(), android.R.color.white)
                    );
                }
            }
        }
    }

    @Override
    public int getItemCount() {
        if(answerKey != null) return answerKey.getTotalNumber();
        else return 0;

    }

    public void setAnswerKey(AnswerKey answerKey) {
        this.answerKey = answerKey;
        notifyDataSetChanged();
    }

    public class AnswerKeyNumberViewHolder extends RecyclerView.ViewHolder {

        private TextView number;
        private EnumMap<Option, TextView> options;

        public AnswerKeyNumberViewHolder(View itemView) {
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
