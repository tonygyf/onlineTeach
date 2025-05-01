package com.example.onlineteach.ui.course;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import com.example.onlineteach.utils.ToastUtils;
import androidx.recyclerview.widget.RecyclerView;

import com.example.onlineteach.R;
import com.example.onlineteach.data.model.Course;

import java.util.ArrayList;
import java.util.List;

public class CourseListAdapter extends RecyclerView.Adapter<CourseListAdapter.CourseViewHolder> {

    private List<Course> courses = new ArrayList<>();

    @NonNull
    @Override
    public CourseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_course, parent, false);
        return new CourseViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CourseViewHolder holder, int position) {
        Course course = courses.get(position);
        holder.bind(course);
    }

    @Override
    public int getItemCount() {
        return courses.size();
    }

    public void setCourses(List<Course> courses) {
        this.courses = courses;
        notifyDataSetChanged();
    }

    static class CourseViewHolder extends RecyclerView.ViewHolder {
        private final ImageView imageView;
        private final TextView titleView;
        private final TextView teacherView;
        private final TextView creditsView;
        private final Button enrollButton;

        public CourseViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.image_course);
            titleView = itemView.findViewById(R.id.text_course_title);
            teacherView = itemView.findViewById(R.id.text_course_teacher);
            creditsView = itemView.findViewById(R.id.text_course_credits);
            enrollButton = itemView.findViewById(R.id.button_enroll);
        }

        public void bind(Course course) {
            titleView.setText(course.getTitle());
            teacherView.setText(course.getTeacher());
            creditsView.setText(String.format("学分: %.1f", course.getCredits()));
            
            enrollButton.setOnClickListener(v -> {
                ToastUtils.showShortToast(v.getContext(), 
                    "报名课程: " + course.getTitle());
            });
        }
    }
}