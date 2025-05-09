package com.example.onlineteach.ui.dashboard;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.onlineteach.data.model.Enrollment;
import com.example.onlineteach.databinding.ItemEnrolledCourseBinding;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class EnrolledCoursesAdapter extends RecyclerView.Adapter<EnrolledCoursesAdapter.ViewHolder> {
    private List<Enrollment> enrollments;
    private final SimpleDateFormat dateFormat;

    public EnrolledCoursesAdapter() {
        dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
    }

    public void setEnrollments(List<Enrollment> enrollments) {
        this.enrollments = enrollments;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemEnrolledCourseBinding binding = ItemEnrolledCourseBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Enrollment enrollment = enrollments.get(position);
        holder.binding.textViewCourseName.setText("课程ID: " + enrollment.getCourseId());
        holder.binding.textViewEnrollmentDate.setText("选课时间: " + 
                dateFormat.format(new Date(enrollment.getEnrollmentDate())));
    }

    @Override
    public int getItemCount() {
        return enrollments != null ? enrollments.size() : 0;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        private final ItemEnrolledCourseBinding binding;

        ViewHolder(ItemEnrolledCourseBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}