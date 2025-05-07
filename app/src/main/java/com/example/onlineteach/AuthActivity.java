package com.example.onlineteach;

import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;

import com.example.onlineteach.data.dao.UserDao;
import com.example.onlineteach.data.database.AppDatabase;
import com.example.onlineteach.data.model.User;
import com.example.onlineteach.dialog.LottieLoadingDialog;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AuthActivity extends AppCompatActivity {
    // 在你的 AuthActivity.java 的 onCreate 方法中 (或者应用的某个初始化位置)
//    dialog类使用
//    private LottieLoadingDialog loadingDialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auth);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }
        // 初始化 LottieLoadingDialog
//        loadingDialog = new LottieLoadingDialog(this);

        // 在后台线程插入测试用户 (仅用于测试，实际应用应有注册功能)
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        executorService.execute(() -> {
            UserDao userDao = AppDatabase.getDatabase(getApplicationContext()).userDao();
            // 创建一个测试用户 (用户名，学号，密码)
            User testUser = new User("testuser", "12345678", "password123");
            // 检查用户是否已存在，避免重复插入
            User existingUser = userDao.getUserByUserName("testuser");
            if (existingUser == null) {
                long userId = userDao.insertUser(testUser);
                Log.d("AuthActivity", "Inserted test user with ID: " + userId);
            } else {
                Log.d("AuthActivity", "Test user already exists.");
            }
        });
        // 别忘了在 Activity 销毁时关闭 executorService
    }

}