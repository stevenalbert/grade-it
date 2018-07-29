package model;

import java.util.Arrays;

public class AnswerSheet {

    private static final int MINIMUM_EX_CODE = 000;
    private static final int MAXIMUM_EX_CODE = 999;
    private static final int MINIMUM_M_CODE = 000;
    private static final int MAXIMUM_M_CODE = 999;
    private static final int DEFAULT_TOTAL_ANSWER = 40;
    private static final int ANSWER_TRUE = 1;
    private static final int ANSWER_FALSE = 0;
    private static final int ANSWER_MULTIPLE = 2;
    private static final int NO_VERDICT = 3;

    protected long id;
    protected int exCode;
    protected int mCode;
    protected Answer[] answers;
    protected int[] verdicts;
    protected int totalCorrect;

    public AnswerSheet(int exCode, int mCode, Answer[] answers, int[] verdicts, int totalCorrect) {
        setExCode(exCode);
        setMCode(mCode);
        setAnswers(answers);
        setVerdicts(verdicts);
        this.totalCorrect = totalCorrect;
    }

    public AnswerSheet(int exCode, int mCode, int totalAnswer) {
        setExCode(exCode);
        setMCode(mCode);
        createEmptyAnswers(totalAnswer);
        createAnswerVerdicts(totalAnswer);
    }

    public AnswerSheet(int exCode, int mCode, Answer[] answers) {
        setExCode(exCode);
        setMCode(mCode);
        setAnswers(answers);
        createAnswerVerdicts(this.answers.length);
    }

    public AnswerSheet(int totalAnswer) {
        this(0, 0, totalAnswer);
    }

    public AnswerSheet(String exCode, String mCode, Answer[] answers) {
        this(Integer.valueOf(exCode), Integer.valueOf(mCode), answers);
    }

    public AnswerSheet(String exCode, String mCode, int totalAnswer) {
        this(Integer.valueOf(exCode), Integer.valueOf(mCode), totalAnswer);
    }

    public AnswerSheet(int exCode, int mCode) {
        this(exCode, mCode, DEFAULT_TOTAL_ANSWER);
    }

    public AnswerSheet(String exCode, String mCode) {
        this(exCode, mCode, DEFAULT_TOTAL_ANSWER);
    }

    public void scoreAnswerSheet(AnswerKey answerKey) {
        if(this.getMCode() == answerKey.getMCode()) {
            int totalCorrect = 0;
            Option[] options = Option.values();
            for(int number = 1; number <= getTotalAnswer(); number++) {
                Option keyOption = answerKey.getAnswerKey(number);
                Answer answer = getAnswerOn(number);
                if(answer.isOptionChosen(keyOption)) {
                    setAnswerVerdict(number, ANSWER_TRUE);
                    totalCorrect++;
                    for(Option option : options) {
                        if(!option.equals(keyOption) && answer.isOptionChosen(option)) {
                            setAnswerVerdict(number, ANSWER_MULTIPLE);
                            totalCorrect--;
                            break;
                        }
                    }
                } else {
                    setAnswerVerdict(number, ANSWER_FALSE);
                }
            }
            this.totalCorrect = totalCorrect;
        }
    }

    public void setExCode(String exCode) {
        setExCode(Integer.valueOf(exCode));
    }

    private void setExCode(int exCode) {
        if (exCode >= MINIMUM_EX_CODE && exCode <= MAXIMUM_EX_CODE)
            this.exCode = exCode;
        else
            this.exCode = MINIMUM_EX_CODE;
    }

    public void setMCode(String mCode) {
        setMCode(Integer.valueOf(mCode));
    }

    private void setMCode(int mCode) {
        if (mCode >= MINIMUM_M_CODE && mCode <= MAXIMUM_M_CODE)
            this.mCode = mCode;
        else
            this.mCode = MINIMUM_M_CODE;
    }

    private void setAnswers(Answer[] answers) {
        if (answers == null)
            createEmptyAnswers(DEFAULT_TOTAL_ANSWER);
        else
            this.answers = answers;
    }

    private void setVerdicts(int[] verdicts) {
        if (verdicts == null)
            if(answers == null) createAnswerVerdicts(DEFAULT_TOTAL_ANSWER);
            else createAnswerVerdicts(answers.length);
        else
            this.verdicts = verdicts;
    }

    private void createEmptyAnswers(int totalAnswer) {
        if (totalAnswer < 0)
            totalAnswer = DEFAULT_TOTAL_ANSWER;

        this.answers = new Answer[totalAnswer];

        for (int i = 0; i < this.answers.length; i++) {
            this.answers[i] = new Answer();
        }
    }

    private void createAnswerVerdicts(int totalAnswer) {
        if (totalAnswer < 0)
            totalAnswer = DEFAULT_TOTAL_ANSWER;

        // Create new answers verdict
        verdicts = new int[totalAnswer];
        Arrays.fill(verdicts, NO_VERDICT);
    }

    public void setAnswerOn(int number, Answer answer) {
        if (number >= 1 && number <= this.answers.length) {
            this.answers[number - 1] = answer;
        } else throw new IndexOutOfBoundsException("Number starts from 1 to " + answers.length);
    }

    public void setAnswerOn(int number, Option option, boolean isChosen) {
        if (number >= 1 && number <= this.answers.length) {
            this.answers[number - 1].setOptionChosen(option, isChosen);
        } else throw new IndexOutOfBoundsException("Number starts from 1 to " + answers.length);
    }

    public void setAnswerVerdict(int number, int verdict) {
        if (number >= 1 && number <= this.answers.length) {
            this.verdicts[number - 1] = verdict;
        } else throw new IndexOutOfBoundsException("Number starts from 1 to " + answers.length);
    }

    public Answer getAnswerOn(int number) {
        if (number >= 1 && number <= this.answers.length) {
            return this.answers[number - 1];
        } else throw new IndexOutOfBoundsException("Number starts from 1 to " + answers.length);
    }

    public int getTotalAnswer() {
        return answers.length;
    }

    public int getExCode() {
        return this.exCode;
    }

    public String getExCodeString() {
        StringBuilder exCodeTextBuilder = new StringBuilder("000");
        String exCodeTextString = Integer.toString(getExCode());
        exCodeTextBuilder.replace(exCodeTextBuilder.length() - exCodeTextString.length(), exCodeTextBuilder.length(), exCodeTextString);
        return exCodeTextBuilder.toString();
    }

    public int getMCode() {
        return this.mCode;
    }

    public String getMCodeString() {
        StringBuilder mCodeTextBuilder = new StringBuilder("000");
        String mCodeTextString = Integer.toString(getMCode());
        mCodeTextBuilder.replace(mCodeTextBuilder.length() - mCodeTextString.length(), mCodeTextBuilder.length(), mCodeTextString);
        return mCodeTextBuilder.toString();
    }

    public int getAnswerVerdict(int number) {
        if (number >= 1 && number <= this.answers.length) {
            return this.verdicts[number - 1];
        } else throw new IndexOutOfBoundsException("Number starts from 1 to " + answers.length);
    }

    public boolean isAnswerTrue(int number) {
        return getAnswerVerdict(number) == ANSWER_TRUE;
    }

    public boolean isAnswerFalse(int number) {
        return getAnswerVerdict(number) == ANSWER_FALSE;
    }

    public boolean isMultipleAnswer(int number) {
        return getAnswerVerdict(number) == ANSWER_MULTIPLE;
    }

    public int getTotalCorrect() {
        return this.totalCorrect;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getId() {
        return id;
    }

    public Answer[] getAnswers() {
        return answers;
    }

    public int[] getVerdicts() {
        return verdicts;
    }
}
