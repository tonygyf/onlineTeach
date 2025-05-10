package com.example.onlineteach.ui.memo;

import android.app.Application;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.onlineteach.data.model.Memo;
import com.example.onlineteach.data.repository.MemoRepository;

import java.util.List;

public class MemoListViewModel extends AndroidViewModel {
    private MemoRepository memoRepository;
    private LiveData<List<Memo>> allMemos;
    private LiveData<List<Memo>> uncompletedMemos;
    private LiveData<Integer> uncompletedMemoCount;
    private MutableLiveData<Memo> selectedMemo = new MutableLiveData<>();

    public MemoListViewModel(Application application) {
        super(application);
        memoRepository = new MemoRepository(application);
        allMemos = memoRepository.getAllMemos();
        uncompletedMemos = memoRepository.getUncompletedMemos();
        uncompletedMemoCount = memoRepository.getUncompletedMemoCount();
    }

    public LiveData<List<Memo>> getAllMemos() {
        return allMemos;
    }

    public LiveData<List<Memo>> getUncompletedMemos() {
        return uncompletedMemos;
    }

    public LiveData<Integer> getUncompletedMemoCount() {
        return uncompletedMemoCount;
    }

    public void selectMemo(Memo memo) {
        selectedMemo.setValue(memo);
    }

    public LiveData<Memo> getSelectedMemo() {
        return selectedMemo;
    }
    
    public LiveData<Memo> getMemoById(int memoId) {
        return memoRepository.getMemoById(memoId);
    }

    public void addMemo(Memo memo) {
        memoRepository.insert(memo);
    }

    public void updateMemo(Memo memo) {
        memoRepository.update(memo);
    }

    public void deleteMemo(Memo memo) {
        memoRepository.delete(memo);
    }

    public void toggleMemoComplete(Memo memo) {
        memo.setCompleted(!memo.isCompleted());
        memoRepository.update(memo);
    }
}