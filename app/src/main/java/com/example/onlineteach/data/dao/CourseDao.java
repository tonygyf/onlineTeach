package com.example.onlineteach.data.dao;

import androidx.lifecycle.LiveData; // Import LiveData
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.OnConflictStrategy;

import com.example.onlineteach.data.model.Course; // Import Course model

import java.util.List;

@Dao
public interface CourseDao {

    // Insert a single course, replace on conflict
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertCourse(Course course); // Use void or long as needed

    // Insert multiple courses
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<Course> courses);

    // Query all courses and return as LiveData for observing changes
    @Query("SELECT * FROM courses ORDER BY title ASC") // Order courses alphabetically by title
    LiveData<List<Course>> getAllCourses(); // Return LiveData

    // Query a course by its ID
    @Query("SELECT * FROM courses WHERE courseId = :id LIMIT 1")
    Course getCourseById(int id); // Direct return (use on background thread)

    // Query a course by its ID and return as LiveData
    @Query("SELECT * FROM courses WHERE courseId = :id LIMIT 1")
    LiveData<Course> getCourseByIdLiveData(int id); // LiveData return

    // Example: Delete all courses (optional)
    @Query("DELETE FROM courses")
    void deleteAllCourses();
}