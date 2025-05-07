package com.example.onlineteach;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import com.example.onlineteach.ui.intro.IntroSlide;
import com.example.onlineteach.ui.intro.IntroSlideAdapter;

import java.util.ArrayList;
import java.util.List;

public class IntroActivity extends AppCompatActivity {

    private ViewPager2 introViewPager;
    private Button skipButton;
    private List<IntroSlide> introSlides;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_intro);

        // 隐藏ActionBar
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        introViewPager = findViewById(R.id.intro_view_pager);
        skipButton = findViewById(R.id.skip_button);

        // 设置介绍滑动页面
        setupIntroSlides();
        introViewPager.setAdapter(new IntroSlideAdapter(introSlides));

        // 设置跳过按钮点击事件
        skipButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finishIntroAndGoToMain();
            }
        });
    }

    /**
     * 设置介绍滑动页面
     */
    private void setupIntroSlides() {
        introSlides = new ArrayList<>();

        // 添加介绍页面
        introSlides.add(new IntroSlide(
                "lesson.json",
                "在线教学平台",
                "随时随地学习，提高学习效率"));

        introSlides.add(new IntroSlide(
                "student.json",
                "丰富的课程资源",
                "各类课程资源，满足不同学习需求"));

        introSlides.add(new IntroSlide(
                "teacher.json",
                "实时互动交流",
                "师生互动，小组讨论，提高学习体验"));
    }

    /**
     * 完成介绍并跳转到主页面
     */
    private void finishIntroAndGoToMain() {
        // 保存已显示过介绍页面的标记
        SharedPreferences sharedPreferences = getSharedPreferences("app_prefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("intro_shown", true);
        editor.apply();

        // 跳转到主页面
        startActivity(new Intent(IntroActivity.this, MainActivity.class));
        finish();
    }
}