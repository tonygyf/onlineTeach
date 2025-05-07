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
    private MutableLiveData<Course> selectedCourse = new MutableLiveData<>();

    public CourseListViewModel(Application application) {
        super(application);
        courseRepository = new CourseRepository(application);
        courses = courseRepository.getAllCourses();
    }

    /**
     * 获取所有课程列表
     * @return 课程列表的LiveData对象
     */
    public LiveData<List<Course>> getCourses() {
        return courses;
    }
    
    /**
     * 根据ID获取课程详情
     * @param courseId 课程ID
     * @return 课程详情的LiveData对象
     */
    public LiveData<Course> getCourseById(int courseId) {
        return courseRepository.getCourseById(courseId);
    }
    
    /**
     * 设置当前选中的课程
     * @param course 选中的课程
     */
    public void selectCourse(Course course) {
        selectedCourse.setValue(course);
    }
    
    /**
     * 获取当前选中的课程
     * @return 选中课程的LiveData对象
     */
    public LiveData<Course> getSelectedCourse() {
        return selectedCourse;
    }
    
    /**
     * 添加新课程
     * @param course 要添加的课程
     */
    public void addCourse(Course course) {
        courseRepository.insertCourse(course);
    }
}