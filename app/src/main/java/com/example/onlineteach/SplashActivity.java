package com.example.onlineteach;

import android.content.Intent;
import android.os.Bundle;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.airbnb.lottie.LottieAnimationView;
import com.airbnb.lottie.LottieComposition;
import com.airbnb.lottie.LottieCompositionFactory;
import com.airbnb.lottie.LottieTask;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        
        Log.d("SplashActivity", "onCreate 开始执行");
        
        // 设置超时处理，确保即使动画加载失败也能跳转
        new android.os.Handler().postDelayed(() -> {
            // 如果3秒后仍在此Activity，则强制跳转
            if (!isFinishing()) {
                Log.d("SplashActivity", "超时触发，强制跳转");
                startActivity(new Intent(SplashActivity.this, AuthActivity.class));
                finish();
            }
        }, 3000); // 3秒超时

        try {
            // 获取动画视图
            LottieAnimationView lottie = findViewById(R.id.splash_lottie);
            
            // 预加载动画资源，避免直接从XML加载可能导致的问题
            LottieTask<LottieComposition> compositionTask = 
                LottieCompositionFactory.fromRawRes(this, R.raw.my_splash_animation);
                
            compositionTask.addListener(composition -> {
                Log.d("SplashActivity", "动画资源加载成功");
                // 动画资源加载成功后设置
                lottie.setComposition(composition);
                lottie.setRepeatCount(0); // 不重复播放
                
                // 添加动画监听器
                lottie.playAnimation();
                
                // 添加动画监听器
                lottie.addAnimatorListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                        Log.d("SplashActivity", "动画播放结束，准备跳转");
                        // 动画结束后跳转到主界面
                        startActivity(new Intent(SplashActivity.this, AuthActivity.class));
                        finish();  // 确保结束当前的 SplashActivity
                    }

                    @Override
                    public void onAnimationStart(Animator animation) {
                        super.onAnimationStart(animation);
                        Log.d("SplashActivity", "动画开始播放");
                    }

                    @Override
                    public void onAnimationCancel(Animator animation) {
                        super.onAnimationCancel(animation);
                        Log.d("SplashActivity", "动画播放取消");
                    }

                    @Override
                    public void onAnimationRepeat(Animator animation) {
                        super.onAnimationRepeat(animation);
                        Log.d("SplashActivity", "动画重复播放");
                    }
                });
            });
        } catch (Exception e) {
            Log.e("SplashActivity", "动画加载失败", e);
            // 发生异常时也跳转到下一个界面
            startActivity(new Intent(SplashActivity.this, AuthActivity.class));
            finish();
        }
    }
}
