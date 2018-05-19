package model.label;

public class ExCodeLabel extends AnswerSheetLabel {

	private static final int COLUMN_LIMIT = 3;
	private static final int VALUE_LIMIT = 10;
	
	public ExCodeLabel(int columnNumber, int value) {
		super(String.valueOf(columnNumber), String.valueOf(value));
		
		if(!isColumnNumberValid(columnNumber) || !isValueValid(value))
			throw new IllegalArgumentException("Argument is not valid");
	}
	
	private boolean isColumnNumberValid(int columnNumber) {
		if(columnNumber >= 0 && columnNumber < COLUMN_LIMIT)
			return true;
		else throw new IllegalArgumentException("Column number limits from 0 (inclusive) to " + COLUMN_LIMIT + "exclusive");
	}
	
	private boolean isValueValid(int value) {
		if(value >= 0 && value < VALUE_LIMIT) 
			return true;
		else throw new IllegalArgumentException("Value limits from 0 (inclusive) to " + VALUE_LIMIT + "exclusive");
	}
	
	public int getColumnNumber() {
		return Integer.valueOf(getColInfo());
	}
	
	public int getValue() {
		return Integer.valueOf(getRowInfo());
	}
}
