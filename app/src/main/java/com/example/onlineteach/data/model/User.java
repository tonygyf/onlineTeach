package com.example.onlineteach.data.model;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.ColumnInfo;
import androidx.room.Ignore; // 导入 @Ignore 注解

@Entity(tableName = "users")
public class User {

    @PrimaryKey(autoGenerate = true)
    public int uid;

    @ColumnInfo(name = "user_name")
    public String userName;

    @ColumnInfo(name = "student_id")
    public String studentId;

    @ColumnInfo(name = "password")
    public String password;

    // Room 需要一个无参构造函数
    public User() {
    }

    // 使用 @Ignore 标记这个构造函数，告诉 Room 在读取数据库时不使用它
    @Ignore
    public User(String userName, String studentId, String password) {
        this.userName = userName;
        this.studentId = studentId;
        this.password = password;
    }

    // 添加带uid的构造函数
    @Ignore
    public User(int uid, String userName, String studentId) {
        this.uid = uid;
        this.userName = userName;
        this.studentId = studentId;
    }

    // 提供 getter 方法
    public int getUid() {
        return uid;
    }

    public String getUserName() {
        return userName;
    }

    public String getStudentId() {
        return studentId;
    }

    // 提供 setter 方法
    public void setUid(int uid) {
        this.uid = uid;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public void setStudentId(String studentId) {
        this.studentId = studentId;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}