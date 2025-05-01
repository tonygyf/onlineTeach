package com.example.onlineteach;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

public class SplashActivity extends AppCompatActivity {

    private static final long SPLASH_DURATION_MS = 3000; // 设置开屏显示的时间，例如 3000 毫秒 (3 秒)

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 隐藏状态栏，实现全屏显示
        getWindow().setFlags(
            android.view.WindowManager.LayoutParams.FLAG_FULLSCREEN,
            android.view.WindowManager.LayoutParams.FLAG_FULLSCREEN
        );
        setContentView(R.layout.activity_splash); // 设置布局文件

        // 使用 Handler 来实现延时跳转
        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                // 创建 Intent 跳转到主界面 Activity
                Intent intent = new Intent(SplashActivity.this, AuthActivity.class);
                startActivity(intent); // 启动认证界面

                // 结束当前的 Splash Activity，防止用户按返回键回到开屏界面
                finish();
            }
        }, SPLASH_DURATION_MS); // 延时的时间
    }
}