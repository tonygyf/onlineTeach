package com.example.onlineteach.data.database;

import androidx.room.Database;
import androidx.room.RoomDatabase;
import android.content.Context; // Keep Context import

import com.example.onlineteach.data.dao.UserDao;
import com.example.onlineteach.data.model.User;
import com.example.onlineteach.data.dao.CourseDao;   // <-- Import CourseDao
import com.example.onlineteach.data.model.Course;  // <-- Import Course entity

// Add Course.class to entities array and increment version if needed
@Database(entities = {User.class, Course.class}, version = 1, exportSchema = false) // <-- Add Course.class
public abstract class AppDatabase extends RoomDatabase {

    public abstract UserDao userDao();
    public abstract CourseDao courseDao(); // <-- Add abstract method for CourseDao

    private static volatile AppDatabase INSTANCE;

    public static AppDatabase getDatabase(final Context context) { // Use Context
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = androidx.room.Room.databaseBuilder(
                                    context.getApplicationContext(),
                                    AppDatabase.class,
                                    "app_database"
                            )
                            // If you change the schema (like adding Course), you might need
                            // to handle migrations or use fallbackToDestructiveMigration()
                            // for development.
                            // .fallbackToDestructiveMigration() // <-- Add this during development if needed
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}