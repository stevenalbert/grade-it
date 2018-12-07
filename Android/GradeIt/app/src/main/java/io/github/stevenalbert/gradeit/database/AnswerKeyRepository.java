package io.github.stevenalbert.gradeit.database;

import android.app.Application;
import android.arch.lifecycle.LiveData;
import android.os.AsyncTask;

import java.util.List;

import io.github.stevenalbert.gradeit.dao.AnswerKeyDao;
import io.github.stevenalbert.gradeit.model.AnswerKey;
import io.github.stevenalbert.gradeit.model.AnswerKeyCode;

/**
 * Created by Steven Albert on 7/6/2018.
 */
public class AnswerKeyRepository {

    private static final int NO_CURRENT_MCODE = -2;

    private AnswerKeyDao answerKeyDao;
    private LiveData<List<AnswerKeyCode>> answerKeysMetadata;
    private LiveData<List<AnswerKey>> answerKeys;
    private LiveData<AnswerKey> answerKey;

    private int currentMCode;

    public AnswerKeyRepository(Application application) {
        AppDatabase database = AppDatabase.getInstance(application);
        answerKeyDao = database.answerKeyDao();
        currentMCode = NO_CURRENT_MCODE;
    }

    public LiveData<List<AnswerKeyCode>> getAnswerKeysMetadata() {
        if(answerKeysMetadata == null) {
            answerKeysMetadata = answerKeyDao.getAllAnswerKeysMetadata();
        }
        return answerKeysMetadata;
    }

    public LiveData<List<AnswerKey>> getAnswerKeys() {
        if(answerKeys == null) {
            answerKeys = answerKeyDao.getAllAnswerKeys();
        }
        return answerKeys;
    }

    public LiveData<AnswerKey> getAnswerKeyByMCode(int mCode) {
        if(currentMCode != mCode) {
            answerKey = answerKeyDao.getAnswerKey(mCode);
            currentMCode = mCode;
        }
        return answerKey;
    }

    public void insert(AnswerKey answerKey) {
        new InsertAsyncTask(answerKeyDao).execute(answerKey);
    }

    public void delete(AnswerKey answerKey) {
        new DeleteAsyncTask(answerKeyDao).execute(answerKey);
    }

    public void deleteByMCode(Integer mCode) {
        new DeleteByMCodeAsyncTask(answerKeyDao).execute(mCode);
    }

    public void deleteAll() {
        new DeleteAllAsyncTask(answerKeyDao).execute();
    }

    private static class InsertAsyncTask extends AsyncTask<AnswerKey, Void, Void> {

        private AnswerKeyDao answerKeyDao;

        private InsertAsyncTask(AnswerKeyDao answerKeyDao) {
            this.answerKeyDao = answerKeyDao;
        }

        @Override
        protected Void doInBackground(AnswerKey... answerKeys) {
            answerKeyDao.insert(answerKeys[0]);
            return null;
        }
    }

    private static class DeleteAsyncTask extends AsyncTask<AnswerKey, Void, Void> {

        private AnswerKeyDao answerKeyDao;

        private DeleteAsyncTask(AnswerKeyDao answerKeyDao) {
            this.answerKeyDao = answerKeyDao;
        }

        @Override
        protected Void doInBackground(AnswerKey... answerKeys) {
            answerKeyDao.delete(answerKeys[0]);
            return null;
        }
    }

    private static class DeleteByMCodeAsyncTask extends AsyncTask<Integer, Void, Void> {

        private AnswerKeyDao answerKeyDao;

        private DeleteByMCodeAsyncTask(AnswerKeyDao answerKeyDao) {
            this.answerKeyDao = answerKeyDao;
        }

        @Override
        protected Void doInBackground(Integer... mCodes) {
            answerKeyDao.deleteByMCode(mCodes[0]);
            return null;
        }
    }

    private static class DeleteAllAsyncTask extends AsyncTask<Void, Void, Void> {

        private AnswerKeyDao answerKeyDao;

        private DeleteAllAsyncTask(AnswerKeyDao answerKeyDao) {
            this.answerKeyDao = answerKeyDao;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            answerKeyDao.deleteAll();
            return null;
        }
    }
}
