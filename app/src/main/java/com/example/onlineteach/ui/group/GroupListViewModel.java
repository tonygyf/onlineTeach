package com.example.onlineteach.ui.group;

import android.app.Application;
import android.util.Log;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.onlineteach.data.model.Group;
import com.example.onlineteach.data.repository.GroupRepository;
import com.example.onlineteach.data.repository.UserRepository;

import java.util.ArrayList;
import java.util.List;

public class GroupListViewModel extends AndroidViewModel {
    private static final String TAG = "GroupListViewModel";
    private GroupRepository groupRepository;
    private UserRepository userRepository;
    private LiveData<List<Group>> groups;
    private MutableLiveData<String> errorMessage = new MutableLiveData<>();

    public GroupListViewModel(Application application) {
        super(application);
        groupRepository = new GroupRepository(application);
        userRepository = new UserRepository(application);
        
        // 获取当前登录用户的ID
        int userId = userRepository.getLoggedInUserId();
        Log.d(TAG, "当前登录用户ID: " + userId);
        
        if (userId != -1) {
            // 获取用户加入的所有群组
            groups = groupRepository.getGroupsForUser(userId);
            Log.d(TAG, "已获取用户群组LiveData");
        } else {
            // 如果没有登录用户，返回空列表
            MutableLiveData<List<Group>> emptyGroups = new MutableLiveData<>();
            emptyGroups.setValue(new ArrayList<>());
            groups = emptyGroups;
            Log.d(TAG, "用户未登录，返回空群组列表");
        }
        
        // 添加测试数据（如果需要测试）
        addSampleGroupsIfNeeded();
    }

    public LiveData<List<Group>> getGroups() {
        Log.d(TAG, "获取群组LiveData");
        return groups;
    }

    public void createGroup(String name, String description) {
        int userId = userRepository.getLoggedInUserId();
        if (userId != -1) {
            Group newGroup = new Group();
            newGroup.setName(name);
            newGroup.setDescription(description);
            newGroup.setCreatorId(userId);
            newGroup.setCreateTime(System.currentTimeMillis());
            newGroup.setMemberCount(1); // 创建者自己
            
            groupRepository.createGroup(newGroup, new GroupRepository.GroupOperationCallback() {
                @Override
                public void onSuccess(Group group) {
                    // 群组创建成功，LiveData会自动更新
                }

                @Override
                public void onError(String errorMessage) {
                    // 处理错误
                }
            });
        }
    }

    public void joinGroup(int groupId) {
        int userId = userRepository.getLoggedInUserId();
        if (userId != -1) {
            groupRepository.joinGroup(groupId, userId, new GroupRepository.GroupOperationCallback() {
                @Override
                public void onSuccess(Group group) {
                    // 加入群组成功，LiveData会自动更新
                }

                @Override
                public void onError(String errorMessage) {
                    // 处理错误
                }
            });
        }
    }

    // 添加测试数据的方法，实际开发中可以删除
    private void addSampleGroupsIfNeeded() {
        // 检查当前用户是否已登录
        int userId = userRepository.getLoggedInUserId();
        if (userId == -1) {
            Log.d(TAG, "未登录用户，不添加测试数据");
            return;
        }
        
        // 检查是否已有群组数据
        groups.observeForever(groupList -> {
            if (groupList == null || groupList.isEmpty()) {
                Log.d(TAG, "群组列表为空，添加测试数据");
                // 创建测试群组
                Group testGroup = new Group();
                testGroup.setName("测试群组");
                testGroup.setDescription("这是一个测试群组，用于验证群组列表功能");
                testGroup.setCreatorId(userId);
                testGroup.setCreateTime(System.currentTimeMillis());
                testGroup.setMemberCount(1);
                
                // 添加到数据库
                groupRepository.createGroup(testGroup, new GroupRepository.GroupOperationCallback() {
                    @Override
                    public void onSuccess(Group group) {
                        Log.d(TAG, "测试群组创建成功: " + group.getName());
                    }
                    
                    @Override
                    public void onError(String errorMessage) {
                        Log.e(TAG, "测试群组创建失败: " + errorMessage);
                    }
                });
            } else {
                Log.d(TAG, "已有群组数据，不添加测试数据，当前群组数量: " + groupList.size());
            }
        });
    }
}