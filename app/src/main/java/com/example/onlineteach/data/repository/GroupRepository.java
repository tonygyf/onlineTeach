package com.example.onlineteach.data.repository;

import android.content.Context;
import android.util.Log;

import androidx.lifecycle.LiveData;

import com.example.onlineteach.data.dao.GroupDao;
import com.example.onlineteach.data.database.AppDatabase;
import com.example.onlineteach.data.model.Group;
import com.example.onlineteach.data.model.GroupMember;
import com.example.onlineteach.data.model.GroupMessage;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class GroupRepository {
    private static final String TAG = "GroupRepository";

    private GroupDao groupDao;
    private ExecutorService executorService;

    public GroupRepository(Context context) {
        AppDatabase db = AppDatabase.getDatabase(context);
        groupDao = db.groupDao();
        executorService = Executors.newSingleThreadExecutor();
    }

    // 群组相关方法
    public LiveData<List<Group>> getAllGroups() {
        return groupDao.getAllGroups();
    }

    public LiveData<List<Group>> getGroupsByCreator(int userId) {
        return groupDao.getGroupsByCreator(userId);
    }

    public LiveData<List<Group>> getGroupsForUser(int userId) {
        return groupDao.getGroupsForUser(userId);
    }

    public void createGroup(Group group, GroupOperationCallback callback) {
        executorService.execute(() -> {
            try {
                long groupId = groupDao.insertGroup(group);
                if (groupId > 0) {
                    // 创建成功，将创建者添加为管理员
                    GroupMember creator = new GroupMember();
                    creator.setGroupId((int) groupId);
                    creator.setUserId(group.getCreatorId());
                    creator.setJoinTime(System.currentTimeMillis());
                    creator.setAdmin(true);
                    
                    long memberId = groupDao.insertGroupMember(creator);
                    if (memberId > 0) {
                        group.setGroupId((int) groupId);
                        callback.onSuccess(group);
                    } else {
                        callback.onError("创建群组成功但添加创建者失败");
                    }
                } else {
                    callback.onError("创建群组失败");
                }
            } catch (Exception e) {
                Log.e(TAG, "Error creating group: " + e.getMessage());
                callback.onError("创建群组过程中发生错误");
            }
        });
    }

    public void joinGroup(int groupId, int userId, GroupOperationCallback callback) {
        executorService.execute(() -> {
            try {
                // 检查群组是否存在
                Group group = groupDao.getGroupById(groupId);
                if (group == null) {
                    callback.onError("群组不存在");
                    return;
                }

                // 检查用户是否已在群组中
                boolean isInGroup = groupDao.isUserInGroup(groupId, userId);
                if (isInGroup) {
                    callback.onError("您已经在该群组中");
                    return;
                }

                // 添加用户到群组
                GroupMember member = new GroupMember();
                member.setGroupId(groupId);
                member.setUserId(userId);
                member.setJoinTime(System.currentTimeMillis());
                member.setAdmin(false);

                long memberId = groupDao.insertGroupMember(member);
                if (memberId > 0) {
                    // 更新群组成员数量
                    int memberCount = groupDao.getGroupMemberCount(groupId);
                    group.setMemberCount(memberCount);
                    groupDao.updateGroup(group);
                    
                    callback.onSuccess(group);
                } else {
                    callback.onError("加入群组失败");
                }
            } catch (Exception e) {
                Log.e(TAG, "Error joining group: " + e.getMessage());
                callback.onError("加入群组过程中发生错误");
            }
        });
    }

    public void leaveGroup(int groupId, int userId, GroupOperationCallback callback) {
        executorService.execute(() -> {
            try {
                // 检查群组是否存在
                Group group = groupDao.getGroupById(groupId);
                if (group == null) {
                    callback.onError("群组不存在");
                    return;
                }

                // 检查用户是否在群组中
                boolean isInGroup = groupDao.isUserInGroup(groupId, userId);
                if (!isInGroup) {
                    callback.onError("您不在该群组中");
                    return;
                }

                // 如果是创建者，不允许退出
                if (group.getCreatorId() == userId) {
                    callback.onError("群主不能退出群组，请先转让群主或解散群组");
                    return;
                }

                // TODO: 实现退出群组的逻辑
                // 这里需要先查询到对应的GroupMember对象，然后删除
                // 由于当前DAO没有提供根据userId和groupId查询GroupMember的方法，暂时略过
                
                // 更新群组成员数量
                int memberCount = groupDao.getGroupMemberCount(groupId);
                group.setMemberCount(memberCount - 1);
                groupDao.updateGroup(group);
                
                callback.onSuccess(group);
            } catch (Exception e) {
                Log.e(TAG, "Error leaving group: " + e.getMessage());
                callback.onError("退出群组过程中发生错误");
            }
        });
    }

    // 群组消息相关方法
    public LiveData<List<GroupMessage>> getGroupMessages(int groupId) {
        return groupDao.getGroupMessages(groupId);
    }

    public LiveData<List<GroupMessage>> getRecentGroupMessages(int groupId, int limit) {
        return groupDao.getRecentGroupMessages(groupId, limit);
    }
    
    public void getGroupById(int groupId, GroupOperationCallback callback) {
        executorService.execute(() -> {
            try {
                Group group = groupDao.getGroupById(groupId);
                if (group != null) {
                    callback.onSuccess(group);
                } else {
                    callback.onError("群组不存在");
                }
            } catch (Exception e) {
                Log.e(TAG, "Error getting group by id: " + e.getMessage());
                callback.onError("获取群组信息过程中发生错误");
            }
        });
    }

    public void sendMessage(GroupMessage message, MessageCallback callback) {
        executorService.execute(() -> {
            try {
                // 检查群组是否存在
                Group group = groupDao.getGroupById(message.getGroupId());
                if (group == null) {
                    callback.onError("群组不存在");
                    return;
                }

                // 检查用户是否在群组中
                boolean isInGroup = groupDao.isUserInGroup(message.getGroupId(), message.getSenderId());
                if (!isInGroup) {
                    callback.onError("您不在该群组中，无法发送消息");
                    return;
                }

                // 发送消息
                long messageId = groupDao.insertGroupMessage(message);
                if (messageId > 0) {
                    message.setMessageId((int) messageId);
                    callback.onSuccess(message);
                } else {
                    callback.onError("发送消息失败");
                }
            } catch (Exception e) {
                Log.e(TAG, "Error sending message: " + e.getMessage());
                callback.onError("发送消息过程中发生错误");
            }
        });
    }

    // 回调接口
    public interface GroupOperationCallback {
        void onSuccess(Group group);
        void onError(String errorMessage);
    }

    public interface MessageCallback {
        void onSuccess(GroupMessage message);
        void onError(String errorMessage);
    }
}