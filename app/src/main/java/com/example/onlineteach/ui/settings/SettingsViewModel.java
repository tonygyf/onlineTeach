package com.example.onlineteach.ui.settings;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import androidx.appcompat.app.AppCompatDelegate;

public class SettingsViewModel extends ViewModel {
    private final MutableLiveData<Boolean> isDarkMode;
    private final MutableLiveData<Boolean> isLightMode;

    public SettingsViewModel() {
        isDarkMode = new MutableLiveData<>();
        isLightMode = new MutableLiveData<>();
        // 获取当前主题模式
        int currentMode = AppCompatDelegate.getDefaultNightMode();
        boolean isDark = currentMode == AppCompatDelegate.MODE_NIGHT_YES;
        boolean isLight = currentMode == AppCompatDelegate.MODE_NIGHT_NO;
        isDarkMode.setValue(isDark);
        isLightMode.setValue(isLight);
    }

    public LiveData<Boolean> getIsDarkMode() {
        return isDarkMode;
    }

    public LiveData<Boolean> getIsLightMode() {
        return isLightMode;
    }

    public void setDarkMode(boolean darkMode) {
        isDarkMode.setValue(darkMode);
        isLightMode.setValue(!darkMode);
        // 设置应用主题
        AppCompatDelegate.setDefaultNightMode(
            darkMode ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO
        );
    }

    public void setLightMode(boolean lightMode) {
        isLightMode.setValue(lightMode);
        isDarkMode.setValue(!lightMode);
        // 设置应用主题
        AppCompatDelegate.setDefaultNightMode(
            lightMode ? AppCompatDelegate.MODE_NIGHT_NO : AppCompatDelegate.MODE_NIGHT_YES
        );
    }
}