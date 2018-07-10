package io.github.stevenalbert.answersheetscorer.viewmodel;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.support.annotation.NonNull;

import java.util.List;

import io.github.stevenalbert.answersheetscorer.database.AnswerKeyRepository;
import io.github.stevenalbert.answersheetscorer.model.AnswerKey;

/**
 * Created by Steven Albert on 7/6/2018.
 */
public class AnswerKeyViewModel extends AndroidViewModel {

    private AnswerKeyRepository repository;
    private LiveData<List<AnswerKey>> answerKeys;

    public AnswerKeyViewModel(@NonNull Application application) {
        super(application);
        repository = new AnswerKeyRepository(application);
    }

    public LiveData<List<AnswerKey>> getAnswerKeys() {
        answerKeys = repository.getAnswerKeys(AnswerKeyRepository.NO_SPECIFIC_MCODE);
        return answerKeys;
    }

    public LiveData<List<AnswerKey>> getAnswerKeyByMCode(int mCode) {
        answerKeys = repository.getAnswerKeys(mCode);
        return answerKeys;
    }

    public void insert(AnswerKey answerKey) {
        repository.insert(answerKey);
    }

    public void delete(AnswerKey answerKey) {
        repository.delete(answerKey);
    }

    public void deleteAll() {
        repository.deleteAll();
    }
}
