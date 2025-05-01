package com.example.onlineteach.ui.personalinfo;

import androidx.lifecycle.ViewModelProvider;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;

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
import android.widget.Toast;

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
                ToastUtils.showShortToast(requireContext(), "修改个人信息功能待实现");
                break;
            case 1: // 浏览记录
                ToastUtils.showShortToast(requireContext(), "浏览记录功能待实现");
                break;
            case 2: // 退出账户
                mViewModel.logout();
                ToastUtils.showShortToast(requireContext(), "已退出登录");
                break;
        }
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

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null; // 清除绑定对象以避免内存泄漏
    }
}