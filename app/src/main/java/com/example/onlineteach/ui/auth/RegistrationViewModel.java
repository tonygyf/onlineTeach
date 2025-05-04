package com.example.onlineteach.ui.auth;

import android.app.Application;
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.onlineteach.data.model.User;
import com.example.onlineteach.data.repository.UserRepository;

public class RegistrationViewModel extends AndroidViewModel {

    private static final String TAG = "RegistrationViewModel";
    private UserRepository userRepository;

    private MutableLiveData<RegistrationResult> _registrationResult = new MutableLiveData<>();
    public LiveData<RegistrationResult> getRegistrationResult() {
        return _registrationResult;
    }

    private MutableLiveData<Boolean> _isLoading = new MutableLiveData<>(false);
    public LiveData<Boolean> getIsLoading() {
        return _isLoading;
    }

    public RegistrationViewModel(@NonNull Application application) {
        super(application);
        userRepository = new UserRepository(application.getApplicationContext());
    }

    public void registerUser(String username, String studentId, String password) {
        if (username.isEmpty() || studentId.isEmpty() || password.isEmpty()) {
            _registrationResult.setValue(new RegistrationResult(false, "用户名、学号或密码不能为空"));
            return;
        }

        _isLoading.setValue(true);

        final Handler handler = new Handler(Looper.getMainLooper());
        final long startTime = System.currentTimeMillis();
        final long minDisplayTime = 2000; // 最小显示时间 (2秒)

        User user = new User(username, studentId, password);

        userRepository.insertUser(user, new UserRepository.RegistrationCallback() {
            @Override
            public void onSuccess(User user) {
                long elapsedTime = System.currentTimeMillis() - startTime;
                long delay = Math.max(0, minDisplayTime - elapsedTime);

                handler.postDelayed(() -> {
                    _isLoading.setValue(false);
                    _registrationResult.postValue(new RegistrationResult(true, "注册成功！"));
                }, delay);
            }

            @Override
            public void onError(String errorMessage) {
                long elapsedTime = System.currentTimeMillis() - startTime;
                long delay = Math.max(0, minDisplayTime - elapsedTime);

                handler.postDelayed(() -> {
                    _isLoading.setValue(false);
                    _registrationResult.postValue(new RegistrationResult(false, errorMessage));
                }, delay);
            }
        });
    }

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