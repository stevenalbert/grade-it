package io.github.stevenalbert.answersheetscorer.viewmodel;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.support.annotation.NonNull;

import java.util.List;

import io.github.stevenalbert.answersheetscorer.database.AnswerSheetRepository;
import io.github.stevenalbert.answersheetscorer.model.AnswerSheet;
import io.github.stevenalbert.answersheetscorer.model.AnswerSheetCode;

/**
 * Created by Steven Albert on 7/5/2018.
 */
public class AnswerSheetViewModel extends AndroidViewModel {

    private AnswerSheetRepository repository;
    private LiveData<List<AnswerSheetCode>> answerSheetsMetadata;
    private LiveData<List<AnswerSheet>> answerSheets;
    private LiveData<AnswerSheet> answerSheet;

    public AnswerSheetViewModel(@NonNull Application application) {
        super(application);
        repository = new AnswerSheetRepository(application);
    }

    public LiveData<List<AnswerSheetCode>> getAnswerSheetsMetadata() {
        answerSheetsMetadata = repository.getAnswerSheetsMetadata(AnswerSheetRepository.ALL_MCODE);
        return answerSheetsMetadata;
    }

    public LiveData<List<AnswerSheetCode>> getAnswerSheetsMetadataByMCode(int mCode) {
        answerSheetsMetadata = repository.getAnswerSheetsMetadata(mCode);
        return answerSheetsMetadata;
    }

    public LiveData<List<AnswerSheet>> getAnswerSheets() {
        answerSheets = repository.getAnswerSheets(AnswerSheetRepository.ALL_MCODE);
        return answerSheets;
    }

    public LiveData<List<AnswerSheet>> getAnswerSheetsByMCode(int mCode) {
        answerSheets = repository.getAnswerSheets(mCode);
        return answerSheets;
    }

    public LiveData<AnswerSheet> getAnswerSheet(int exCode, int mCode) {
        answerSheet = repository.getAnswerSheet(exCode, mCode);
        return answerSheet;
    }

    public void insert(AnswerSheet answerSheet) {
        repository.insert(answerSheet);
    }

    public void delete(AnswerSheet answerSheet) {
        repository.delete(answerSheet);
    }
}
