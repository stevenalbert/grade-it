package io.github.stevenalbert.gradeit.model.label;

import io.github.stevenalbert.gradeit.model.Option;

public class AnswerLabel extends AnswerSheetLabel {

    private static final int NUMBER_LIMIT = 999;

    public AnswerLabel(int number, Option option) {
        super(String.valueOf(option.getOption()), String.valueOf(number), SORT_BY_ROW);

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

    @Override
    public int compareTo(AnswerSheetLabel o) {
        if (!(o instanceof AnswerLabel))
            return super.compareTo(o);
        else {
            AnswerLabel l = (AnswerLabel) o;
            if (this.getNumber() == l.getNumber())
                if (this.getOption().compareTo(l.getOption()) == 0)
                    return 0;
                else
                    return getOption().compareTo(l.getOption());
            else
                return Integer.compare(this.getNumber(), l.getNumber());
        }
    }

    @Override
    public String toString() {
        return "Answer #" + getRowInfo() + "- (" + getColInfo() + ")";
    }
}
