package com.example.onlineteach.data.model;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;
import androidx.room.ColumnInfo;

@Entity(tableName = "group_members",
        foreignKeys = {
                @ForeignKey(entity = User.class,
                        parentColumns = "uid",
                        childColumns = "user_id",
                        onDelete = ForeignKey.CASCADE),
                @ForeignKey(entity = Group.class,
                        parentColumns = "groupId",
                        childColumns = "group_id",
                        onDelete = ForeignKey.CASCADE)
        },
        indices = {
                @Index(value = {"user_id", "group_id"}, unique = true)
        })
public class GroupMember {
    @PrimaryKey(autoGenerate = true)
    private int id;

    @ColumnInfo(name = "user_id")
    private int userId;

    @ColumnInfo(name = "group_id")
    private int groupId;

    @ColumnInfo(name = "join_time")
    private long joinTime;

    @ColumnInfo(name = "is_admin")
    private boolean isAdmin;

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public int getGroupId() {
        return groupId;
    }

    public void setGroupId(int groupId) {
        this.groupId = groupId;
    }

    public long getJoinTime() {
        return joinTime;
    }

    public void setJoinTime(long joinTime) {
        this.joinTime = joinTime;
    }

    public boolean isAdmin() {
        return isAdmin;
    }

    public void setAdmin(boolean admin) {
        isAdmin = admin;
    }
}