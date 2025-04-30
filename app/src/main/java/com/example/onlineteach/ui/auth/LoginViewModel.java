package com.example.onlineteach.ui.auth;

import android.app.Application;
import androidx.lifecycle.AndroidViewModel; // 导入 AndroidViewModel
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.annotation.NonNull;
import android.util.Log; // 导入 Log

import com.example.onlineteach.data.model.User; // 导入 User
import com.example.onlineteach.data.repository.UserRepository; // 导入 UserRepository

public class LoginViewModel extends AndroidViewModel {

    private static final String TAG = "LoginViewModel";

    private UserRepository userRepository; // 引用 UserRepository

    private MutableLiveData<LoginResult> _loginResult = new MutableLiveData<>();
    public LiveData<LoginResult> getLoginResult() {
        return _loginResult;
    }

    // AndroidViewModel 的构造函数需要 Application
    public LoginViewModel(@NonNull Application application) {
        super(application);
        // 初始化 UserRepository
        userRepository = new UserRepository(application.getApplicationContext());
    }

    /**
     * 处理用户登录逻辑，调用 UserRepository 进行验证和保存状态
     * @param username 用户输入的用户名
     * @param password 用户输入的密码
     */
    public void loginUser(String username, String password) {
        if (username.isEmpty() || password.isEmpty()) {
            _loginResult.setValue(new LoginResult(false, "用户名或密码不能为空"));
            return;
        }

        // 调用 UserRepository 的 loginUser 方法，并提供回调
        userRepository.loginUser(username, password, new UserRepository.LoginCallback() {
            @Override
            public void onSuccess(User user) {
                // 登录成功，通过 LiveData 通知 UI
                _loginResult.postValue(new LoginResult(true, "登录成功！欢迎 " + user.getUserName()));
            }

            @Override
            public void onError(String errorMessage) {
                // 登录失败，通过 LiveData 通知 UI
                _loginResult.postValue(new LoginResult(false, errorMessage));
            }
        });
    }

    // LoginResult 内部类保持不变
    public static class LoginResult {
        private boolean success;
        private String message;

        public LoginResult(boolean success, String message) {
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

    @Override
    protected void onCleared() {
        super.onCleared();
        // ViewModel 被销毁时，如果 UserRepository 需要清理资源，可以在这里调用
        // 例如 userRepository.shutdownExecutor(); 如果你在 UserRepository 中暴露了这样的方法
    }
}