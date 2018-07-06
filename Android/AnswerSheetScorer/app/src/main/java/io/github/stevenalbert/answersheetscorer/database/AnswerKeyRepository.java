package io.github.stevenalbert.answersheetscorer.database;

import android.app.Application;
import android.arch.lifecycle.LiveData;
import android.os.AsyncTask;

import java.util.List;

import io.github.stevenalbert.answersheetscorer.dao.AnswerKeyDao;
import io.github.stevenalbert.answersheetscorer.model.AnswerKey;

/**
 * Created by Steven Albert on 7/6/2018.
 */
public class AnswerKeyRepository {
    public static final int NO_SPECIFIC_MCODE = -1;

    private AnswerKeyDao answerKeyDao;
    private LiveData<List<AnswerKey>> answerSheets;
    private int currentMCode;

    public AnswerKeyRepository(Application application) {
        AppDatabase database = AppDatabase.getInstance(application);
        answerKeyDao = database.answerKeyDao();
        currentMCode = -2;
    }

    public LiveData<List<AnswerKey>> getAnswerKeys(int mCode) {
        if(currentMCode != mCode) {
            answerSheets = (mCode == NO_SPECIFIC_MCODE ?
                    answerKeyDao.getAllAnswerKeys() : answerKeyDao.getAnswerKey(mCode));
        }
        return answerSheets;
    }

    public void insert(AnswerKey answerKey) {
        new InsertAsyncTask(answerKeyDao).execute(answerKey);
    }

    private static class InsertAsyncTask extends AsyncTask<AnswerKey, Void, Void> {

        private AnswerKeyDao answerKeyDao;

        public InsertAsyncTask(AnswerKeyDao answerKeyDao) {
            this.answerKeyDao = answerKeyDao;
        }

        @Override
        protected Void doInBackground(AnswerKey... answerKeys) {
            answerKeyDao.insert(answerKeys[0]);
            return null;
        }
    }

}
