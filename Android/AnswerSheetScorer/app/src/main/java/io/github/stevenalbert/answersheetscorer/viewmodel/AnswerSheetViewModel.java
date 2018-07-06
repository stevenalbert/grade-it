package io.github.stevenalbert.answersheetscorer.viewmodel;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.support.annotation.NonNull;

import java.util.List;

import io.github.stevenalbert.answersheetscorer.database.AnswerSheetRepository;
import io.github.stevenalbert.answersheetscorer.model.AnswerSheet;

/**
 * Created by Steven Albert on 7/5/2018.
 */
public class AnswerSheetViewModel extends AndroidViewModel {

    private AnswerSheetRepository repository;
    private LiveData<List<AnswerSheet>> answerSheets;

    public AnswerSheetViewModel(@NonNull Application application) {
        super(application);
        repository = new AnswerSheetRepository(application);
    }

    public LiveData<List<AnswerSheet>> getAnswerSheets() {
        answerSheets = repository.getAnswerSheets(AnswerSheetRepository.NO_SPECIFIC_MCODE);
        return answerSheets;
    }

    public LiveData<List<AnswerSheet>> getAnswerSheetsByMCode(int mCode) {
        answerSheets = repository.getAnswerSheets(mCode);
        return answerSheets;
    }

    public void insert(AnswerSheet answerSheet) {
        repository.insert(answerSheet);
    }
}
