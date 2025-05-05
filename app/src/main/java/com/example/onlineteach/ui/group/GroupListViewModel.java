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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class GroupListViewModel extends AndroidViewModel {
    private static final String TAG = "GroupListViewModel";
    private GroupRepository groupRepository;
    private UserRepository userRepository;
    private LiveData<List<Group>> groups;
    private MutableLiveData<String> toastMessage = new MutableLiveData<>(); // 修改：使用 MutableLiveData
    private ExecutorService executorService;

    // 添加获取UserRepository的方法，供Fragment使用
    public UserRepository getUserRepository() {
        return userRepository;
    }

    public GroupListViewModel(Application application) {
        super(application);
        groupRepository = new GroupRepository(application);
        userRepository = new UserRepository(application);
        executorService = Executors.newSingleThreadExecutor();

        // 获取所有群组，而不是只获取用户加入的群组
        groups = groupRepository.getAllGroups();
        Log.d(TAG, "已获取所有群组LiveData");

        // 添加测试数据（如果需要测试）
        addSampleGroupsIfNeeded();
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        // 关闭ExecutorService，避免内存泄漏
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdown();
        }
    }

    public LiveData<List<Group>> getGroups() {
        Log.d(TAG, "获取群组LiveData");
        return groups;
    }

    public LiveData<String> getToastMessage() {
        return toastMessage;
    }
    
    /**
     * 清除Toast消息
     */
    public void clearToastMessage() {
        toastMessage.postValue(null);
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
                    toastMessage.postValue(errorMessage);
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
                    toastMessage.postValue(errorMessage);
                }
            });
        }
    }

    /**
     * 检查用户是否在群组中，如果不是则自动加入
     *
     * @param groupId  群组ID
     * @param callback 操作结果回调
     */
    public void checkAndJoinGroup(int groupId, GroupRepository.GroupOperationCallback callback) {
        int userId = userRepository.getLoggedInUserId();
        if (userId == -1) {
            callback.onError("用户未登录");
            return;
        }

        // 先获取群组信息
        groupRepository.getGroupById(groupId, new GroupRepository.GroupOperationCallback() {
            @Override
            public void onSuccess(Group group) {
                // 检查用户是否已在群组中
                groupRepository.isUserInGroup(groupId, userId, new GroupRepository.UserInGroupCallback() {
                    @Override
                    public void onResult(boolean isInGroup) {
                        if (isInGroup) {
                            // 用户已在群组中，直接回调成功
                            callback.onSuccess(group);
                        } else {
                            // 用户不在群组中，自动加入
                            Log.d(TAG, "用户不在群组中，自动加入群组: " + groupId);
                            groupRepository.joinGroup(groupId, userId, new GroupRepository.GroupOperationCallback() {
                                @Override
                                public void onSuccess(Group updatedGroup) {
                                    Log.d(TAG, "自动加入群组成功: " + updatedGroup.getName());
                                    callback.onSuccess(updatedGroup);
                                }

                                @Override
                                public void onError(String errorMessage) {
                                    Log.e(TAG, "自动加入群组失败: " + errorMessage);
                                    toastMessage.postValue(errorMessage);
                                    callback.onError(errorMessage);
                                }
                            });
                        }
                    }
                    
                    @Override
                    public void onError(String errorMessage) {
                        Log.e(TAG, "检查用户是否在群组中时发生错误: " + errorMessage);
                        toastMessage.postValue("检查群组成员状态时发生错误: " + errorMessage);
                        callback.onError(errorMessage);
                    }
                });
            }

            @Override
            public void onError(String errorMessage) {
                Log.e(TAG, "获取群组信息失败: " + errorMessage);
                toastMessage.postValue(errorMessage);
            }
        });
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