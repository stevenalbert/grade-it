package io.github.stevenalbert.answersheetscorer.model;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.Index;
import android.arch.persistence.room.PrimaryKey;
import android.arch.persistence.room.TypeConverters;
import android.os.Parcel;
import android.os.Parcelable;

import java.util.Arrays;

@Entity (tableName = "answer_sheet",
        indices = {@Index(value = {"ex_code", "m_code"}, unique = true)})
@TypeConverters({
        AnswerSheetConverter.class
})
public class AnswerSheet implements Parcelable {

    @Ignore
    private static final int MINIMUM_EX_CODE = 000;
    @Ignore
    private static final int MAXIMUM_EX_CODE = 999;
    @Ignore
    private static final int MINIMUM_M_CODE = 000;
    @Ignore
    private static final int MAXIMUM_M_CODE = 999;
    @Ignore
    private static final int DEFAULT_TOTAL_ANSWER = 40;
    @Ignore
    public static final boolean ANSWER_TRUE = true;
    @Ignore
    public static final boolean ANSWER_FALSE = false;

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    protected long id;

    @ColumnInfo(name = "ex_code")
    protected int exCode;
    @ColumnInfo(name = "m_code")
    protected int mCode;
    @ColumnInfo(name = "answers")
    protected Answer[] answers;
    @ColumnInfo(name = "scored")
    protected boolean isScored;
    @ColumnInfo(name = "verdicts")
    protected boolean[] verdicts;

    public AnswerSheet(int exCode, int mCode, Answer[] answers, boolean isScored, boolean[] verdicts) {
        setExCode(exCode);
        setMCode(mCode);
        setAnswers(answers);
        setVerdicts(verdicts);
    }

    @Ignore
    public AnswerSheet(int exCode, int mCode, int totalAnswer) {
        setExCode(exCode);
        setMCode(mCode);
        createEmptyAnswers(totalAnswer);
        createAnswerVerdicts(totalAnswer);
    }

    @Ignore
    public AnswerSheet(int exCode, int mCode, Answer[] answers) {
        setExCode(exCode);
        setMCode(mCode);
        setAnswers(answers);
        createAnswerVerdicts(this.answers.length);
    }

    @Ignore
    public AnswerSheet(int totalAnswer) {
        this(0, 0, totalAnswer);
    }

    @Ignore
    public AnswerSheet(String exCode, String mCode, Answer[] answers) {
        this(Integer.valueOf(exCode), Integer.valueOf(mCode), answers);
    }

    @Ignore
    public AnswerSheet(String exCode, String mCode, int totalAnswer) {
        this(Integer.valueOf(exCode), Integer.valueOf(mCode), totalAnswer);
    }

    @Ignore
    public AnswerSheet(int exCode, int mCode) {
        this(exCode, mCode, DEFAULT_TOTAL_ANSWER);
    }

    @Ignore
    public AnswerSheet(String exCode, String mCode) {
        this(exCode, mCode, DEFAULT_TOTAL_ANSWER);
    }

    protected AnswerSheet(Parcel in) {
        id = in.readLong();
        exCode = in.readInt();
        mCode = in.readInt();
        answers = AnswerSheetConverter.answersFromString(in.readString());
        isScored = in.readByte() != 0;
        verdicts = in.createBooleanArray();
    }

    public static final Creator<AnswerSheet> CREATOR = new Creator<AnswerSheet>() {
        @Override
        public AnswerSheet createFromParcel(Parcel in) {
            return new AnswerSheet(in);
        }

        @Override
        public AnswerSheet[] newArray(int size) {
            return new AnswerSheet[size];
        }
    };

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

    private void setVerdicts(boolean[] verdicts) {
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
        verdicts = new boolean[totalAnswer];
        Arrays.fill(verdicts, ANSWER_FALSE);
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

    public void setAnswerVerdict(int number, boolean verdict) {
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

    public int getMCode() {
        return this.mCode;
    }

    public boolean getAnswerVerdict(int number) {
        if (number >= 1 && number <= this.answers.length) {
            return isScored && this.verdicts[number - 1];
        } else throw new IndexOutOfBoundsException("Number starts from 1 to " + answers.length);
    }

    public boolean isAnswerTrue(int number) {
        return getAnswerVerdict(number) == ANSWER_TRUE;
    }

    public boolean isAnswerFalse(int number) {
        return getAnswerVerdict(number) == ANSWER_FALSE;
    }

    public boolean isScored() {
        return isScored;
    }

    public void setId(long id) {
        this.id = id;
    }

    public void setScored(boolean scored) {
        isScored = scored;
    }

    public long getId() {
        return id;
    }

    public Answer[] getAnswers() {
        return answers;
    }

    public boolean[] getVerdicts() {
        return verdicts;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(id);
        dest.writeInt(getExCode());
        dest.writeInt(getMCode());
        dest.writeString(AnswerSheetConverter.answersToString(getAnswers()));
        dest.writeByte((byte) (isScored() ? 1 : 0));
        dest.writeBooleanArray(getVerdicts());
    }
}
