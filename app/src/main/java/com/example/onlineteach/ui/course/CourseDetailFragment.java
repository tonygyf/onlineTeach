package com.example.onlineteach.ui.course;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;

import com.example.onlineteach.R;
import com.example.onlineteach.data.database.AppDatabase;
import com.example.onlineteach.data.model.Course;
import com.example.onlineteach.data.repository.EnrollmentRepository;
import com.example.onlineteach.data.repository.UserRepository;
import com.example.onlineteach.utils.ToastUtils;
import com.example.onlineteach.databinding.FragmentCourseDetailBinding;

import java.util.concurrent.atomic.AtomicBoolean;

public class CourseDetailFragment extends Fragment {

    private FragmentCourseDetailBinding binding;
    private CourseDetailViewModel viewModel;
    private ImageView courseImage;
    private TextView courseTitle;
    private TextView courseTeacher;
    private TextView courseCredits;
    private TextView courseDescription;
    private Button enrollButton;
    private EnrollmentRepository enrollmentRepository;
    private UserRepository userRepository;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentCourseDetailBinding.inflate(inflater, container, false);
        viewModel = new ViewModelProvider(this).get(CourseDetailViewModel.class);

        // 获取传递的课程ID
        int courseId = getArguments() != null ? getArguments().getInt("courseId", -1) : -1;
        if (courseId != -1) {
            viewModel.loadCourse(courseId);
            observeCourse();
        }

        // 初始化视图
        courseImage = binding.imageCourseDetail;
        courseTitle = binding.textCourseTitleDetail;
        courseTeacher = binding.textCourseTeacherDetail;
        courseCredits = binding.textCourseCreditsDetail;
        courseDescription = binding.textCourseDescription;
        enrollButton = binding.buttonEnrollDetail;
        
        return binding.getRoot();
    }

    private void observeCourse() {
        viewModel.getCourse().observe(getViewLifecycleOwner(), this::updateUI);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        // 初始化仓库
        enrollmentRepository = new EnrollmentRepository(requireContext());
        userRepository = new UserRepository(requireContext());

        // 设置报名按钮点击事件
        enrollButton.setOnClickListener(v -> {
            Course course = viewModel.getCourse().getValue();
            if (course != null) {
                // 使用AtomicBoolean来在异步操作中存储结果
                AtomicBoolean isAlreadyEnrolled = new AtomicBoolean(false);
                
                // 在后台线程中检查是否已报名
                AppDatabase.databaseWriteExecutor.execute(() -> {
                    // 获取当前登录用户ID
                    int currentUserId = userRepository.getLoggedInUserId();
                    if (currentUserId == -1) {
                        requireActivity().runOnUiThread(() -> {
                            ToastUtils.showShortToast(requireContext(), "请先登录");
                        });
                        return;
                    }
                    
                    isAlreadyEnrolled.set(enrollmentRepository.isEnrolled(currentUserId, course.getCourseId()));
                    
                    // 在主线程中处理结果
                    requireActivity().runOnUiThread(() -> {
                        if (isAlreadyEnrolled.get()) {
                            ToastUtils.showShortToast(requireContext(), "您已报名该课程");
                        } else {
                            // 报名课程
                            enrollmentRepository.enrollCourse(currentUserId, course.getCourseId());
                            ToastUtils.showShortToast(requireContext(), "报名成功: " + course.getTitle());
                        }
                    });
                });
            }
        });
    }
    
    /**
     * 更新UI显示
     * @param course 课程对象
     */
    private void updateUI(Course course) {
        if (course != null) {
            courseTitle.setText(course.getTitle());
            courseTeacher.setText("授课教师: " + course.getTeacher());
            courseCredits.setText(String.format("学分: %.1f", course.getCredits()));
            
            // 设置课程描述
            if (course.getDescription() != null && !course.getDescription().isEmpty()) {
                courseDescription.setText(course.getDescription());
            } else {
                courseDescription.setText("这是" + course.getTitle() + "课程的详细介绍，包含课程大纲、教学目标、考核方式等信息。");
            }
            
            // 设置课程图片
            // 这里假设使用drawable资源，实际应用中可能需要从网络加载或其他方式
            int resourceId = getResources().getIdentifier(
                    course.getImageUrl().replace("@drawable/", ""),
                    "drawable",
                    requireContext().getPackageName());
            
            if (resourceId != 0) {
                courseImage.setImageResource(resourceId);
            }
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}