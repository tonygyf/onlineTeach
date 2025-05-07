package com.example.onlineteach.data.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import com.example.onlineteach.data.model.Enrollment;

import java.util.List;

@Dao
public interface EnrollmentDao {
    @Insert
    void insert(Enrollment enrollment);

    @Query("SELECT * FROM enrollments WHERE userId = :userId")
    LiveData<List<Enrollment>> getEnrollmentsByUserId(int userId);

    @Query("SELECT * FROM enrollments WHERE courseId = :courseId")
    LiveData<List<Enrollment>> getEnrollmentsByCourseId(int courseId);

    @Query("SELECT EXISTS(SELECT 1 FROM enrollments WHERE userId = :userId AND courseId = :courseId)")
    boolean isEnrolled(int userId, int courseId);
}