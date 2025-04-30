package com.example.onlineteach.data.repository; // 建议放在 data.repository 包下

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.example.onlineteach.data.dao.UserDao; // 导入 UserDao
import com.example.onlineteach.data.database.AppDatabase; // 导入 AppDatabase
import com.example.onlineteach.data.model.User; // 导入 User 实体类

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class UserRepository {

    private static final String TAG = "UserRepository";
    private static final String PREFS_NAME = "UserPrefs";
    private static final String LOGGED_IN_USER_ID_KEY = "logged_in_user_id";

    private UserDao userDao;
    private SharedPreferences sharedPreferences;
    private ExecutorService executorService; // 用于在后台线程执行数据库和SharedPreferences操作

    // UserRepository 需要一个 Context 来获取数据库和SharedPreferences实例
    public UserRepository(Context context) {
        AppDatabase db = AppDatabase.getDatabase(context);
        userDao = db.userDao();
        sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        executorService = Executors.newSingleThreadExecutor();
    }

    /**
     * 在后台线程插入用户
     */
    public void insertUser(User user) {
        executorService.execute(() -> {
            userDao.insertUser(user);
            Log.d(TAG, "User inserted: " + user.getUserName());
        });
    }

    /**
     * 在后台线程验证用户并保存登录状态
     * @param username 用户输入的用户名
     * @param password 用户输入的密码
     * @param callback 回调，通知验证结果和用户对象
     */
    public void loginUser(String username, String password, LoginCallback callback) {
        if (username.isEmpty() || password.isEmpty()) {
            Log.d(TAG, "Login attempt with empty username or password."); // 添加日志
            callback.onError("用户名或密码不能为空");
            return;
        }

        executorService.execute(() -> {
            Log.d(TAG, "Executing login database query for user: " + username); // 添加日志
            // TODO: 根据用户名或学号查询用户，并验证密码
            // 使用你 UserDao 中实际的查询方法
            User user = userDao.getUserByUserName(username); // 假设通过用户名查询

            // TODO: 在这里添加实际的密码验证逻辑
            boolean passwordMatches = true; // 暂时模拟密码正确

            if (user != null && passwordMatches) {
                Log.d(TAG, "User found and password matches for user: " + username); // 添加日志
                // 登录成功，保存用户 ID 到 SharedPreferences
                saveLoggedInUserId(user.getUid());
                // 通知回调成功并返回用户对象
                callback.onSuccess(user);
                Log.d(TAG, "Login successful, onSuccess callback triggered."); // 添加日志
            } else {
                Log.d(TAG, "User not found or password mismatch for user: " + username); // 添加日志
                // 登录失败
                callback.onError("用户名或密码错误");
                Log.d(TAG, "Login failed, onError callback triggered."); // 添加日志
            }
        });
    }

    /**
     * 保存登录用户的 ID 到 SharedPreferences
     */
    private void saveLoggedInUserId(int userId) {
        sharedPreferences.edit().putInt(LOGGED_IN_USER_ID_KEY, userId).apply();
        Log.d(TAG, "Logged in user ID saved: " + userId);
    }

    /**
     * 从 SharedPreferences 获取当前登录用户的 ID
     * @return 登录用户的 ID，如果未登录返回 -1 或其他标识符
     */
    public int getLoggedInUserId() {
        return sharedPreferences.getInt(LOGGED_IN_USER_ID_KEY, -1); // 默认返回 -1 表示未登录
    }

    /**
     * 在后台线程根据 ID 获取登录用户对象
     * @param callback 回调，通知用户对象
     */
    public void getLoggedInUser(UserCallback callback) {
        executorService.execute(() -> {
            int loggedInUserId = getLoggedInUserId();
            if (loggedInUserId != -1) {
                User user = userDao.getUserById(loggedInUserId);
                if (user != null) {
                    callback.onUserLoaded(user);
                } else {
                    Log.w(TAG, "Logged in user not found in database with ID: " + loggedInUserId);
                    callback.onUserNotFound();
                }
            } else {
                callback.onUserNotFound();
            }
        });
    }

    /**
     * 用户登出，清除登录状态
     */
    public void logoutUser() {
        executorService.execute(() -> {
            sharedPreferences.edit().remove(LOGGED_IN_USER_ID_KEY).apply();
            Log.d(TAG, "User logged out.");
        });
    }


    // 回调接口，用于通知登录结果
    public interface LoginCallback {
        void onSuccess(User user);
        void onError(String errorMessage);
    }

    // 回调接口，用于通知用户加载结果
    public interface UserCallback {
        void onUserLoaded(User user);
        void onUserNotFound();
    }

    // TODO: 在Repository销毁时关闭执行器 (虽然Repository没有明确的生命周期，
    // 但如果和ViewModel的生命周期关联，可以在ViewModel的onCleared中管理)
}