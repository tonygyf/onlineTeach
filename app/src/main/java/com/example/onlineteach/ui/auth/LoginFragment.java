package com.example.onlineteach.ui.auth;

import androidx.lifecycle.ViewModelProvider;

import android.content.Intent; // 导入 Intent
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController; // 导入 NavController
import androidx.navigation.Navigation; // 导入 Navigation

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast; // 导入 Toast

import com.example.onlineteach.MainActivity; // 替换为你的主 Activity 类路径
import com.example.onlineteach.R; // 导入你的 R 文件
import com.example.onlineteach.databinding.FragmentLoginBinding; // 导入 View Binding 生成的类
import com.example.onlineteach.dialog.LottieLoadingDialog;
import com.example.onlineteach.utils.ToastUtils;


public class LoginFragment extends Fragment {

    private LoginViewModel loginViewModel;
    private FragmentLoginBinding binding; // 使用 View Binding
    private LottieLoadingDialog loadingDialog;  // 引入 LottieLoadingDialog

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        // **** 使用 View Binding 膨胀布局 ****
        binding = FragmentLoginBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        // 获取 LoginViewModel 实例
        loginViewModel = new ViewModelProvider(this).get(LoginViewModel.class);

        // 初始化 LottieLoadingDialog
        loadingDialog = new LottieLoadingDialog(requireContext());

        // 观察 ViewModel 中的登录结果 LiveData
        loginViewModel.getLoginResult().observe(getViewLifecycleOwner(), result -> {
            if (result != null) {
                if (result.isSuccess()) {
                    // 登录成功，显示消息并跳转到主 Activity
                    ToastUtils.showShortToast(getContext(), result.getMessage());
                    Intent intent = new Intent(requireActivity(), MainActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    requireActivity().finish(); // 关闭当前的认证 Activity

                } else {
                    // 登录失败，显示错误信息
                    ToastUtils.showShortToast(getContext(), result.getMessage());
                    // TODO: 可以在输入框下方显示具体的错误信息
                }
            }
        });
        // 观察加载状态 LiveData
        loginViewModel.getIsLoading().observe(getViewLifecycleOwner(), isLoading -> {
            if (isLoading) {
                loadingDialog.show();  // 显示加载动画
            } else {
                loadingDialog.dismiss();  // 隐藏加载动画
            }
        });

        // 设置登录按钮点击事件，获取输入并调用 ViewModel
        binding.buttonLogin.setOnClickListener(v -> {
            String username = binding.editTextLoginUsername.getText().toString().trim();
            String password = binding.editTextLoginPassword.getText().toString().trim();

            // 调用 ViewModel 中的登录方法
            loginViewModel.loginUser(username, password);
        });

        // 设置“没有账号？去注册” TextView 点击事件
        binding.textViewRegisterLink.setOnClickListener(v -> {
            NavController navController = Navigation.findNavController(v);
            // 使用 auth_navigation.xml 中定义的从 loginFragment 到 registrationFragment 的 Action ID
            navController.navigate(R.id.action_loginFragment_to_registrationFragment);
        });

        // **** 返回 View Binding 的根视图，这是显示界面的关键 ****
        return root;
    }

    // onViewCreated 方法（可选，如果需要在视图创建后进行额外设置）
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // 可以在这里进行视图相关的额外设置
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null; // 在 Fragment 销毁视图时清除绑定对象，避免内存泄漏
    }
}