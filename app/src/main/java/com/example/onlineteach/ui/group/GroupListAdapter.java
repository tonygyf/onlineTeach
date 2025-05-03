package com.example.onlineteach.ui.group;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.onlineteach.R;
import com.example.onlineteach.data.model.Group;

import java.util.ArrayList;
import java.util.List;

public class GroupListAdapter extends RecyclerView.Adapter<GroupListAdapter.GroupViewHolder> {

    private List<Group> groups = new ArrayList<>();
    private final OnGroupClickListener listener;

    public interface OnGroupClickListener {
        void onGroupClick(Group group);
    }

    public GroupListAdapter(OnGroupClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public GroupViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_group, parent, false);
        return new GroupViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull GroupViewHolder holder, int position) {
        Group group = groups.get(position);
        holder.bind(group, listener);
    }

    @Override
    public int getItemCount() {
        return groups.size();
    }

    public void setGroups(List<Group> groups) {
        this.groups = groups;
        notifyDataSetChanged();
    }

    static class GroupViewHolder extends RecyclerView.ViewHolder {
        private final ImageView imageView;
        private final TextView nameView;
        private final TextView descriptionView;
        private final TextView memberCountView;

        public GroupViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.image_group);
            nameView = itemView.findViewById(R.id.text_group_name);
            descriptionView = itemView.findViewById(R.id.text_group_description);
            memberCountView = itemView.findViewById(R.id.text_member_count);
        }

        public void bind(Group group, OnGroupClickListener listener) {
            nameView.setText(group.getName());
            descriptionView.setText(group.getDescription());
            memberCountView.setText(String.format("成员: %d", group.getMemberCount()));
            
            // 设置点击事件
            itemView.setOnClickListener(v -> {
                listener.onGroupClick(group);
            });
        }
    }
}