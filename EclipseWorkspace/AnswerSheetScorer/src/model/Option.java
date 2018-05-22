package model;

public enum Option {

    A, B, C, D, E, X;

    public Character getOption() {
        return this.name().charAt(0);
    }

    public static boolean isValid(Character c) {
        if (getOption(c) != null)
            return true;
        else
            return false;
    }

    public static Option getOption(Character c) {
        Option[] options = Option.values();
        for (int i = 0; i < options.length; i++) {
            if (options[i].getOption().equals(c)) {
                return options[i];
            }
        }
        return null;
    }
}
