package com.example.onlineteach.ui.group;

import android.app.Application;
import android.util.Log;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.onlineteach.data.model.Group;
import com.example.onlineteach.data.model.GroupMessage;
import com.example.onlineteach.data.model.User;
import com.example.onlineteach.data.repository.GroupRepository;
import com.example.onlineteach.data.repository.UserRepository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class GroupChatViewModel extends AndroidViewModel {
    private static final String TAG = "GroupChatViewModel";
    
    private GroupRepository groupRepository;
    private UserRepository userRepository;
    private int groupId;
    private LiveData<List<GroupMessage>> messages;
    private MutableLiveData<String> errorMessage = new MutableLiveData<>();
    private MutableLiveData<Group> groupInfo = new MutableLiveData<>();
    private Map<Integer, MutableLiveData<User>> userCache = new HashMap<>();
    private ExecutorService executorService = Executors.newSingleThreadExecutor();

    public GroupChatViewModel(Application application) {
        super(application);
        groupRepository = new GroupRepository(application);
        userRepository = new UserRepository(application);
    }
    
    @Override
    protected void onCleared() {
        super.onCleared();
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdown();
        }
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
    
    /**
     * 获取用户信息的LiveData
     * @param userId 用户ID
     * @return 包含用户信息的LiveData
     */
    public LiveData<User> getUserInfo(int userId) {
        // 检查缓存中是否已有该用户的LiveData
        if (!userCache.containsKey(userId)) {
            // 创建新的LiveData并添加到缓存
            MutableLiveData<User> userData = new MutableLiveData<>();
            userCache.put(userId, userData);
            
            // 在后台线程中加载用户数据
            loadUserData(userId, userData);
        }
        return userCache.get(userId);
    }
    
    /**
     * 在后台线程中加载用户数据
     */
    private void loadUserData(int userId, MutableLiveData<User> userData) {
        executorService.execute(() -> {
            try {
                // 从数据库获取用户信息
                User user = userRepository.getUserById(userId);
                // 在主线程中更新LiveData
                userData.postValue(user);
            } catch (Exception e) {
                Log.e(TAG, "加载用户数据失败: " + e.getMessage());
                errorMessage.postValue("加载用户数据失败");
            }
        });
    }
}