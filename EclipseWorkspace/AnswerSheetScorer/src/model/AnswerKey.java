package model;

/**
 * Created by Steven Albert on 7/6/2018.
 */
public class AnswerKey {

    private static final int DEFAULT_TOTAL_ANSWER = 40;

    public static final int ANSWER_KEY_EX_CODE = 000;

    protected int mCode;
    protected Option[] answerKeys;

    public AnswerKey(int mCode, Option[] answerKeys) {
        setMCode(mCode);
        setAnswerKeys(answerKeys);
    }

    public AnswerKey(int mCode, int totalNumber) {
        setMCode(mCode);
        setAnswerKeys(answerKeys);
    }

    private void setMCode(int mCode) {
        this.mCode = mCode;
    }

    private void setAnswerKeys(Option[] answerKeys) {
        if(answerKeys == null)
            this.answerKeys = new Option[DEFAULT_TOTAL_ANSWER];
        else this.answerKeys = answerKeys;
    }

    private void createAnswerKeys(int totalNumber) {
        if(totalNumber <= 0)
            totalNumber = DEFAULT_TOTAL_ANSWER;
        this.answerKeys = new Option[totalNumber];
    }

    public void setAnswerKey(int number, Option answer) {
        if (number >= 1 && number <= this.answerKeys.length) {
            this.answerKeys[number - 1] = answer;
        } else throw new IndexOutOfBoundsException("Number starts from 1 to " + answerKeys.length);
    }

    public int getMCode() {
        return mCode;
    }

    public String getMCodeString() {
        StringBuilder mCodeTextBuilder = new StringBuilder("000");
        String mCodeTextString = Integer.toString(getMCode());
        mCodeTextBuilder.replace(mCodeTextBuilder.length() - mCodeTextString.length(), mCodeTextBuilder.length(), mCodeTextString);
        return mCodeTextBuilder.toString();
    }

    public Option[] getAnswerKeys() {
        return answerKeys;
    }

    public Option getAnswerKey(int number) {
        if (number >= 1 && number <= this.answerKeys.length) {
            return this.answerKeys[number - 1];
        } else throw new IndexOutOfBoundsException("Number starts from 1 to " + answerKeys.length);
    }

    public static boolean isAnswerKey(AnswerSheet answerSheet) {
        return answerSheet.getExCode() == ANSWER_KEY_EX_CODE;
    }

    public static AnswerKey fromAnswerSheet(AnswerSheet answerSheet) {
        if(answerSheet.getExCode() == ANSWER_KEY_EX_CODE) {
            AnswerKey answerKey = new AnswerKey(answerSheet.getMCode(), answerSheet.getTotalAnswer());
            Option[] options = Option.values();
            for(int i = 1; i <= answerSheet.getTotalAnswer(); i++) {
                Answer answer = answerSheet.getAnswerOn(i);
                for(Option option : options) {
                    if(answer.isOptionChosen(option)) {
                        answerKey.setAnswerKey(i, option);
                        break;
                    }
                }
            }
            return answerKey;
        } else return null;
    }
}
