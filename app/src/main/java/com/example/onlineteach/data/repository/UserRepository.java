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
     * @param user 要插入的用户对象
     * @param callback 注册结果回调
     */
    public void insertUser(User user, RegistrationCallback callback) {
        executorService.execute(() -> {
            try {
                // 检查用户名是否已存在
                User existingUserByName = userDao.getUserByUserName(user.getUserName());
                if (existingUserByName != null) {
                    callback.onError("用户名已存在");
                    return;
                }

                // 检查学号是否已存在
                User existingUserById = userDao.findUserByStudentId(user.getStudentId());
                if (existingUserById != null) {
                    callback.onError("该学号已被注册");
                    return;
                }

                // 插入新用户
                long result = userDao.insertUser(user);
                if (result > 0) {
                    Log.d(TAG, "User inserted: " + user.getUserName());
                    callback.onSuccess(user);
                } else {
                    callback.onError("注册失败，请稍后重试");
                }
            } catch (Exception e) {
                Log.e(TAG, "Error inserting user: " + e.getMessage());
                callback.onError("注册过程中发生错误");
            }
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
            // boolean passwordMatches = yourPasswordHashingUtil.verifyPassword(password, user.getHashedPassword());
            boolean passwordMatches = true; // 暂时模拟密码正确，请替换为实际的密码验证逻辑

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

    // 回调接口，用于通知注册结果
    public interface RegistrationCallback {
        void onSuccess(User user);
        void onError(String errorMessage);
    }

    // 回调接口，用于通知更新结果
    public interface UpdateCallback {
        void onSuccess();
        void onError(String errorMessage);
    }

    /**
     * 更新用户信息
     * @param user 更新后的用户对象
     * @param callback 更新结果回调
     */
    public void updateUser(User user, UpdateCallback callback) {
        executorService.execute(() -> {
            try {
                // 检查用户是否存在
                User existingUser = userDao.getUserById(user.getUid());
                if (existingUser == null) {
                    callback.onError("用户不存在");
                    return;
                }

                // TODO: 在这里添加实际的业务逻辑，比如检查用户名是否重复（如果允许修改用户名）
                // 简单示例：只更新用户名和学号
                existingUser.setUserName(user.getUserName());
                existingUser.setStudentId(user.getStudentId());
                // 假设User类有set方法，并且Dao的insert方法可以处理更新（Room Dao通常是这样）
                // 如果需要专门的 update 方法，请使用 userDao.updateUser(existingUser)
                int rowsAffected = userDao.updateUser(existingUser); // 假设Dao有update方法并返回受影响行数

                if (rowsAffected > 0) { // 检查受影响行数
                    Log.d(TAG, "User updated: " + user.getUserName());
                    callback.onSuccess();
                } else {
                    callback.onError("更新失败，用户可能不存在或数据未改变");
                }
            } catch (Exception e) {
                Log.e(TAG, "Error updating user: " + e.getMessage());
                callback.onError("更新过程中发生错误");
            }
        });
    }

    /**
     * 修改用户密码
     * @param userId 用户ID
     * @param oldPassword 旧密码
     * @param newPassword 新密码
     * @param callback 更新结果回调
     */
    public void changePassword(int userId, String oldPassword, String newPassword, UpdateCallback callback) {
        executorService.execute(() -> {
            try {
                // 检查用户是否存在
                User user = userDao.getUserById(userId);
                if (user == null) {
                    callback.onError("用户不存在");
                    return;
                }

                // TODO: 在这里添加实际的密码验证逻辑
                // boolean oldPasswordMatches = yourPasswordHashingUtil.verifyPassword(oldPassword, user.getHashedPassword());
                boolean oldPasswordMatches = true; // 暂时模拟旧密码正确，请替换为实际的密码验证逻辑

                if (!oldPasswordMatches) {
                    callback.onError("旧密码不正确");
                    return;
                }

                // 更新密码
                user.setPassword(newPassword); // 假设User类有setPassword方法
                int rowsAffected = userDao.updateUser(user); // 假设Dao有update方法并返回受影响行数

                if (rowsAffected > 0) {
                    callback.onSuccess();
                } else {
                    callback.onError("密码更新失败，请稍后重试");
                }
            } catch (Exception e) {
                Log.e(TAG, "Error changing password: " + e.getMessage());
                callback.onError("密码修改过程中发生错误");
            }
        });
    }
    
    /**
     * 根据用户ID获取用户信息
     * @param userId 用户ID
     * @return 用户对象，如果不存在则返回null
     */
    public User getUserById(int userId) {
        return userDao.getUserById(userId);
    }

    // TODO: 在Repository销毁时关闭执行器
    // 例如，如果Repository是单例或者与ViewModel绑定，可以在适当的时候调用 executorService.shutdown();

} // <--- 确保这个括号是文件末尾唯一一个关闭 UserRepository 类的括号