package com.example.onlineteach.ui.auth;

import android.app.Application;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.annotation.NonNull;
import android.util.Log;
import android.os.Handler;
import android.os.Looper;

import com.example.onlineteach.data.model.User;
import com.example.onlineteach.data.repository.UserRepository;

public class LoginViewModel extends AndroidViewModel {

    private static final String TAG = "LoginViewModel";

    private UserRepository userRepository;

    private MutableLiveData<LoginResult> _loginResult = new MutableLiveData<>();
    public LiveData<LoginResult> getLoginResult() {
        return _loginResult;
    }
    private MutableLiveData<Boolean> _isLoading = new MutableLiveData<>(false);
    public LiveData<Boolean> getIsLoading() {
        return _isLoading;
    }

    public LoginViewModel(@NonNull Application application) {
        super(application);
        userRepository = new UserRepository(application.getApplicationContext());
    }

    public void loginUser(String username, String password) {
        if (username.isEmpty() || password.isEmpty()) {
            _loginResult.setValue(new LoginResult(false, "用户名或密码不能为空"));
            return;
        }

        _isLoading.setValue(true);

        final Handler handler = new Handler(Looper.getMainLooper());
        final long startTime = System.currentTimeMillis();
        final long minDisplayTime = 2000; // 最小显示时间 (2秒)

        userRepository.loginUser(username, password, new UserRepository.LoginCallback() {
            @Override
            public void onSuccess(User user) {
                long elapsedTime = System.currentTimeMillis() - startTime;
                long delay = Math.max(0, minDisplayTime - elapsedTime);

                handler.postDelayed(() -> {
                    _isLoading.setValue(false);
                    _loginResult.postValue(new LoginResult(true, "登录成功！欢迎 " + user.getUserName()));
                }, delay);
            }

            @Override
            public void onError(String errorMessage) {
                long elapsedTime = System.currentTimeMillis() - startTime;
                long delay = Math.max(0, minDisplayTime - elapsedTime);

                handler.postDelayed(() -> {
                    _isLoading.setValue(false);
                    _loginResult.postValue(new LoginResult(false, errorMessage));
                }, delay);
            }
        });
    }

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
    }
}