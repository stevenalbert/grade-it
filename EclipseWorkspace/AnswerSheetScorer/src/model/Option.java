package model;

public enum Option {

    A, B, C, D, E;

    public Character getOption() {
        return this.name().charAt(0);
    }

    public static boolean isValid(Character c) {
        return getOption(c) != null;
    }

    public static Option getOption(Character c) {
        Option[] options = Option.values();
        for (Option option : options) {
            if (option.getOption().equals(c)) {
                return option;
            }
        }
        return null;
    }
}
