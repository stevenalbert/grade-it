package io.github.stevenalbert.answersheetscorer.dao;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import java.util.List;

import io.github.stevenalbert.answersheetscorer.model.AnswerKey;

/**
 * Created by Steven Albert on 7/6/2018.
 */
@Dao
public interface AnswerKeyDao {
    @Insert
    long insert(AnswerKey answerKey);
    @Update
    long update(AnswerKey answerKey);
    @Query("SELECT * FROM answer_key")
    LiveData<List<AnswerKey>> getAllAnswerKeys();
    @Query("SELECT * FROM answer_key WHERE m_code = :mCode")
    LiveData<List<AnswerKey>> getAnswerKey(int mCode);
}
