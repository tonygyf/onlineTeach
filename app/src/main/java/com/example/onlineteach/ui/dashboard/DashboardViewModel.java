package com.example.onlineteach.ui.dashboard;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.example.onlineteach.data.model.Enrollment;
import com.example.onlineteach.data.repository.EnrollmentRepository;

import java.util.List;

public class DashboardViewModel extends AndroidViewModel {
    private EnrollmentRepository enrollmentRepository;
    private LiveData<List<Enrollment>> enrolledCourses;

    public DashboardViewModel(Application application) {
        super(application);
        enrollmentRepository = new EnrollmentRepository(application);
        // 假设当前用户ID为1，实际应从用户会话获取
        enrolledCourses = enrollmentRepository.getEnrollmentsByUserId(1);
    }

    public LiveData<List<Enrollment>> getEnrolledCourses() {
        return enrolledCourses;
    }
}