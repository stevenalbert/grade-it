package io.github.stevenalbert.gradeit.dao;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import java.util.List;

import io.github.stevenalbert.gradeit.model.AnswerKey;
import io.github.stevenalbert.gradeit.model.AnswerKeyCode;

/**
 * Created by Steven Albert on 7/6/2018.
 */
@Dao
public interface AnswerKeyDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    long insert(AnswerKey answerKey);
    @Update(onConflict = OnConflictStrategy.IGNORE)
    int update(AnswerKey answerKey);
    @Delete
    int delete(AnswerKey answerKey);
    @Query("DELETE FROM answer_key")
    void deleteAll();
    @Query("SELECT m_code FROM answer_key")
    LiveData<List<AnswerKeyCode>> getAllAnswerKeysMetadata();
    @Query("SELECT * FROM answer_key")
    LiveData<List<AnswerKey>> getAllAnswerKeys();
    @Query("SELECT * FROM answer_key WHERE m_code = :mCode")
    LiveData<AnswerKey> getAnswerKey(int mCode);
}
