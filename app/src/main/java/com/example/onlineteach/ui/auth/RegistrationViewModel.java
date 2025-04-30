package com.example.onlineteach.ui.auth; // 建议放在 auth 包下

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.onlineteach.data.model.User; // 导入 User 实体类
import com.example.onlineteach.data.dao.UserDao; // 导入 UserDao 接口

public class RegistrationViewModel extends ViewModel {

    // LiveData 用于通知 UI 注册结果
    private MutableLiveData<RegistrationResult> _registrationResult = new MutableLiveData<>();
    public LiveData<RegistrationResult> getRegistrationResult() {
        return _registrationResult;
    }

    // 使用 UserDao 来处理数据存储
    private UserDao userDao;

    public RegistrationViewModel(UserDao userDao) {
        this.userDao = userDao;
    }

    /**
     * 处理用户注册逻辑
     * @param username 用户输入的用户名
     * @param studentId 用户输入的学号
     * @param password 用户输入的密码（用于验证，不存储）
     */
    public void registerUser(String username, String studentId, String password) {
        // 在实际应用中，这里会进行更严格的输入验证、与后端或本地数据库交互等

        if (username.isEmpty() || studentId.isEmpty() || password.isEmpty()) {
            _registrationResult.setValue(new RegistrationResult(false, "用户名、学号或密码不能为空"));
            return;
        }

        try {
            // 检查学号是否已存在
            User existingUser = userDao.findUserByStudentId(studentId);
            if (existingUser != null) {
                _registrationResult.setValue(new RegistrationResult(false, "该学号已被注册"));
                return;
            }

            // 创建新用户并保存到数据库
            User newUser = new User(username, studentId, password);
            userDao.insertUser(newUser);
            
            _registrationResult.setValue(new RegistrationResult(true, "注册成功！"));
        } catch (Exception e) {
            _registrationResult.setValue(new RegistrationResult(false, "注册失败：" + e.getMessage()));
        }
    }

    // 模拟注册成功与否的方法
    private boolean simulateRegistrationSuccess(String username, String studentId) {
        // 在实际应用中，这里会检查用户名和学号是否已存在，并尝试保存到数据库或调用API
        // 为了演示，我们简单模拟一下
        return !username.equalsIgnoreCase("existing_user") && !studentId.equals("12345678"); // 假设"existing_user"和"12345678"是已存在的用户名和学号
    }

    // 一个内部类，用于封装注册结果
    public static class RegistrationResult {
        private boolean success;
        private String message;

        public RegistrationResult(boolean success, String message) {
            this.success = success;
            this.message = message;
        }

        public boolean isSuccess() {
            return success;
        }

        public String getMessage() {
            return message;
        }
    }
}