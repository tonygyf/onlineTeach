package com.example.onlineteach.data.repository;

import android.app.Application;
import androidx.lifecycle.LiveData;

import com.example.onlineteach.data.database.AppDatabase;
import com.example.onlineteach.data.dao.MemoDao;
import com.example.onlineteach.data.model.Memo;

import java.util.List;

public class MemoRepository {
    private MemoDao memoDao;
    private LiveData<List<Memo>> allMemos;
    private LiveData<List<Memo>> uncompletedMemos;
    private LiveData<Integer> uncompletedMemoCount;

    public MemoRepository(Application application) {
        AppDatabase db = AppDatabase.getDatabase(application);
        memoDao = db.memoDao();
        allMemos = memoDao.getAllMemos();
        uncompletedMemos = memoDao.getUncompletedMemos();
        uncompletedMemoCount = memoDao.getUncompletedMemoCount();
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

    public LiveData<Memo> getMemoById(int memoId) {
        return memoDao.getMemoById(memoId);
    }

    public void insert(Memo memo) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            memoDao.insert(memo);
        });
    }

    public void update(Memo memo) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            memoDao.update(memo);
        });
    }

    public void delete(Memo memo) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            memoDao.delete(memo);
        });
    }
}