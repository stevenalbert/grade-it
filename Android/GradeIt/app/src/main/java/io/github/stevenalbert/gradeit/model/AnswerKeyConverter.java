package io.github.stevenalbert.gradeit.model;

import android.arch.persistence.room.TypeConverter;

/**
 * Created by Steven Albert on 7/7/2018.
 */
public class AnswerKeyConverter {

    private static final Character NULL_OPTION = '-';

    @TypeConverter
    public static String optionsToString(Option[] options) {
        StringBuilder builder = new StringBuilder();
        for(Option option : options) {
            builder.append(option == null ? NULL_OPTION : option.getOption());
        }
        return builder.toString();
    }

    @TypeConverter
    public static Option[] optionsFromString(String value) {
        Option[] options = new Option[value.length()];
        for(int i = 0; i < options.length; i++) {
            options[i] = value.charAt(i) == NULL_OPTION ? null : Option.getOption(value.charAt(i));
        }
        return options;
    }
}
