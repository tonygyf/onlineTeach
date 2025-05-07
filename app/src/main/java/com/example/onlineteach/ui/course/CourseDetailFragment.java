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

import com.example.onlineteach.R;
import com.example.onlineteach.data.database.AppDatabase;
import com.example.onlineteach.data.model.Course;
import com.example.onlineteach.data.repository.EnrollmentRepository;
import com.example.onlineteach.utils.ToastUtils;

import java.util.concurrent.atomic.AtomicBoolean;

public class CourseDetailFragment extends Fragment {

    private CourseListViewModel viewModel;
    private ImageView courseImage;
    private TextView courseTitle;
    private TextView courseTeacher;
    private TextView courseCredits;
    private TextView courseDescription;
    private Button enrollButton;
    private EnrollmentRepository enrollmentRepository;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_course_detail, container, false);
        
        // 初始化视图
        courseImage = root.findViewById(R.id.image_course_detail);
        courseTitle = root.findViewById(R.id.text_course_title_detail);
        courseTeacher = root.findViewById(R.id.text_course_teacher_detail);
        courseCredits = root.findViewById(R.id.text_course_credits_detail);
        courseDescription = root.findViewById(R.id.text_course_description);
        enrollButton = root.findViewById(R.id.button_enroll_detail);
        
        return root;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        // 获取ViewModel
        viewModel = new ViewModelProvider(requireActivity(), 
            ViewModelProvider.AndroidViewModelFactory.getInstance(requireActivity().getApplication()))
            .get(CourseListViewModel.class);
        
        // 观察选中的课程
        viewModel.getSelectedCourse().observe(getViewLifecycleOwner(), this::updateUI);
        
        // 设置报名按钮点击事件
        // 初始化报名仓库
        enrollmentRepository = new EnrollmentRepository(requireContext());

        // 设置报名按钮点击事件
        enrollButton.setOnClickListener(v -> {
            Course course = viewModel.getSelectedCourse().getValue();
            if (course != null) {
                // 使用AtomicBoolean来在异步操作中存储结果
                AtomicBoolean isAlreadyEnrolled = new AtomicBoolean(false);
                
                // 在后台线程中检查是否已报名
                AppDatabase.databaseWriteExecutor.execute(() -> {
                    // 假设当前用户ID为1，实际应从用户会话获取
                    int currentUserId = 1;
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
}