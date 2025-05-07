package com.example.onlineteach.data.model;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(tableName = "enrollments",
        foreignKeys = {
            @ForeignKey(entity = User.class,
                    parentColumns = "uid",
                    childColumns = "userId",
                    onDelete = ForeignKey.CASCADE),
            @ForeignKey(entity = Course.class,
                    parentColumns = "courseId",
                    childColumns = "courseId",
                    onDelete = ForeignKey.CASCADE)
        },
        indices = {
            @Index(value = {"userId", "courseId"}, unique = true),
                @Index(value = {"courseId"})  // ✅ 新增单独索引
        })
public class Enrollment {
    @PrimaryKey(autoGenerate = true)
    private int id;

    private int userId;
    private int courseId;
    private long enrollmentDate;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public int getCourseId() {
        return courseId;
    }

    public void setCourseId(int courseId) {
        this.courseId = courseId;
    }

    public long getEnrollmentDate() {
        return enrollmentDate;
    }

    public void setEnrollmentDate(long enrollmentDate) {
        this.enrollmentDate = enrollmentDate;
    }
}