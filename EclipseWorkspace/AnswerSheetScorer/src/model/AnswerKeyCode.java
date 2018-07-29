package model;

/**
 * Created by Steven Albert on 7/16/2018.
 */
public class AnswerKeyCode {
    public int mCode;

    public AnswerKeyCode(int mCode) {
        this.mCode = mCode;
    }

    public String getMCodeString() {
        StringBuilder mCodeTextBuilder = new StringBuilder("000");
        String mCodeTextString = Integer.toString(mCode);
        mCodeTextBuilder.replace(mCodeTextBuilder.length() - mCodeTextString.length(), mCodeTextBuilder.length(), mCodeTextString);
        return mCodeTextBuilder.toString();
    }
}
