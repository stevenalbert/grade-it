package io.github.stevenalbert.gradeit.model;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Ignore;
import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Steven Albert on 7/16/2018.
 */
public class AnswerKeyCode implements Parcelable {
    @ColumnInfo(name = "m_code")
    public int mCode;
    @ColumnInfo(name = "total_number")
    public int totalNumber;

    @Ignore
    public AnswerKeyCode(int mCode) {
        this.mCode = mCode;
        this.totalNumber = 0;
    }

    public AnswerKeyCode(int mCode, int totalNumber) {
        this.mCode = mCode;
        this.totalNumber = totalNumber;
    }

    protected AnswerKeyCode(Parcel in) {
        mCode = in.readInt();
        totalNumber = in.readInt();
    }

    public static final Creator<AnswerKeyCode> CREATOR = new Creator<AnswerKeyCode>() {
        @Override
        public AnswerKeyCode createFromParcel(Parcel in) {
            return new AnswerKeyCode(in);
        }

        @Override
        public AnswerKeyCode[] newArray(int size) {
            return new AnswerKeyCode[size];
        }
    };

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
        dest.writeInt(mCode);
        dest.writeInt(totalNumber);
    }
}
