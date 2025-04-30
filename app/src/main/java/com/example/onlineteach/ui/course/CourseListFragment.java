package com.example.onlineteach.ui.course;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.onlineteach.R;
import com.example.onlineteach.data.model.Course;

public class CourseListFragment extends Fragment {

    private CourseListViewModel mViewModel;
    private RecyclerView recyclerView;

    public static CourseListFragment newInstance() {
        return new CourseListFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_course_list, container, false);
        recyclerView = root.findViewById(R.id.recycler_view_courses);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        return root;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mViewModel = new ViewModelProvider(this).get(CourseListViewModel.class);

        // 初始化适配器
        CourseListAdapter adapter = new CourseListAdapter();
        recyclerView.setAdapter(adapter);

        // 观察课程列表数据变化
        mViewModel.getCourses().observe(getViewLifecycleOwner(), courses -> {
            adapter.setCourses(courses);
        });
    }
}