package io.github.stevenalbert.gradeit.model;

import android.arch.persistence.room.ColumnInfo;
import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Steven Albert on 7/16/2018.
 */
public class AnswerSheetCode implements Parcelable {
    @ColumnInfo(name = "ex_code")
    public int exCode;
    @ColumnInfo(name = "m_code")
    public int mCode;

    public AnswerSheetCode(int exCode, int mCode) {
        this.exCode = exCode;
        this.mCode = mCode;
    }

    protected AnswerSheetCode(Parcel in) {
        exCode = in.readInt();
        mCode = in.readInt();
    }

    public static final Creator<AnswerSheetCode> CREATOR = new Creator<AnswerSheetCode>() {
        @Override
        public AnswerSheetCode createFromParcel(Parcel in) {
            return new AnswerSheetCode(in);
        }

        @Override
        public AnswerSheetCode[] newArray(int size) {
            return new AnswerSheetCode[size];
        }
    };

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

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(exCode);
        dest.writeInt(mCode);
    }
}
