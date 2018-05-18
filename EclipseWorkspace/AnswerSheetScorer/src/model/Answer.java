package model;

import java.util.ArrayList;

public class Answer {

	public enum Option {
		A, B, C, D, E;
		
		public Character getOption() {
			return this.name().charAt(0);
		}
	}
	
	private Option chosenOption;
	private ArrayList<Option> cancelledOptions;
	
	public Answer(Option chosenOption) {
		this(chosenOption, null);
	}
	
	public Answer(Option chosenOption, ArrayList<Option> cancelledOptions) {
		setChosenOptions(chosenOption);
		setCancelledOptions(cancelledOptions);
	}
	
	private void setCancelledOptions(ArrayList<Option> cancelledOptions) {
		this.cancelledOptions = (cancelledOptions == null ? new ArrayList<>() : cancelledOptions); 
	}
	
	private void setChosenOptions(Option chosenOption) {
		if(this.chosenOption != null && chosenOption != null && !this.chosenOption.equals(chosenOption)) 
			addCancelledOption(this.chosenOption);
		this.chosenOption = chosenOption;
	}
	
	public void addCancelledOption(Option cancelledOption) {
		if(this.chosenOption.equals(cancelledOption)) 
			setChosenOptions(null);
		if(!this.cancelledOptions.contains(cancelledOption)) 
			this.cancelledOptions.add(cancelledOption);
	}
	
	public Option getChosenOption() {
		return this.chosenOption;
	}
	
	public ArrayList<Option> getCancelledOptions() {
		return new ArrayList<>(this.cancelledOptions);
	}
	
	public boolean hasChosenOption() {
		return this.chosenOption != null;
	}
	
	public boolean isChosenOptionEquals(Answer answer) {
		if(answer != null && this.hasChosenOption() && answer.hasChosenOption())
			return this.chosenOption.equals(answer.getChosenOption());
		else return false;
	}
	
	public boolean isCancelledOptionsEquals(Answer answer) {
		if(answer != null && this.cancelledOptions != null) 
			return this.cancelledOptions.equals(answer.getCancelledOptions());
		else return false;
	}
	
	@Override
	public boolean equals(Object obj) {
		if(obj instanceof Answer) {
			Answer answer = (Answer) obj;
			return this.isChosenOptionEquals(answer) && isCancelledOptionsEquals(answer);
		}
		return false;
	}
}
