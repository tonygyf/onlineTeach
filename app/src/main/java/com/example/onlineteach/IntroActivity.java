package com.example.onlineteach;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.example.onlineteach.ui.intro.IntroSlide;
import com.example.onlineteach.ui.intro.IntroSlideAdapter;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public class IntroActivity extends AppCompatActivity {

    private ViewPager2 introViewPager;
    private Button skipButton;
    private List<IntroSlide> introSlides;
    private ViewPager2.OnPageChangeCallback pageChangeCallback;

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

        try {
            IntroSlideAdapter adapter = new IntroSlideAdapter(introSlides);
            introViewPager.setAdapter(adapter);

            introViewPager.setPageTransformer(new ViewPager2.PageTransformer() {
                private static final float MIN_SCALE = 0.85f;
                private static final float MIN_ALPHA = 0.5f;

                @Override
                public void transformPage(@NonNull View page, float position) {
                    try {
                        int pageWidth = page.getWidth();
                        int pageHeight = page.getHeight();

                        if (position < -1) {
                            page.setAlpha(0f);
                        } else if (position <= 1) {
                            float scaleFactor = Math.max(MIN_SCALE, 1 - Math.abs(position));
                            float vertMargin = pageHeight * (1 - scaleFactor) / 2;
                            float horzMargin = pageWidth * (1 - scaleFactor) / 2;

                            if (position < 0) {
                                page.setTranslationX(horzMargin - vertMargin / 2);
                            } else {
                                page.setTranslationX(-horzMargin + vertMargin / 2);
                            }

                            page.setScaleX(scaleFactor);
                            page.setScaleY(scaleFactor);
                            page.setAlpha(MIN_ALPHA + (scaleFactor - MIN_SCALE) / (1 - MIN_SCALE) * (1 - MIN_ALPHA));
                        } else {
                            page.setAlpha(0f);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });

            pageChangeCallback = new ViewPager2.OnPageChangeCallback() {
                @Override
                public void onPageSelected(int position) {
                    super.onPageSelected(position);
                    try {
                        if (position == introSlides.size() - 1) {
                            skipButton.setText("完成");
                        } else {
                            skipButton.setText("跳过");
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onPageScrollStateChanged(int state) {
                    super.onPageScrollStateChanged(state);
                    try {
                        if (state == ViewPager2.SCROLL_STATE_IDLE) {
                            introViewPager.setUserInputEnabled(true);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            };
            introViewPager.registerOnPageChangeCallback(pageChangeCallback);
            introViewPager.setOffscreenPageLimit(1);
        } catch (Exception e) {
            e.printStackTrace();
        }

        skipButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finishIntroAndGoToMain();
            }
        });

        introViewPager.setOnTouchListener(new View.OnTouchListener() {
            private float startX;
            private float startY;
            private long startTime;
            private static final int MIN_SWIPE_TIME = 100;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                try {
                    switch (event.getAction()) {
                        case MotionEvent.ACTION_DOWN:
                            startX = event.getX();
                            startY = event.getY();
                            startTime = System.currentTimeMillis();
                            break;
                        case MotionEvent.ACTION_UP:
                            float endX = event.getX();
                            float endY = event.getY();
                            long endTime = System.currentTimeMillis();
                            float distanceX = Math.abs(endX - startX);
                            float distanceY = Math.abs(endY - startY);
                            long duration = endTime - startTime;

                            if (distanceX > distanceY && duration < MIN_SWIPE_TIME) {
                                return true;
                            }
                            break;
                    }
                    return false;
                } catch (Exception e) {
                    e.printStackTrace();
                    return true;
                }
            }
        });

        try {
            Field recyclerViewField = ViewPager2.class.getDeclaredField("mRecyclerView");
            recyclerViewField.setAccessible(true);
            RecyclerView recyclerView = (RecyclerView) recyclerViewField.get(introViewPager);

            Field touchSlopField = RecyclerView.class.getDeclaredField("mTouchSlop");
            touchSlopField.setAccessible(true);
            int touchSlop = (int) touchSlopField.get(recyclerView);
            touchSlopField.set(recyclerView, touchSlop * 2);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setupIntroSlides() {
        introSlides = new ArrayList<>();

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

    private void finishIntroAndGoToMain() {
        try {
            SharedPreferences sharedPreferences = getSharedPreferences("app_prefs", MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean("intro_shown", true);
            editor.apply();

            startActivity(new Intent(IntroActivity.this, MainActivity.class));
            finish();
        } catch (Exception e) {
            e.printStackTrace();
            finish();
        }
    }

    @Override
    protected void onDestroy() {
        if (introViewPager != null && pageChangeCallback != null) {
            try {
                introViewPager.unregisterOnPageChangeCallback(pageChangeCallback);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        super.onDestroy();
    }
}
