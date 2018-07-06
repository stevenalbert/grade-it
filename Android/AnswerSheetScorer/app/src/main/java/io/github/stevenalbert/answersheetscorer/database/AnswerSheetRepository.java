package io.github.stevenalbert.answersheetscorer.database;

import android.app.Application;
import android.arch.lifecycle.LiveData;
import android.os.AsyncTask;

import java.util.List;

import io.github.stevenalbert.answersheetscorer.dao.AnswerSheetDao;
import io.github.stevenalbert.answersheetscorer.model.AnswerSheet;

/**
 * Created by Steven Albert on 7/5/2018.
 */
public class AnswerSheetRepository {

    private AnswerSheetDao answerSheetDao;
    private LiveData<List<AnswerSheet>> answerSheets;

    public AnswerSheetRepository(Application application) {
        AppDatabase database = AppDatabase.getInstance(application);
        answerSheetDao = database.answerSheetDao();
        answerSheets = answerSheetDao.getAllAnswerSheets();
    }

    public LiveData<List<AnswerSheet>> getAnswerSheets() {
        return answerSheets;
    }

    public void insert(AnswerSheet answerSheet) {
        new InsertAsyncTask(answerSheetDao).execute(answerSheet);
    }

    private static class InsertAsyncTask extends AsyncTask<AnswerSheet, Void, Void> {

        private AnswerSheetDao answerSheetDao;

        public InsertAsyncTask(AnswerSheetDao answerSheetDao) {
            this.answerSheetDao = answerSheetDao;
        }

        @Override
        protected Void doInBackground(AnswerSheet... answerSheets) {
            answerSheetDao.insert(answerSheets[0]);
            return null;
        }
    }
}
