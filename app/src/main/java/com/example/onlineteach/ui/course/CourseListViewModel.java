package com.example.onlineteach.ui.course;

import android.app.Application;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.onlineteach.data.model.Course;
import com.example.onlineteach.data.repository.CourseRepository;

import java.util.ArrayList;
import java.util.List;

public class CourseListViewModel extends AndroidViewModel {
    private CourseRepository courseRepository;
    private LiveData<List<Course>> courses;

    public CourseListViewModel(Application application) {
        super(application);
        courseRepository = new CourseRepository(application);
        courses = courseRepository.getAllCourses();
        
        // 如果数据库中没有数据，添加一些测试数据
        addSampleCoursesIfNeeded();
    }

    public LiveData<List<Course>> getCourses() {
        return courses;
    }

    private void addSampleCoursesIfNeeded() {
        // 添加一些测试数据到数据库
        List<Course> courseList = new ArrayList<>();
        
        Course course1 = new Course();
        course1.setTitle("大学教学的语言技能");
        course1.setCredits(1.0f);
        course1.setTeacher("教发老师");
        courseList.add(course1);

        Course course2 = new Course();
        course2.setTitle("教学准备五件事");
        course2.setCredits(1.0f);
        course2.setTeacher("教发老师");
        courseList.add(course2);

        courseRepository.insertMultipleCourses(courseList);
    }
}