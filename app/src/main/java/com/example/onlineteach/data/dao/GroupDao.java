package com.example.onlineteach.data.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;
import androidx.room.Delete;
import androidx.room.OnConflictStrategy;

import com.example.onlineteach.data.model.Group;
import com.example.onlineteach.data.model.GroupMember;
import com.example.onlineteach.data.model.GroupMessage;

import java.util.List;

@Dao
public interface GroupDao {
    // 群组相关操作
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insertGroup(Group group);

    @Update
    int updateGroup(Group group);

    @Delete
    void deleteGroup(Group group);

    @Query("SELECT * FROM groups")
    LiveData<List<Group>> getAllGroups();

    @Query("SELECT * FROM groups WHERE groupId = :groupId LIMIT 1")
    Group getGroupById(int groupId);

    @Query("SELECT * FROM groups WHERE creator_id = :userId")
    LiveData<List<Group>> getGroupsByCreator(int userId);

    // 群组成员相关操作
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insertGroupMember(GroupMember member);

    @Delete
    void deleteGroupMember(GroupMember member);

    @Query("SELECT * FROM group_members WHERE group_id = :groupId")
    LiveData<List<GroupMember>> getGroupMembers(int groupId);

    @Query("SELECT * FROM group_members WHERE user_id = :userId")
    LiveData<List<GroupMember>> getUserGroups(int userId);

    @Query("SELECT g.* FROM groups g INNER JOIN group_members gm ON g.groupId = gm.group_id WHERE gm.user_id = :userId")
    LiveData<List<Group>> getGroupsForUser(int userId);

    @Query("SELECT COUNT(*) FROM group_members WHERE group_id = :groupId")
    int getGroupMemberCount(int groupId);

    @Query("SELECT EXISTS(SELECT 1 FROM group_members WHERE group_id = :groupId AND user_id = :userId LIMIT 1)")
    boolean isUserInGroup(int groupId, int userId);

    // 群组消息相关操作
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insertGroupMessage(GroupMessage message);

    @Query("SELECT * FROM group_messages WHERE group_id = :groupId ORDER BY send_time DESC")
    LiveData<List<GroupMessage>> getGroupMessages(int groupId);

    @Query("SELECT * FROM group_messages WHERE group_id = :groupId ORDER BY send_time DESC LIMIT :limit")
    LiveData<List<GroupMessage>> getRecentGroupMessages(int groupId, int limit);

}