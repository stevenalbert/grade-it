package model;

import java.util.EnumMap;

public class Answer {

    private EnumMap<Option, Boolean> isChosen;

    public Answer() {
        isChosen = new EnumMap<Option, Boolean>(Option.class);
    }

    public void setOptionChosen(Option option, boolean value) {
        isChosen.put(option, value);
    }

    public boolean isOptionChosen(Option option) {
        return isChosen.get(option);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Answer) {
            Answer answer = (Answer) obj;
            Option[] options = Option.values();
            for (Option option : options) {
                if (isChosen.get(option) != answer.isChosen.get(option))
                    return false;
            }
            return true;
        }
        return false;
    }
}
