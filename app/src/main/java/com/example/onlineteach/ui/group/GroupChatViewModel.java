package com.example.onlineteach.ui.group;

import android.app.Application;
import android.util.Log;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.onlineteach.data.model.Group;
import com.example.onlineteach.data.model.GroupMessage;
import com.example.onlineteach.data.repository.GroupRepository;
import com.example.onlineteach.data.repository.UserRepository;

import java.util.List;

public class GroupChatViewModel extends AndroidViewModel {
    private static final String TAG = "GroupChatViewModel";
    
    private GroupRepository groupRepository;
    private UserRepository userRepository;
    private int groupId;
    private LiveData<List<GroupMessage>> messages;
    private MutableLiveData<String> errorMessage = new MutableLiveData<>();
    private MutableLiveData<Group> groupInfo = new MutableLiveData<>();

    public GroupChatViewModel(Application application) {
        super(application);
        groupRepository = new GroupRepository(application);
        userRepository = new UserRepository(application);
    }

    public void setGroupId(int groupId) {
        this.groupId = groupId;
        // 加载群组信息
        loadGroupInfo();
        // 加载群组消息
        messages = groupRepository.getGroupMessages(groupId);
    }

    private void loadGroupInfo() {
        groupRepository.getGroupById(groupId, new GroupRepository.GroupOperationCallback() {
            @Override
            public void onSuccess(Group group) {
                groupInfo.postValue(group);
            }

            @Override
            public void onError(String error) {
                errorMessage.postValue("获取群组信息失败: " + error);
            }
        });
    }

    public LiveData<List<GroupMessage>> getMessages() {
        return messages;
    }

    public LiveData<Group> getGroupInfo() {
        return groupInfo;
    }

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    public void sendMessage(String content) {
        int currentUserId = userRepository.getLoggedInUserId();
        if (currentUserId == -1) {
            errorMessage.postValue("您需要登录才能发送消息");
            return;
        }

        GroupMessage message = new GroupMessage();
        message.setGroupId(groupId);
        message.setSenderId(currentUserId);
        message.setContent(content);
        message.setSendTime(System.currentTimeMillis());
        message.setMessageType(0); // 文本消息

        groupRepository.sendMessage(message, new GroupRepository.MessageCallback() {
            @Override
            public void onSuccess(GroupMessage message) {
                // 消息发送成功，LiveData会自动更新
                Log.d(TAG, "消息发送成功: " + message.getContent());
            }

            @Override
            public void onError(String error) {
                errorMessage.postValue("发送消息失败: " + error);
            }
        });
    }

    public int getCurrentUserId() {
        return userRepository.getLoggedInUserId();
    }
}