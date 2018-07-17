package io.github.stevenalbert.answersheetscorer.database;

import android.app.Application;
import android.arch.lifecycle.LiveData;
import android.os.AsyncTask;

import java.util.List;

import io.github.stevenalbert.answersheetscorer.dao.AnswerSheetDao;
import io.github.stevenalbert.answersheetscorer.model.AnswerSheet;
import io.github.stevenalbert.answersheetscorer.model.AnswerSheetCode;

/**
 * Created by Steven Albert on 7/5/2018.
 */
public class AnswerSheetRepository {

    private static final int NO_CURRENT_MCODE = -2;
    public static final int ALL_MCODE = -1;

    private AnswerSheetDao answerSheetDao;
    private LiveData<List<AnswerSheetCode>> answerSheetsMetadata;
    private LiveData<List<AnswerSheet>> answerSheets;
    private LiveData<AnswerSheet> answerSheet;
    private int currentMCode;
    private int currentAnswerSheetMCode;
    private int currentAnswerSheetExCode;
    private int metadataMCode;

    public AnswerSheetRepository(Application application) {
        AppDatabase database = AppDatabase.getInstance(application);
        answerSheetDao = database.answerSheetDao();
        currentMCode = NO_CURRENT_MCODE;
        metadataMCode = NO_CURRENT_MCODE;
    }

    public LiveData<List<AnswerSheetCode>> getAnswerSheetsMetadata(int mCode) {
        if(metadataMCode != mCode){
            answerSheetsMetadata = (mCode == ALL_MCODE ?
                    answerSheetDao.getAllAnswerSheetsMetadata() : answerSheetDao.getAllAnswerSheetsMetadataByMCode(mCode));
        }
        return answerSheetsMetadata;
    }

    public LiveData<List<AnswerSheet>> getAnswerSheets(int mCode) {
        if(currentMCode != mCode) {
            answerSheets = (mCode == ALL_MCODE ?
                    answerSheetDao.getAllAnswerSheets() : answerSheetDao.getAnswerSheetsByMCode(mCode));
            currentMCode = mCode;
        }
        return answerSheets;
    }

    public LiveData<AnswerSheet> getAnswerSheet(int exCode, int mCode) {
        if(currentAnswerSheetExCode != exCode || currentAnswerSheetMCode != mCode) {
            answerSheet = answerSheetDao.getAnswerSheet(exCode, mCode);
            currentAnswerSheetMCode = mCode;
            currentAnswerSheetExCode = exCode;
        }
        return answerSheet;
    }

    public void insert(AnswerSheet answerSheet) {
        new InsertAsyncTask(answerSheetDao).execute(answerSheet);
    }

    public void delete(AnswerSheet answerSheet) {
        new DeleteAsyncTask(answerSheetDao).execute(answerSheet);
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

    private static class DeleteAsyncTask extends AsyncTask<AnswerSheet, Void, Void> {

        private AnswerSheetDao answerSheetDao;

        private DeleteAsyncTask(AnswerSheetDao answerSheetDao) {
            this.answerSheetDao = answerSheetDao;
        }

        @Override
        protected Void doInBackground(AnswerSheet... answerSheets) {
            answerSheetDao.delete(answerSheets[0]);
            return null;
        }
    }
}
