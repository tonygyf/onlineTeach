package com.example.onlineteach.ui.personalinfo;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import android.Manifest;
import android.animation.Animator;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.Gravity;
import android.widget.FrameLayout;

import com.airbnb.lottie.LottieAnimationView;
import com.example.onlineteach.AuthActivity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import com.google.android.material.textfield.TextInputEditText;
import com.example.onlineteach.R;
import com.example.onlineteach.utils.ToastUtils;
import com.example.onlineteach.databinding.FragmentPersonalInfoBinding; // Data Binding 生成的类
import com.example.onlineteach.ui.home.MenuAdapter;
import com.example.onlineteach.ui.home.MenuItem;

public class PersonalInfoFragment extends Fragment {

    private PersonalInfoViewModel mViewModel;
    private FragmentPersonalInfoBinding binding; // Data Binding 绑定对象

    public static PersonalInfoFragment newInstance() {
        return new PersonalInfoFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        // 使用 Data Binding 膨胀布局
        binding = FragmentPersonalInfoBinding.inflate(inflater, container, false);

        // 获取 ViewModel
        mViewModel = new ViewModelProvider(this).get(PersonalInfoViewModel.class);

        // 将 ViewModel 绑定到布局中的变量
        binding.setViewModel(mViewModel);

        // 设置生命周期所有者，以便 LiveData 能够自动更新 UI
        binding.setLifecycleOwner(getViewLifecycleOwner());

        // 返回 Data Binding 的根视图
        return binding.getRoot();
    }

    // onActivityCreated 已被废弃，通常在 onViewCreated 中进行视图相关的初始化
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        // 设置更换头像按钮的点击事件
        binding.btnChangeAvatar.setOnClickListener(v -> {
            openGallery();
        });
        
        // 观察头像URI变化
        mViewModel.getAvatarUri().observe(getViewLifecycleOwner(), uri -> {
            if (uri != null) {
                binding.profileAvatar.setImageURI(uri);
            }
        });
        
        // 加载保存的头像
        mViewModel.loadAvatarFromPrefs(requireContext());
        
        // 设置菜单列表
        setupMenuRecyclerView();
    }
    
    /**
     * 设置菜单RecyclerView
     */
    private void setupMenuRecyclerView() {
        RecyclerView recyclerView = binding.recyclerViewMenu;
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        
        // 观察菜单项数据变化
        mViewModel.getMenuItems().observe(getViewLifecycleOwner(), menuItems -> {
            MenuAdapter adapter = new MenuAdapter(menuItems, position -> {
                // 处理菜单项点击事件
                handleMenuItemClick(position);
            });
            recyclerView.setAdapter(adapter);
        });
    }
    
    /**
     * 处理菜单项点击事件
     * @param position 点击的菜单项位置
     */
    private void handleMenuItemClick(int position) {
        // 根据点击的位置执行相应的操作
        switch (position) {
            case 0: // 修改个人信息
                showEditPersonalInfoDialog();
                break;
            case 1: // 修改密码
                showChangePasswordDialog();
                break;
            case 2: // 浏览记录
                ToastUtils.showShortToast(requireContext(), "浏览记录功能待实现");
                break;
            case 3: // 退出账户
                mViewModel.logout();
                // 显示退出动画
                LottieAnimationView exitAnimationView = new LottieAnimationView(requireContext());
                exitAnimationView.setAnimation("exit.json");
                exitAnimationView.setVisibility(View.VISIBLE);
                exitAnimationView.playAnimation();
                
                // 将动画视图添加到当前布局
                FrameLayout container = new FrameLayout(requireContext());
                container.setLayoutParams(new ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT));
                container.setBackgroundColor(Color.WHITE);
                container.addView(exitAnimationView);
                
                // 设置动画视图的布局参数 - 使用较大的固定尺寸
                FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                        FrameLayout.LayoutParams.MATCH_PARENT,
                        FrameLayout.LayoutParams.MATCH_PARENT);
                params.gravity = Gravity.CENTER;
                // 设置边距，保持一定的边界空间
                int margin = (int) (getResources().getDisplayMetrics().density * 20);
                params.setMargins(margin, margin, margin, margin);
                exitAnimationView.setLayoutParams(params);
                
                // 将动画容器添加到当前视图
                ViewGroup rootView = (ViewGroup) requireActivity().getWindow().getDecorView().findViewById(android.R.id.content);
                rootView.addView(container);
                
                // 添加动画监听器，动画结束后跳转到AuthActivity
                exitAnimationView.addAnimatorListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animation) {}
                    
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        // 动画结束后跳转到AuthActivity
                        Intent intent = new Intent(requireActivity(), AuthActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        requireActivity().finish();
                    }
                    
                    @Override
                    public void onAnimationCancel(Animator animation) {
                        // 如果动画被取消，也执行跳转
                        Intent intent = new Intent(requireActivity(), AuthActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        requireActivity().finish();
                    }
                    
                    @Override
                    public void onAnimationRepeat(Animator animation) {}
                });
                
                ToastUtils.showShortToast(requireContext(), "已退出登录");
                break;
        }
    }

    /**
     * 显示编辑个人信息对话框
     */
    private void showEditPersonalInfoDialog() {
        // 创建对话框
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_edit_personal_info, null);
        builder.setView(dialogView);

        // 获取对话框中的控件
        TextInputEditText editTextUsername = dialogView.findViewById(R.id.editTextUsername);
        TextInputEditText editTextStudentId = dialogView.findViewById(R.id.editTextStudentId);
        Button btnCancel = dialogView.findViewById(R.id.btnCancel);
        Button btnSave = dialogView.findViewById(R.id.btnSave);

        // 设置当前用户信息
        editTextUsername.setText(mViewModel.getUserName().getValue());
        editTextStudentId.setText(mViewModel.getStudentId().getValue());

        // 创建对话框
        AlertDialog dialog = builder.create();

        // 设置取消按钮点击事件
        btnCancel.setOnClickListener(v -> dialog.dismiss());

        // 设置保存按钮点击事件
        btnSave.setOnClickListener(v -> {
            String newUsername = editTextUsername.getText().toString().trim();
            String newStudentId = editTextStudentId.getText().toString().trim();

            if (newUsername.isEmpty() || newStudentId.isEmpty()) {
                ToastUtils.showShortToast(requireContext(), "用户名和学号不能为空");
                return;
            }

            // 调用ViewModel更新用户信息
            mViewModel.updateUserInfo(newUsername, newStudentId);
            dialog.dismiss();
            ToastUtils.showShortToast(requireContext(), "个人信息已更新");
        });

        dialog.show();
    }
    
    private static final int PICK_IMAGE_REQUEST = 1;
    private static final int PERMISSION_REQUEST_CODE = 2;
    
    // 打开图库选择图片
    private void openGallery() {
        // 检查权限
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                // 请求权限
                ActivityCompat.requestPermissions(requireActivity(),
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                        PERMISSION_REQUEST_CODE);
                return;
            }
        }
        
        // 已有权限，打开图库
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }
    
    // 处理图片选择结果
    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null && data.getData() != null) {
            Uri imageUri = data.getData();
            // 先设置临时显示
            mViewModel.setAvatarUri(imageUri);
            // 保存到内部存储并更新URI
            Uri savedUri = mViewModel.saveAvatarToInternalStorage(requireContext(), imageUri);
            if (savedUri != null) {
                mViewModel.setAvatarUri(savedUri);
                ToastUtils.showShortToast(getContext(), "头像已保存");
            } else {
                ToastUtils.showShortToast(getContext(), "头像保存失败");
            }
        } else if (resultCode == Activity.RESULT_CANCELED) {
            ToastUtils.showShortToast(getContext(), "取消选择头像");
        }
    }
    
    // 处理权限请求结果
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // 权限被授予，打开图库
                openGallery();
            } else {
                // 权限被拒绝
                ToastUtils.showShortToast(getContext(), "需要存储权限才能选择头像");
            }
            return;
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    /**
     * 显示修改密码对话框
     */
    private void showChangePasswordDialog() {
        // 创建对话框
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_change_password, null);
        builder.setView(dialogView);

        // 获取对话框中的控件
        TextInputEditText editTextOldPassword = dialogView.findViewById(R.id.editTextOldPassword);
        TextInputEditText editTextNewPassword = dialogView.findViewById(R.id.editTextNewPassword);
        TextInputEditText editTextConfirmPassword = dialogView.findViewById(R.id.editTextConfirmPassword);
        Button btnCancel = dialogView.findViewById(R.id.btnCancel);
        Button btnSave = dialogView.findViewById(R.id.btnSave);

        // 创建对话框
        AlertDialog dialog = builder.create();

        // 设置取消按钮点击事件
        btnCancel.setOnClickListener(v -> dialog.dismiss());

        // 设置保存按钮点击事件
        btnSave.setOnClickListener(v -> {
            String oldPassword = editTextOldPassword.getText().toString().trim();
            String newPassword = editTextNewPassword.getText().toString().trim();
            String confirmPassword = editTextConfirmPassword.getText().toString().trim();

            if (oldPassword.isEmpty() || newPassword.isEmpty() || confirmPassword.isEmpty()) {
                ToastUtils.showShortToast(requireContext(), "请填写所有密码字段");
                return;
            }

            if (!newPassword.equals(confirmPassword)) {
                ToastUtils.showShortToast(requireContext(), "新密码与确认密码不一致");
                return;
            }

            // 调用ViewModel更新密码
            mViewModel.changePassword(oldPassword, newPassword);
            dialog.dismiss();
        });

        // 观察密码修改结果
        mViewModel.getPasswordChangeResult().observe(getViewLifecycleOwner(), success -> {
            if (success != null) {
                if (success) {
                    ToastUtils.showShortToast(requireContext(), "密码修改成功");
                } else {
                    ToastUtils.showShortToast(requireContext(), "密码修改失败，请检查旧密码是否正确");
                }
            }
        });

        dialog.show();
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
                    actionBar.setTitle(R.string.title_personal);
                    actionBar.show();
                }
            }
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null; // 清除绑定对象以避免内存泄漏
    }
}