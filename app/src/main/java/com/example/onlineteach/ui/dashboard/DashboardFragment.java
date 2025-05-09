package com.example.onlineteach.ui.dashboard;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.onlineteach.R;
import com.example.onlineteach.databinding.FragmentDashboardBinding;

public class DashboardFragment extends Fragment {

    private FragmentDashboardBinding binding;
    private DashboardViewModel dashboardViewModel;
    private EnrolledCoursesAdapter enrolledCoursesAdapter;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        dashboardViewModel = new ViewModelProvider(this).get(DashboardViewModel.class);

        binding = FragmentDashboardBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        setupRecyclerView();
        observeEnrolledCourses();

        return root;
    }

    private void setupRecyclerView() {
        enrolledCoursesAdapter = new EnrolledCoursesAdapter();
        binding.recyclerViewEnrolledCourses.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.recyclerViewEnrolledCourses.setAdapter(enrolledCoursesAdapter);
    }

    private void observeEnrolledCourses() {
        dashboardViewModel.getEnrolledCourses().observe(getViewLifecycleOwner(), enrollments -> {
            enrolledCoursesAdapter.setEnrollments(enrollments);
        });
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