package com.example.onlineteach.utils;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.onlineteach.R;

/**
 * Toast工具类，用于显示带有应用图标的自定义Toast
 */
public class ToastUtils {

    /**
     * 显示带有应用图标的自定义Toast
     *
     * @param context 上下文
     * @param message 要显示的消息
     * @param duration Toast显示时长
     */
    public static void showToast(Context context, String message, int duration) {
        // 获取LayoutInflater实例
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        
        // 加载自定义Toast布局
        View layout = inflater.inflate(R.layout.custom_toast, null);
        
        // 设置图标和消息
        ImageView imageView = layout.findViewById(R.id.toast_icon);
        TextView textView = layout.findViewById(R.id.toast_text);
        
        // 设置应用图标（使用launcher图标）
        imageView.setImageResource(R.mipmap.ic_launcher);
        textView.setText(message);
        
        // 创建并显示Toast
        Toast toast = new Toast(context);
        toast.setDuration(duration);
        toast.setView(layout);
        toast.show();
    }
    
    /**
     * 显示短时间的带有应用图标的Toast
     *
     * @param context 上下文
     * @param message 要显示的消息
     */
    public static void showShortToast(Context context, String message) {
        showToast(context, message, Toast.LENGTH_SHORT);
    }
    
    /**
     * 显示长时间的带有应用图标的Toast
     *
     * @param context 上下文
     * @param message 要显示的消息
     */
    public static void showLongToast(Context context, String message) {
        showToast(context, message, Toast.LENGTH_LONG);
    }
}