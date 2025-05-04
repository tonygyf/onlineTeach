package com.example.onlineteach.dialog;


import android.app.Dialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.Window;
import android.view.WindowManager;
import com.airbnb.lottie.LottieAnimationView;

public class LottieLoadingDialog {

    private final Dialog dialog;

    public LottieLoadingDialog(Context context) {
        dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(false);

        LayoutInflater inflater = LayoutInflater.from(context);
        LottieAnimationView animView = new LottieAnimationView(context);
        animView.setAnimation("loading.json");  // 这里的文件名需要与你的 .json 文件一致
        animView.setRepeatCount(-1);  // 设置动画循环播放
        animView.playAnimation();  // 播放动画

        dialog.setContentView(animView);
        Window window = dialog.getWindow();
        if (window != null) {
            window.setBackgroundDrawableResource(android.R.color.transparent);
            window.setLayout(WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT);  // 自动调整窗口大小
        }
    }

    public void show() {
        if (!dialog.isShowing()) {
            dialog.show();  // 显示加载动画
        }
    }

    public void dismiss() {
        if (dialog.isShowing()) {
            dialog.dismiss();  // 隐藏加载动画
        }
    }
}
