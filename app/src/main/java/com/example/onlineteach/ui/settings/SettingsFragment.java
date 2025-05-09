package com.example.onlineteach.ui.settings;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Switch;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.onlineteach.R;
import com.example.onlineteach.databinding.FragmentSettingsBinding;

public class SettingsFragment extends Fragment {

    private FragmentSettingsBinding binding;
    private SettingsViewModel settingsViewModel;

    public View onCreateView(@NonNull LayoutInflater inflater,
                            ViewGroup container, Bundle savedInstanceState) {
        settingsViewModel = new ViewModelProvider(this).get(SettingsViewModel.class);

        binding = FragmentSettingsBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        // 设置主题切换开关的初始状态和监听器
        settingsViewModel.getIsDarkMode().observe(getViewLifecycleOwner(), isDarkMode -> {
            binding.switchTheme.setChecked(isDarkMode);
        });

        binding.switchTheme.setOnCheckedChangeListener((buttonView, isChecked) -> {
            settingsViewModel.setDarkMode(isChecked);
        });

        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    @Override
    public void onResume() {
        super.onResume();
        // 确保在恢复到此Fragment时，ActionBar显示正确的标题
        if (getActivity() instanceof AppCompatActivity) {
            AppCompatActivity activity = (AppCompatActivity) getActivity();
            if (activity != null) {
                androidx.appcompat.app.ActionBar actionBar = activity.getSupportActionBar();
                if (actionBar != null) {
                    actionBar.setTitle(R.string.title_settings);
                    actionBar.show();
                }
            }
        }
    }
}