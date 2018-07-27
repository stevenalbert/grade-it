package io.github.stevenalbert.gradeit.model;

import android.arch.persistence.room.TypeConverter;

/**
 * Created by Steven Albert on 7/3/2018.
 */
public class AnswerSheetConverter {

    private static final String SEPARATOR = ",";

    @TypeConverter
    public static String answerToString(Answer answer) {
        StringBuilder builder = new StringBuilder();
        Option[] options = Option.values();
        for (Option option : options) {
            if (answer.isOptionChosen(option))
                builder.append(option.getOption());
        }

        return builder.toString();
    }

    @TypeConverter
    public static Answer answerFromString(String string) {
        Answer answer = new Answer();
        for(int i = 0; i < string.length(); i++) {
            answer.setOptionChosen(Option.getOption(string.charAt(i)), true);
        }
        return answer;
    }

    @TypeConverter
    public static String answersToString(Answer[] answers) {
        StringBuilder builder = new StringBuilder();
        int length = answers.length;
        for(int i = 0; i < length; i++) {
            builder.append(answerToString(answers[i]));
            if(i < length - 1) builder.append(SEPARATOR);
        }
        return builder.toString();
    }

    @TypeConverter
    public static Answer[] answersFromString(String string) {
        String[] values = string.split(SEPARATOR);
        Answer[] answers = new Answer[values.length];
        int length = answers.length;
        for(int i = 0; i < length; i++) {
            answers[i] = answerFromString(values[i]);
        }
        return answers;
    }

    @TypeConverter
    public static String verdictsToString(int[] verdicts) {
        StringBuilder builder = new StringBuilder();
        for(int i : verdicts) {
            builder.append(i);
        }
        return builder.toString();
    }

    @TypeConverter
    public static int[] verdictsFromString(String string) {
        int[] verdicts = new int[string.length()];
        for(int i = 0; i < verdicts.length; i++) {
            verdicts[i] = Integer.valueOf(string.substring(i, i + 1));
        }
        return verdicts;
    }
}
