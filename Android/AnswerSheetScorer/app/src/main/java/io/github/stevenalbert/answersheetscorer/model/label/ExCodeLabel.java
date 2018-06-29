package io.github.stevenalbert.answersheetscorer.model.label;

public class ExCodeLabel extends AnswerSheetLabel {

    private static final int COLUMN_LIMIT = 10;
    private static final int VALUE_LIMIT = 10;

    public ExCodeLabel(int columnNumber, int value) {
        super(String.valueOf(columnNumber), String.valueOf(value), SORT_BY_COLUMN);

        if (!isColumnNumberValid(columnNumber) || !isValueValid(value))
            throw new IllegalArgumentException("Argument is not valid");
    }

    private boolean isColumnNumberValid(int columnNumber) {
        if (columnNumber >= 0 && columnNumber < COLUMN_LIMIT)
            return true;
        else
            throw new IllegalArgumentException("column number = " + columnNumber
                    + ", column number limits from 0 (inclusive) to " + COLUMN_LIMIT + " (exclusive)");
    }

    private boolean isValueValid(int value) {
        if (value >= 0 && value < VALUE_LIMIT)
            return true;
        else
            throw new IllegalArgumentException(
                    "value = " + value + ", value limits from 0 (inclusive) to " + VALUE_LIMIT + " (exclusive)");
    }

    public int getColumnNumber() {
        return Integer.valueOf(getColInfo());
    }

    public int getValue() {
        return Integer.valueOf(getRowInfo());
    }

    @Override
    public int compareTo(AnswerSheetLabel o) {
        if (!(o instanceof ExCodeLabel))
            return super.compareTo(o);
        else {
            ExCodeLabel l = (ExCodeLabel) o;
            if (this.getColumnNumber() == l.getColumnNumber())
                if (this.getValue() == l.getValue())
                    return 0;
                else
                    return Integer.compare(this.getValue(), l.getValue());
            else
                return Integer.compare(this.getColumnNumber(), l.getColumnNumber());
        }
    }

    @Override
    public String toString() {
        return "ExCode (" + getColInfo() + "," + getRowInfo() + ")";
    }
}
