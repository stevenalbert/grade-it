package model;

public enum Option {

	A, B, C, D, E, X;
	
	public Character getOption() {
		return this.name().charAt(0);
	}
}
