package com.example.onlineteach.ui.auth;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController; // 导入 NavController
import androidx.navigation.Navigation; // 导入 Navigation

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast; // 导入 Toast

import com.example.onlineteach.R;
import com.example.onlineteach.databinding.FragmentRegistrationBinding; // 导入 View Binding 生成的类

public class RegistrationFragment extends Fragment {

    private RegistrationViewModel mViewModel; // 假设你有一个 RegistrationViewModel
    private FragmentRegistrationBinding binding; // View Binding 绑定对象

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        // 使用 View Binding 膨胀布局
        binding = FragmentRegistrationBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        // 获取 ViewModel
        mViewModel = new ViewModelProvider(this).get(RegistrationViewModel.class);

        // 观察注册结果
        mViewModel.getRegistrationResult().observe(getViewLifecycleOwner(), result -> {
            if (result.isSuccess()) {
                Toast.makeText(getContext(), result.getMessage(), Toast.LENGTH_SHORT).show();
                // 注册成功后导航回登录页面
                NavController navController = Navigation.findNavController(binding.getRoot());
                navController.navigate(R.id.action_registrationFragment_to_loginFragment);
            } else {
                Toast.makeText(getContext(), result.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        // 设置注册按钮的点击事件监听器
        binding.buttonRegister.setOnClickListener(v -> {
            String username = binding.editTextUsername.getText().toString().trim();
            String studentId = binding.editTextStudentId.getText().toString().trim();
            String password = binding.editTextPassword.getText().toString().trim();

            // 调用ViewModel处理注册逻辑
            mViewModel.registerUser(username, studentId, password);
        }); // <-- Added missing parenthesis here

        // 设置“已有账号？去登录” TextView 的点击事件监听器
        binding.textViewLoginLink.setOnClickListener(v -> {
            // 在当前的认证导航图内导航回登录页面
            NavController navController = Navigation.findNavController(v);
            // 使用 auth_navigation.xml 中定义的从 registrationFragment 到 loginFragment 的 Action ID
            navController.navigate(R.id.action_registrationFragment_to_loginFragment);
        });

        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null; // 清除绑定对象
    }

    // onActivityCreated 方法已废弃，通常不再使用
    // @Override
    // public void onActivityCreated(@Nullable Bundle savedInstanceState) {
    //     super.onActivityCreated(savedInstanceState);
    //     mViewModel = new ViewModelProvider(this).get(RegistrationViewModel.class);
    //     // TODO: Use the ViewModel
    // }
}