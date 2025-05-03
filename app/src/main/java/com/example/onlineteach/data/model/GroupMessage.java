package com.example.onlineteach.data.model;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;
import androidx.room.ColumnInfo;

@Entity(tableName = "group_messages",
        foreignKeys = {
                @ForeignKey(entity = User.class,
                        parentColumns = "uid",
                        childColumns = "sender_id",
                        onDelete = ForeignKey.CASCADE),
                @ForeignKey(entity = Group.class,
                        parentColumns = "groupId",
                        childColumns = "group_id",
                        onDelete = ForeignKey.CASCADE)
        },
        indices = {
                @Index(value = {"sender_id"}),
                @Index(value = {"group_id"})
        })
public class GroupMessage {
    @PrimaryKey(autoGenerate = true)
    private int messageId;

    @ColumnInfo(name = "group_id")
    private int groupId;

    @ColumnInfo(name = "sender_id")
    private int senderId;

    @ColumnInfo(name = "content")
    private String content;

    @ColumnInfo(name = "send_time")
    private long sendTime;

    @ColumnInfo(name = "message_type")
    private int messageType; // 0: 文本, 1: 图片, 2: 文件等

    // Getters and Setters
    public int getMessageId() {
        return messageId;
    }

    public void setMessageId(int messageId) {
        this.messageId = messageId;
    }

    public int getGroupId() {
        return groupId;
    }

    public void setGroupId(int groupId) {
        this.groupId = groupId;
    }

    public int getSenderId() {
        return senderId;
    }

    public void setSenderId(int senderId) {
        this.senderId = senderId;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public long getSendTime() {
        return sendTime;
    }

    public void setSendTime(long sendTime) {
        this.sendTime = sendTime;
    }

    public int getMessageType() {
        return messageType;
    }

    public void setMessageType(int messageType) {
        this.messageType = messageType;
    }
}