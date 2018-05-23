package model.label;

import model.Option;

public class AnswerLabel extends AnswerSheetLabel {

    private static final int NUMBER_LIMIT = 99;

    public AnswerLabel(int number, Option option) {
        super(String.valueOf(option.getOption()), String.valueOf(number));

        if (!isNumberValid(number) || option == null)
            throw new IllegalArgumentException("Argument is not valid");
    }

    private boolean isNumberValid(int number) {
        if (number >= 0 && number < NUMBER_LIMIT)
            return true;
        else
            throw new IllegalArgumentException("Number limits from 0 (inclusive) to " + NUMBER_LIMIT + " (exclusive)");
    }

    public int getNumber() {
        return Integer.valueOf(getRowInfo());
    }

    public Option getOption() {
        return Option.getOption(getColInfo().charAt(0));
    }
}
