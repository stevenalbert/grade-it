package io.github.stevenalbert.answersheetscorer.database;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.content.Context;

import io.github.stevenalbert.answersheetscorer.dao.AnswerKeyDao;
import io.github.stevenalbert.answersheetscorer.dao.AnswerSheetDao;
import io.github.stevenalbert.answersheetscorer.model.AnswerKey;
import io.github.stevenalbert.answersheetscorer.model.AnswerSheet;

/**
 * Created by Steven Albert on 7/4/2018.
 */
@Database(entities = {AnswerSheet.class, AnswerKey.class}, version = 1, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {
    private static AppDatabase instance;
    private static final String DB_NAME = "answersheetscorer.db";

    public abstract AnswerSheetDao answerSheetDao();
    public abstract AnswerKeyDao answerKeyDao();

    public static AppDatabase getInstance(final Context context) {
        if(instance == null) {
            synchronized (AppDatabase.class) {
                if(instance == null) {
                    instance = Room.databaseBuilder(context, AppDatabase.class, DB_NAME)
/*
                            .addCallback(new Callback() {
                                @Override
                                public void onCreate(@NonNull SupportSQLiteDatabase db) {
                                    super.onCreate(db);
                                }
                            })
*/
                            .fallbackToDestructiveMigration()
                            .build();
                }
            }
        }
        return instance;
    }
}
