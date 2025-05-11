package com.example.onlineteach.service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.net.Uri;
import android.os.Build;
import android.os.IBinder;
import android.provider.Settings;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.example.onlineteach.MainActivity;
import com.example.onlineteach.R;
import com.example.onlineteach.utils.ToastUtils;

/**
 * 悬浮球助手服务
 * 提供一个可拖动的悬浮球，点击后可以打开主应用
 */
public class FloatingAssistantService extends Service {

    private WindowManager windowManager;
    private View floatingView;
    private WindowManager.LayoutParams params;
    
    // 记录触摸点相对于悬浮球的偏移量
    private float touchX, touchY;
    private float offsetX, offsetY;
    
    @Override
    public void onCreate() {
        super.onCreate();
        
        // 初始化WindowManager
        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        
        // 加载悬浮球布局
        floatingView = LayoutInflater.from(this).inflate(R.layout.layout_floating_ball, null);
        
        // 设置WindowManager的布局参数
        params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                getWindowLayoutType(),
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT);
        
        // 初始位置在屏幕右侧中间
        params.gravity = Gravity.END | Gravity.CENTER_VERTICAL;
        params.x = 0;
        params.y = 0;
        
        // 设置触摸事件监听
        setTouchListener();
        
        // 添加悬浮球到窗口
        windowManager.addView(floatingView, params);
    }
    
    /**
     * 获取适合当前Android版本的窗口类型
     */
    private int getWindowLayoutType() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            return WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        } else {
            return WindowManager.LayoutParams.TYPE_PHONE;
        }
    }
    
    /**
     * 设置触摸事件监听，实现拖动和点击功能
     */
    private void setTouchListener() {
        floatingView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        // 记录初始触摸位置
                        touchX = event.getRawX();
                        touchY = event.getRawY();
                        
                        // 记录初始偏移量
                        offsetX = params.x;
                        offsetY = params.y;
                        return true;
                        
                    case MotionEvent.ACTION_MOVE:
                        // 计算移动距离
                        float moveX = event.getRawX() - touchX;
                        float moveY = event.getRawY() - touchY;
                        
                        // 更新位置
                        params.x = (int) (offsetX - moveX);
                        params.y = (int) (offsetY + moveY);
                        
                        // 更新悬浮球位置
                        windowManager.updateViewLayout(floatingView, params);
                        return true;
                        
                    case MotionEvent.ACTION_UP:
                        // 如果移动距离很小，视为点击
                        if (Math.abs(event.getRawX() - touchX) < 10 && 
                            Math.abs(event.getRawY() - touchY) < 10) {
                            // 点击事件，打开主应用
                            Intent intent = new Intent(FloatingAssistantService.this, MainActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intent);
                        }
                        return true;
                }
                return false;
            }
        });
    }
    
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
    
    @Override
    public void onDestroy() {
        super.onDestroy();
        if (floatingView != null && windowManager != null) {
            windowManager.removeView(floatingView);
        }
    }
    
    /**
     * 当应用被清除出后台时调用
     */
    @Override
    public void onTaskRemoved(Intent rootIntent) {
        super.onTaskRemoved(rootIntent);
        // 应用被清除出后台时，停止服务并移除悬浮球
        stopSelf();
    }
    
    /**
     * 检查是否有悬浮窗权限
     */
    public static boolean canDrawOverlays(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return Settings.canDrawOverlays(context);
        }
        return true;
    }
    
    /**
     * 请求悬浮窗权限
     */
    public static void requestOverlayPermission(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:" + context.getPackageName()));
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
            ToastUtils.showToast(context, "请授予悬浮窗权限", Toast.LENGTH_SHORT);
        }
    }
}