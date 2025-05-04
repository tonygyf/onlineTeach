package com.example.onlineteach.ui.group;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.onlineteach.R;
import com.example.onlineteach.data.model.Group;
import com.example.onlineteach.data.repository.UserRepository;
import com.example.onlineteach.utils.ToastUtils;

public class GroupChatFragment extends Fragment {

    private static final String TAG = "GroupChatFragment";
    private static final String ARG_GROUP_ID = "group_id";

    private GroupChatViewModel viewModel;
    private RecyclerView recyclerView;
    private EditText messageInput;
    private Button sendButton;
    private Toolbar toolbar;
    private GroupChatAdapter adapter;
    private int groupId;

    public static GroupChatFragment newInstance(int groupId) {
        GroupChatFragment fragment = new GroupChatFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_GROUP_ID, groupId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            groupId = getArguments().getInt(ARG_GROUP_ID);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_group_chat, container, false);
        
        // 初始化视图
        recyclerView = root.findViewById(R.id.recycler_view_messages);
        messageInput = root.findViewById(R.id.edit_text_message);
        sendButton = root.findViewById(R.id.button_send);
        toolbar = root.findViewById(R.id.toolbar_group_chat);
        
        // 设置RecyclerView
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        layoutManager.setStackFromEnd(true); // 从底部开始显示
        recyclerView.setLayoutManager(layoutManager);
        
        return root;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        // 初始化ViewModel
        viewModel = new ViewModelProvider(this,
                ViewModelProvider.AndroidViewModelFactory.getInstance(requireActivity().getApplication()))
                .get(GroupChatViewModel.class);
        
        // 设置群组ID
        viewModel.setGroupId(groupId);
        
        // 初始化适配器
        UserRepository userRepository = new UserRepository(requireContext());
        adapter = new GroupChatAdapter(viewModel.getCurrentUserId(), userRepository);
        recyclerView.setAdapter(adapter);
        
        // 观察群组信息变化
        viewModel.getGroupInfo().observe(getViewLifecycleOwner(), group -> {
            if (group != null) {
                updateToolbar(group);
            }
        });
        
        // 观察消息列表变化
        viewModel.getMessages().observe(getViewLifecycleOwner(), messages -> {
            if (messages != null) {
                adapter.setMessages(messages);
                // 滚动到最新消息
                if (messages.size() > 0) {
                    recyclerView.smoothScrollToPosition(messages.size() - 1);
                }
            }
        });
        
        // 观察错误消息
        viewModel.getErrorMessage().observe(getViewLifecycleOwner(), error -> {
            if (error != null && !error.isEmpty()) {
                ToastUtils.showShortToast(getContext(), error);
            }
        });
        
        // 设置发送按钮点击事件
        sendButton.setOnClickListener(v -> {
            String message = messageInput.getText().toString().trim();
            if (!message.isEmpty()) {
                viewModel.sendMessage(message);
                messageInput.setText(""); // 清空输入框
            }
        });
    }
    
    private void updateToolbar(Group group) {
        toolbar.setTitle(group.getName());
        toolbar.setSubtitle("成员: " + group.getMemberCount());
        
        // 设置返回按钮
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back);
        toolbar.setNavigationOnClickListener(v -> {
            requireActivity().onBackPressed();
        });
    }
}