package com.example.onlineteach.ui.group;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.onlineteach.R;
import com.example.onlineteach.data.model.GroupMessage;
import com.example.onlineteach.data.model.User;
import com.example.onlineteach.data.repository.UserRepository;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class GroupChatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int VIEW_TYPE_MESSAGE_SENT = 1;
    private static final int VIEW_TYPE_MESSAGE_RECEIVED = 2;

    private List<GroupMessage> messages = new ArrayList<>();
    private int currentUserId;
    private UserRepository userRepository;

    public GroupChatAdapter(int currentUserId, UserRepository userRepository) {
        this.currentUserId = currentUserId;
        this.userRepository = userRepository;
    }

    @Override
    public int getItemViewType(int position) {
        GroupMessage message = messages.get(position);

        if (message.getSenderId() == currentUserId) {
            // 如果是当前用户发送的消息
            return VIEW_TYPE_MESSAGE_SENT;
        } else {
            // 如果是其他用户发送的消息
            return VIEW_TYPE_MESSAGE_RECEIVED;
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;

        if (viewType == VIEW_TYPE_MESSAGE_SENT) {
            view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_message_sent, parent, false);
            return new SentMessageHolder(view);
        } else if (viewType == VIEW_TYPE_MESSAGE_RECEIVED) {
            view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_message_received, parent, false);
            return new ReceivedMessageHolder(view);
        }

        return null;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        GroupMessage message = messages.get(position);

        switch (holder.getItemViewType()) {
            case VIEW_TYPE_MESSAGE_SENT:
                ((SentMessageHolder) holder).bind(message);
                break;
            case VIEW_TYPE_MESSAGE_RECEIVED:
                ((ReceivedMessageHolder) holder).bind(message);
                break;
        }
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    public void setMessages(List<GroupMessage> messages) {
        this.messages = messages;
        notifyDataSetChanged();
    }

    public void addMessage(GroupMessage message) {
        this.messages.add(message);
        notifyItemInserted(messages.size() - 1);
    }

    private class SentMessageHolder extends RecyclerView.ViewHolder {
        TextView messageText, timeText;

        SentMessageHolder(View itemView) {
            super(itemView);

            messageText = itemView.findViewById(R.id.text_message_body);
            timeText = itemView.findViewById(R.id.text_message_time);
        }

        void bind(GroupMessage message) {
            messageText.setText(message.getContent());

            // 格式化时间
            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
            timeText.setText(sdf.format(new Date(message.getSendTime())));
        }
    }

    private class ReceivedMessageHolder extends RecyclerView.ViewHolder {
        TextView messageText, timeText, nameText;
        ImageView profileImage;

        ReceivedMessageHolder(View itemView) {
            super(itemView);

            messageText = itemView.findViewById(R.id.text_message_body);
            timeText = itemView.findViewById(R.id.text_message_time);
            nameText = itemView.findViewById(R.id.text_message_name);
            profileImage = itemView.findViewById(R.id.image_message_profile);
        }

        void bind(GroupMessage message) {
            messageText.setText(message.getContent());

            // 格式化时间
            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
            timeText.setText(sdf.format(new Date(message.getSendTime())));

            // 获取发送者信息
            User sender = userRepository.getUserById(message.getSenderId());
            if (sender != null) {
                nameText.setText(sender.getUserName());
                // 如果有用户头像，可以在这里设置
            }
        }
    }
}