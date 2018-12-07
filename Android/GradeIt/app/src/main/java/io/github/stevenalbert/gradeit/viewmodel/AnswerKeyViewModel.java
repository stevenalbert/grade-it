package io.github.stevenalbert.gradeit.viewmodel;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.support.annotation.NonNull;

import java.util.List;

import io.github.stevenalbert.gradeit.database.AnswerKeyRepository;
import io.github.stevenalbert.gradeit.model.AnswerKey;
import io.github.stevenalbert.gradeit.model.AnswerKeyCode;

/**
 * Created by Steven Albert on 7/6/2018.
 */
public class AnswerKeyViewModel extends AndroidViewModel {

    private AnswerKeyRepository repository;
    private LiveData<List<AnswerKeyCode>> answerKeysMetadata;
    private LiveData<List<AnswerKey>> answerKeys;
    private LiveData<AnswerKey> answerKeyByMCode;

    public AnswerKeyViewModel(@NonNull Application application) {
        super(application);
        repository = new AnswerKeyRepository(application);
    }

    public LiveData<List<AnswerKeyCode>> getAnswerKeysMetadata() {
        answerKeysMetadata = repository.getAnswerKeysMetadata();
        return answerKeysMetadata;
    }

    public LiveData<List<AnswerKey>> getAnswerKeys() {
        answerKeys = repository.getAnswerKeys();
        return answerKeys;
    }

    public LiveData<AnswerKey> getAnswerKeyByMCode(int mCode) {
        answerKeyByMCode = repository.getAnswerKeyByMCode(mCode);
        return answerKeyByMCode;
    }

    public void insert(AnswerKey answerKey) {
        repository.insert(answerKey);
    }

    public void delete(AnswerKey answerKey) {
        repository.delete(answerKey);
    }

    public void deleteByMCode(Integer mCode) {
        repository.deleteByMCode(mCode);
    }

    public void deleteAll() {
        repository.deleteAll();
    }
}
