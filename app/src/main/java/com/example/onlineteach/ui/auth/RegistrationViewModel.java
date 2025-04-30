package com.example.onlineteach.ui.auth;

import android.app.Application;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.annotation.NonNull;

import com.example.onlineteach.data.model.User;
import com.example.onlineteach.data.repository.UserRepository;

public class RegistrationViewModel extends AndroidViewModel {

    private static final String TAG = "RegistrationViewModel";
    private UserRepository userRepository;

    // LiveData 用于通知 UI 注册结果
    private MutableLiveData<RegistrationResult> _registrationResult = new MutableLiveData<>();
    public LiveData<RegistrationResult> getRegistrationResult() {
        return _registrationResult;
    }

    public RegistrationViewModel(@NonNull Application application) {
        super(application);
        userRepository = new UserRepository(application.getApplicationContext());
    }

    /**
     * 处理用户注册逻辑
     * @param username 用户输入的用户名
     * @param studentId 用户输入的学号
     * @param password 用户输入的密码（用于验证，不存储）
     */
    public void registerUser(String username, String studentId, String password) {
        if (username.isEmpty() || studentId.isEmpty() || password.isEmpty()) {
            _registrationResult.setValue(new RegistrationResult(false, "用户名、学号或密码不能为空"));
            return;
        }
        User user =new User(username, studentId, password);

        userRepository.insertUser(user, new UserRepository.RegistrationCallback() {
            @Override
            public void onSuccess(User user) {
                _registrationResult.postValue(new RegistrationResult(true, "注册成功！"));
            }

            @Override
            public void onError(String errorMessage) {
                _registrationResult.postValue(new RegistrationResult(false, errorMessage));
            }
        });
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