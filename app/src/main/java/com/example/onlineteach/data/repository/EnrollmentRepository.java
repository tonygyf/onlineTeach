package com.example.onlineteach.data.repository;

import android.content.Context;

import androidx.lifecycle.LiveData;

import com.example.onlineteach.data.dao.EnrollmentDao;
import com.example.onlineteach.data.database.AppDatabase;
import com.example.onlineteach.data.model.Enrollment;

import java.util.List;

public class EnrollmentRepository {
    private EnrollmentDao enrollmentDao;

    public EnrollmentRepository(Context context) {
        AppDatabase db = AppDatabase.getDatabase(context);
        enrollmentDao = db.enrollmentDao();
    }

    public void enrollCourse(int userId, int courseId) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            if (!enrollmentDao.isEnrolled(userId, courseId)) {
                Enrollment enrollment = new Enrollment();
                enrollment.setUserId(userId);
                enrollment.setCourseId(courseId);
                enrollment.setEnrollmentDate(System.currentTimeMillis());
                enrollmentDao.insert(enrollment);
            }
        });
    }

    public LiveData<List<Enrollment>> getEnrollmentsByUserId(int userId) {
        return enrollmentDao.getEnrollmentsByUserId(userId);
    }

    public LiveData<List<Enrollment>> getEnrollmentsByCourseId(int courseId) {
        return enrollmentDao.getEnrollmentsByCourseId(courseId);
    }

    public boolean isEnrolled(int userId, int courseId) {
        try {
            return AppDatabase.databaseWriteExecutor.submit(() -> 
                enrollmentDao.isEnrolled(userId, courseId)
            ).get();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}