package com.example.onlineteach.ui.course;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.example.onlineteach.data.model.Course;
import com.example.onlineteach.data.repository.CourseRepository;

public class CourseDetailViewModel extends AndroidViewModel {
    private final CourseRepository courseRepository;
    private LiveData<Course> course;

    public CourseDetailViewModel(Application application) {
        super(application);
        courseRepository = new CourseRepository(application);
    }

    /**
     * 根据课程ID加载课程详情
     * @param courseId 课程ID
     */
    public void loadCourse(int courseId) {
        course = courseRepository.getCourseById(courseId);
    }

    /**
     * 获取课程详情的LiveData对象
     * @return LiveData<Course> 课程详情的LiveData对象
     */
    public LiveData<Course> getCourse() {
        return course;
    }
} 