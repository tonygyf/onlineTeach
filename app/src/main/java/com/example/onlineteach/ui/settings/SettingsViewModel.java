package com.example.onlineteach.ui.settings;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import androidx.appcompat.app.AppCompatDelegate;

public class SettingsViewModel extends ViewModel {
    private final MutableLiveData<Boolean> isDarkMode;

    public SettingsViewModel() {
        isDarkMode = new MutableLiveData<>();
        // 获取当前主题模式
        boolean isDark = AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_YES;
        isDarkMode.setValue(isDark);
    }

    public LiveData<Boolean> getIsDarkMode() {
        return isDarkMode;
    }

    public void setDarkMode(boolean darkMode) {
        isDarkMode.setValue(darkMode);
        // 设置应用主题
        AppCompatDelegate.setDefaultNightMode(
            darkMode ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO
        );
    }
}