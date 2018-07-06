package io.github.stevenalbert.answersheetscorer.dao;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import java.util.List;

import io.github.stevenalbert.answersheetscorer.model.AnswerSheet;

/**
 * Created by Steven Albert on 7/4/2018.
 */
@Dao
public interface AnswerSheetDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    long insert(AnswerSheet answerSheet);
    @Update(onConflict = OnConflictStrategy.IGNORE)
    int update(AnswerSheet answerSheet);
    @Delete
    int delete(AnswerSheet answerSheet);
    @Query("DELETE FROM answer_sheet")
    void deleteAll();
    @Query("SELECT * FROM answer_sheet ORDER BY m_code ASC")
    LiveData<List<AnswerSheet>> getAllAnswerSheets();
    @Query("SELECT * FROM answer_sheet WHERE m_code = :mCode")
    LiveData<List<AnswerSheet>> getAnswerSheetsByMCode(int mCode);
    @Query("SELECT * FROM answer_sheet WHERE ex_code = :exCode AND m_code = :mCode")
    LiveData<AnswerSheet> getAnswerSheet(int exCode, int mCode);
}
