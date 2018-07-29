package model;

/**
 * Created by Steven Albert on 7/16/2018.
 */
public class AnswerSheetCode {
    public int exCode;
    public int mCode;

    public AnswerSheetCode(int exCode, int mCode) {
        this.exCode = exCode;
        this.mCode = mCode;
    }

    public String getExCodeString() {
        StringBuilder exCodeTextBuilder = new StringBuilder("000");
        String exCodeTextString = Integer.toString(exCode);
        exCodeTextBuilder.replace(exCodeTextBuilder.length() - exCodeTextString.length(), exCodeTextBuilder.length(), exCodeTextString);
        return exCodeTextBuilder.toString();
    }

    public String getMCodeString() {
        StringBuilder mCodeTextBuilder = new StringBuilder("000");
        String mCodeTextString = Integer.toString(mCode);
        mCodeTextBuilder.replace(mCodeTextBuilder.length() - mCodeTextString.length(), mCodeTextBuilder.length(), mCodeTextString);
        return mCodeTextBuilder.toString();
    }
}
