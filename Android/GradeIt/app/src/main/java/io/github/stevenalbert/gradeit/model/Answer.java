package io.github.stevenalbert.gradeit.model;

import java.util.EnumMap;

public class Answer {

    private EnumMap<Option, Boolean> isChosen;

    public Answer() {
        isChosen = new EnumMap<>(Option.class);
    }

    public void setOptionChosen(Option option, boolean value) {
        isChosen.put(option, value);
    }

    public boolean isOptionChosen(Option option) {
        return ( isChosen.containsKey(option) ? isChosen.get(option) : false );
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Answer) {
            Answer answer = (Answer) obj;
            Option[] options = Option.values();
            for (Option option : options) {
                if (this.isOptionChosen(option) != answer.isOptionChosen(option))
                    return false;
            }
            return true;
        }
        return false;
    }
}
