package com.example.onlineteach.ui.dashboard;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.viewpager2.widget.ViewPager2;

import com.alamkanak.weekview.WeekView;
import com.alamkanak.weekview.WeekViewEntity;
import com.example.onlineteach.R;
import com.example.onlineteach.databinding.FragmentDashboardBinding;
import com.example.onlineteach.utils.DateTimeUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class DashboardFragment extends Fragment {

    private FragmentDashboardBinding binding;
    private List<IntroSlide> introSlides;


    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        DashboardViewModel dashboardViewModel =
                new ViewModelProvider(this).get(DashboardViewModel.class);

        binding = FragmentDashboardBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        // 设置ViewPager2滑动介绍
        setupIntroSlides();
        binding.introViewPager.setAdapter(new IntroSlideAdapter(introSlides));

        // 设置WeekView日历
        setupWeekView();

        return root;
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
     * 设置周视图日历
     */
    private void setupWeekView() {
        WeekView weekView = binding.weekView;

        weekView.setAdapter(new WeekView.SimpleAdapter() {
            @NonNull

            public List<WeekViewEntity> onLoad(@NonNull LocalDateTime start, @NonNull LocalDateTime end) {
                List<WeekViewEntity> events = new ArrayList<>();
                LocalDate today = LocalDate.now();

                // 添加一些示例课程
                addEvent(events, today, 9, 0, 10, 30, "Java编程基础", Color.parseColor("#FF4081"));
                addEvent(events, today, 13, 0, 14, 30, "Android开发入门", Color.parseColor("#3F51B5"));
                addEvent(events, today.plusDays(1), 10, 0, 11, 30, "数据结构与算法", Color.parseColor("#009688"));
                addEvent(events, today.plusDays(2), 14, 0, 16, 0, "网络编程实践", Color.parseColor("#FF9800"));

                return events;
            }
        });
    }
    /**
     * 添加日历事件
     */
    private void addEvent(List<WeekViewEntity> events, LocalDate date, int startHour, int startMinute,
                          int endHour, int endMinute, String title, int color) {
        LocalDateTime startTime = LocalDateTime.of(date, LocalTime.of(startHour, startMinute));
        LocalDateTime endTime = LocalDateTime.of(date, LocalTime.of(endHour, endMinute));

        int eventId = events.size() + 1;

        WeekViewEntity event = new WeekViewEntity.Event.Builder<>(
                eventId

        )
                .setTitle(title)

                .build();

        events.add(event);
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
                    actionBar.setTitle(R.string.title_dashboard);
                    actionBar.show();
                }
            }
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
        // 清除资源
        introSlides = null;
    }
}