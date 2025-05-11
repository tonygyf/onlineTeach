package com.example.onlineteach;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.example.onlineteach.data.repository.UserRepository;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.example.onlineteach.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private boolean isBackgroundRunning = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        BottomNavigationView navView = findViewById(R.id.nav_view);
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_home, R.id.navigation_dashboard, R.id.navigation_notifications)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_activity_main);
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(binding.navView, navController);
        
        // 检查悬浮窗权限
        checkFloatingPermission();
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_help) {
            // 检查用户是否已登录
            UserRepository userRepository = new UserRepository(this);
            int userId = userRepository.getLoggedInUserId();
            
            if (userId != -1) {
                // 用户已登录，跳转到IntroActivity
                startActivity(new Intent(MainActivity.this, IntroActivity.class));
            } else {
                // 用户未登录，提示需要登录
                Toast.makeText(this, "请先登录后再查看介绍页面", Toast.LENGTH_SHORT).show();
                // 可选：跳转到登录页面
                startActivity(new Intent(MainActivity.this, AuthActivity.class));
            }
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    // ⭐️ 添加这个方法来处理返回按钮点击
    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_activity_main);
        return navController.navigateUp() || super.onSupportNavigateUp();
    }
    
    /**
     * 检查悬浮窗权限
     */
    private void checkFloatingPermission() {
        if (!com.example.onlineteach.service.FloatingAssistantService.canDrawOverlays(this)) {
            // 没有权限，请求权限
            com.example.onlineteach.service.FloatingAssistantService.requestOverlayPermission(this);
        }
    }
    
    /**
     * 启动悬浮球服务
     */
    private void startFloatingService() {
        if (com.example.onlineteach.service.FloatingAssistantService.canDrawOverlays(this)) {
            Intent intent = new Intent(this, com.example.onlineteach.service.FloatingAssistantService.class);
            startService(intent);
            isBackgroundRunning = true;
        } else {
            Toast.makeText(this, "需要悬浮窗权限才能启动悬浮球", Toast.LENGTH_SHORT).show();
            checkFloatingPermission();
        }
    }
    
    /**
     * 停止悬浮球服务
     */
    private void stopFloatingService() {
        if (isBackgroundRunning) {
            Intent intent = new Intent(this, com.example.onlineteach.service.FloatingAssistantService.class);
            stopService(intent);
            isBackgroundRunning = false;
        }
    }
    
    @Override
    protected void onPause() {
        super.onPause();
        // 当应用进入后台时启动悬浮球
        startFloatingService();
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        // 当应用回到前台时停止悬浮球
        stopFloatingService();
    }
}
