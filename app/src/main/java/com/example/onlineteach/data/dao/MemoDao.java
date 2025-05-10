package com.example.onlineteach.data.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.example.onlineteach.data.model.Memo;

import java.util.List;

@Dao
public interface MemoDao {
    @Insert
    void insert(Memo memo);

    @Update
    void update(Memo memo);

    @Delete
    void delete(Memo memo);

    @Query("SELECT * FROM memos ORDER BY reminderTime ASC")
    LiveData<List<Memo>> getAllMemos();

    @Query("SELECT * FROM memos WHERE isCompleted = 0 ORDER BY reminderTime ASC")
    LiveData<List<Memo>> getUncompletedMemos();

    @Query("SELECT COUNT(*) FROM memos WHERE isCompleted = 0")
    LiveData<Integer> getUncompletedMemoCount();

    @Query("SELECT * FROM memos WHERE id = :memoId")
    LiveData<Memo> getMemoById(int memoId);
}