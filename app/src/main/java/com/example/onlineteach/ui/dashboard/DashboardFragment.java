package com.example.onlineteach.ui.dashboard;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.onlineteach.R;
import com.example.onlineteach.databinding.FragmentDashboardBinding;

public class DashboardFragment extends Fragment implements EnrolledCoursesAdapter.OnCourseClickListener {

    private FragmentDashboardBinding binding;
    private DashboardViewModel dashboardViewModel;
    private EnrolledCoursesAdapter enrolledCoursesAdapter;
    private AlertDialog planningDialog;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        dashboardViewModel = new ViewModelProvider(this).get(DashboardViewModel.class);

        binding = FragmentDashboardBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        setupRecyclerView();
        setupCalendarView();
        observeEnrolledCourses();

        return root;
    }

    private void setupRecyclerView() {
        enrolledCoursesAdapter = new EnrolledCoursesAdapter(this);
        binding.recyclerViewEnrolledCourses.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.recyclerViewEnrolledCourses.setAdapter(enrolledCoursesAdapter);
    }

    private void observeEnrolledCourses() {
        dashboardViewModel.getEnrolledCourses().observe(getViewLifecycleOwner(), enrollments -> {
            enrolledCoursesAdapter.setEnrollments(enrollments);
        });
    }

    @Override
    public void onCourseClick(int courseId) {
        // 导航到课程详情页面
        Bundle args = new Bundle();
        args.putInt("courseId", courseId);
        Navigation.findNavController(requireView())
                .navigate(R.id.action_navigation_dashboard_to_course_detail, args);
    }

    private void setupCalendarView() {
        binding.calendarView.setOnDateChangeListener((view, year, month, dayOfMonth) -> {
            showPlanningDialog(year, month, dayOfMonth);
        });
    }

    private void showPlanningDialog(int year, int month, int dayOfMonth) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("课程规划")
                .setMessage(String.format("%d年%d月%d日的课程安排", year, month + 1, dayOfMonth))
                .setPositiveButton("添加课程", (dialog, which) -> {
                    // TODO: 实现添加课程的逻辑
                })
                .setNegativeButton("取消", (dialog, which) -> dialog.dismiss());

        if (planningDialog != null && planningDialog.isShowing()) {
            planningDialog.dismiss();
        }
        planningDialog = builder.create();
        planningDialog.show();
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
    }
}