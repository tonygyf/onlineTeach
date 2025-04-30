package com.example.onlineteach.data.repository;

import android.app.Application; // Use Application context
import androidx.lifecycle.LiveData;
import android.util.Log;

import com.example.onlineteach.data.dao.CourseDao;
import com.example.onlineteach.data.database.AppDatabase;
import com.example.onlineteach.data.model.Course;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CourseRepository {

    private static final String TAG = "CourseRepository";
    private CourseDao courseDao;
    private LiveData<List<Course>> allCourses;
    private ExecutorService executorService;

    // Constructor requires Application context to get database instance
    public CourseRepository(Application application) {
        AppDatabase db = AppDatabase.getDatabase(application);
        courseDao = db.courseDao(); // Get CourseDao from AppDatabase
        allCourses = courseDao.getAllCourses(); // Initialize LiveData stream
        executorService = Executors.newSingleThreadExecutor(); // For background tasks
    }

    /**
     * Returns a LiveData list of all courses from the database.
     * Room executes this query on a background thread automatically for LiveData.
     *
     * @return LiveData<List<Course>> observed by the ViewModel.
     */
    public LiveData<List<Course>> getAllCourses() {
        return allCourses;
    }

    /**
     * Inserts a single course into the database on a background thread.
     *
     * @param course The course to insert.
     */
    public void insertCourse(Course course) {
        executorService.execute(() -> {
            try {
                courseDao.insertCourse(course);
                Log.d(TAG, "Course inserted: " + course.getTitle());
            } catch (Exception e) {
                Log.e(TAG, "Error inserting course: " + e.getMessage());
            }
        });
    }

    /**
     * Inserts multiple courses into the database on a background thread.
     *
     * @param courses The list of courses to insert.
     */
    public void insertMultipleCourses(List<Course> courses) {
        executorService.execute(() -> {
            try {
                courseDao.insertAll(courses);
                Log.d(TAG, courses.size() + " courses inserted.");
            } catch (Exception e) {
                Log.e(TAG, "Error inserting multiple courses: " + e.getMessage());
            }
        });
    }

    /**
     * Example: Gets a specific course by ID directly (use with caution on main thread).
     * Consider using LiveData or running this in a background task.
     *
     * @param courseId The ID of the course to retrieve.
     * @param callback Callback to return the result.
     */
    public void getCourseById(int courseId, CourseCallback callback) {
        executorService.execute(() -> {
            Course course = courseDao.getCourseById(courseId);
            if (course != null) {
                callback.onCourseLoaded(course);
            } else {
                Log.w(TAG, "Course not found with ID: " + courseId);
                callback.onCourseNotFound();
            }
        });
    }

    /**
     * Returns a LiveData object for a single course by ID.
     *
     * @param courseId The ID of the course.
     * @return LiveData<Course>
     */
    public LiveData<Course> getCourseByIdLiveData(int courseId) {
        return courseDao.getCourseByIdLiveData(courseId);
    }


    // Optional: Add methods for update, delete etc. as needed

    // Callback for single course retrieval
    public interface CourseCallback {
        void onCourseLoaded(Course course);
        void onCourseNotFound();
    }

    // Consider shutting down the executorService when the repository is no longer needed,
    // typically managed via ViewModel's onCleared() or Application lifecycle.
    public void shutdownExecutor() {
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdown();
            Log.d(TAG, "ExecutorService shut down.");
        }
    }
}