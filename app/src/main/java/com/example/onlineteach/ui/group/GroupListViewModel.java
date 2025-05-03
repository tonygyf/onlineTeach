package com.example.onlineteach.ui.group;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.onlineteach.data.model.Group;
import com.example.onlineteach.data.repository.GroupRepository;
import com.example.onlineteach.data.repository.UserRepository;

import java.util.ArrayList;
import java.util.List;

public class GroupListViewModel extends AndroidViewModel {
    private GroupRepository groupRepository;
    private UserRepository userRepository;
    private LiveData<List<Group>> groups;

    public GroupListViewModel(Application application) {
        super(application);
        groupRepository = new GroupRepository(application);
        userRepository = new UserRepository(application);
        
        // 获取当前登录用户的ID
        int userId = userRepository.getLoggedInUserId();
        if (userId != -1) {
            // 获取用户加入的所有群组
            groups = groupRepository.getGroupsForUser(userId);
        } else {
            // 如果没有登录用户，返回空列表
            MutableLiveData<List<Group>> emptyGroups = new MutableLiveData<>();
            emptyGroups.setValue(new ArrayList<>());
            groups = emptyGroups;
        }
        
        // 如果数据库中没有数据，可以添加一些测试数据
        // addSampleGroupsIfNeeded();
    }

    public LiveData<List<Group>> getGroups() {
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
        // 这里可以添加一些测试数据
    }
}