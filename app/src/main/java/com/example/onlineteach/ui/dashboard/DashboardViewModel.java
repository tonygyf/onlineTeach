package com.example.onlineteach.ui.dashboard;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.example.onlineteach.data.model.Enrollment;
import com.example.onlineteach.data.repository.EnrollmentRepository;
import com.example.onlineteach.data.repository.UserRepository;

import java.util.List;

public class DashboardViewModel extends AndroidViewModel {
    private EnrollmentRepository enrollmentRepository;
    private UserRepository userRepository;
    private LiveData<List<Enrollment>> enrolledCourses;

    public DashboardViewModel(Application application) {
        super(application);
        enrollmentRepository = new EnrollmentRepository(application);
        userRepository = new UserRepository(application);
        // 使用当前登录用户ID
        int currentUserId = userRepository.getLoggedInUserId();
        enrolledCourses = enrollmentRepository.getEnrollmentsByUserId(currentUserId);
    }

    public LiveData<List<Enrollment>> getEnrolledCourses() {
        return enrolledCourses;
    }
}