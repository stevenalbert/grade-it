package model;

public class AnswerSheet {

	private static final int MINIMUM_EX_CODE = 000;
	private static final int MAXIMUM_EX_CODE = 999;
	private static final int MINIMUM_M_CODE = 000;
	private static final int MAXIMUM_M_CODE = 999;
	private static final int DEFAULT_TOTAL_ANSWER = 40;
	
	private int exCode;
	private int mCode;
	private Answer[] answers;
	
	public AnswerSheet(int exCode, int mCode, Answer[] answers) {
		setExCode(exCode);
		setMCode(mCode);
		setAnswers(answers);
	}

	public AnswerSheet(String exCode, String mCode, Answer[] answers) {
		this(Integer.valueOf(exCode), Integer.valueOf(mCode), answers);
	}

	public AnswerSheet(int exCode, int mCode, int totalAnswer) {
		this(exCode, mCode, new Answer[totalAnswer]);
	}

	public AnswerSheet(String exCode, String mCode, int totalAnswer) {
		this(Integer.valueOf(exCode), Integer.valueOf(mCode), totalAnswer);
	}

	public AnswerSheet(int exCode, int mCode) {
		this(exCode, mCode, null);
	}

	public AnswerSheet(String exCode, String mCode) {
		this(exCode, mCode, null);
	}
	
	private void setExCode(int exCode) {
		if(exCode >= MINIMUM_EX_CODE && exCode <= MAXIMUM_EX_CODE) 
			this.exCode = exCode;
		else this.exCode = MINIMUM_EX_CODE;
	}
	
	private void setMCode(int mCode) {
		if(mCode >= MINIMUM_M_CODE && mCode <= MAXIMUM_M_CODE) 
			this.mCode = mCode;
		else this.mCode = MINIMUM_M_CODE;
	}
	
	private void setAnswers(Answer[] answers) {
		if(answers == null) 
			this.answers = new Answer[DEFAULT_TOTAL_ANSWER];
		else this.answers = answers;
	}
	
	public void setAnswerOn(int number, Answer answer) {
		if(number >= 1 && number <= this.answers.length) {
			this.answers[number - 1] = answer;
		}
	}
	
	public Answer getAnswerOn(int number) {
		return this.answers[number - 1];
	}
	
	public int getExCode() {
		return this.exCode;
	}
	
	public int getMCode() {
		return this.mCode;
	}
}
