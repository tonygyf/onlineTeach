package com.example.onlineteach.ui.course;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.onlineteach.data.model.Course;

import java.util.ArrayList;
import java.util.List;

public class CourseListViewModel extends ViewModel {
    private MutableLiveData<List<Course>> courses;

    public CourseListViewModel() {
        courses = new MutableLiveData<>();
        loadCourses(); // 加载课程数据
    }

    public LiveData<List<Course>> getCourses() {
        return courses;
    }

    private void loadCourses() {
        // TODO: 从数据库或网络加载实际的课程数据
        List<Course> courseList = new ArrayList<>();
        
        // 临时添加一些测试数据
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

        courses.setValue(courseList);
    }
}