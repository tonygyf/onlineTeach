package com.example.onlineteach.data.dao; // 建议放在 data.dao 包下

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.OnConflictStrategy;

import com.example.onlineteach.data.model.User; // 导入 User 实体类

import java.util.List;

// 使用 @Dao 注解标记这是一个 Room DAO
@Dao
public interface UserDao {

    // 插入单个用户，发生冲突时替换现有用户
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insertUser(User user); // 将返回类型从 void 修改为 long

    // 查询所有用户
    @Query("SELECT * FROM users") // SQL 查询语句
    List<User> getAllUsers();

    // 根据 uid 查询用户
    @Query("SELECT * FROM users WHERE uid = :userId LIMIT 1")
    User getUserById(int userId);

    // 根据学号和密码验证用户
    @Query("SELECT * FROM users WHERE student_id = :studentId AND password = :password LIMIT 1")
    User validateUser(String studentId, String password);

    // 根据学号查询用户是否存在
    @Query("SELECT * FROM users WHERE student_id = :studentId LIMIT 1")
    User findUserByStudentId(String studentId);

    // **** 添加根据用户名查询用户的方法 ****
    @Query("SELECT * FROM users WHERE user_name = :userName LIMIT 1")
    User getUserByUserName(String userName);

}