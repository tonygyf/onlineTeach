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
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.onlineteach.R;
import com.example.onlineteach.data.model.Course;
import com.example.onlineteach.utils.ToastUtils;

public class CourseListFragment extends Fragment {

    private CourseListViewModel mViewModel;
    private RecyclerView recyclerView;
    private SwipeRefreshLayout swipeRefreshLayout;
    private CourseListAdapter adapter;

    public static CourseListFragment newInstance() {
        return new CourseListFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_course_list, container, false);
        recyclerView = root.findViewById(R.id.recycler_view_courses);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        
        // 初始化下拉刷新控件
        swipeRefreshLayout = root.findViewById(R.id.swipe_refresh_layout);
        swipeRefreshLayout.setColorSchemeResources(R.color.colorPrimary, R.color.colorAccent);
        
        return root;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // 使用AndroidViewModel的Factory来创建ViewModel
        mViewModel = new ViewModelProvider(requireActivity(), 
            ViewModelProvider.AndroidViewModelFactory.getInstance(requireActivity().getApplication()))
            .get(CourseListViewModel.class);

        // 初始化适配器
        adapter = new CourseListAdapter();
        recyclerView.setAdapter(adapter);
        
        // 设置课程点击事件
        adapter.setOnCourseClickListener(course -> {
            // 设置当前选中的课程
            mViewModel.selectCourse(course);
            // 跳转到课程详情页面
            Bundle args = new Bundle();
            args.putInt("courseId", course.getCourseId());
            Navigation.findNavController(view)
                    .navigate(R.id.action_navigation_course_list_to_course_detail, args);
        });

        // 观察课程列表数据变化
        mViewModel.getCourses().observe(getViewLifecycleOwner(), courses -> {
            adapter.setCourses(courses);
            swipeRefreshLayout.setRefreshing(false);
        });
        
        // 设置下拉刷新监听器
        swipeRefreshLayout.setOnRefreshListener(() -> {
            // 刷新数据
            refreshCourseData();
        });
    }
    
    /**
     * 刷新课程数据
     */
    private void refreshCourseData() {
        // 由于使用了LiveData，数据库更新会自动通知UI更新
        // 这里可以添加额外的刷新逻辑，如从网络获取最新数据等
        swipeRefreshLayout.setRefreshing(true);
        // 模拟网络请求延迟
        recyclerView.postDelayed(() -> {
            swipeRefreshLayout.setRefreshing(false);
            ToastUtils.showShortToast(requireContext(), "课程数据已更新");
        }, 1000);
    }
}