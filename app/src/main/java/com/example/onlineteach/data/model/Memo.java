package com.example.onlineteach.data.model;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "memos")
public class Memo {
    @PrimaryKey(autoGenerate = true)
    private int id;
    private String title;
    private String content;
    private long reminderTime;
    private boolean isCompleted;


    public Memo(String title, String content, long reminderTime) {
        this.title = title;
        this.content = content;
        this.reminderTime = reminderTime;
        this.isCompleted = false;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public long getReminderTime() {
        return reminderTime;
    }

    public void setReminderTime(long reminderTime) {
        this.reminderTime = reminderTime;
    }

    public boolean isCompleted() {
        return isCompleted;
    }

    public void setCompleted(boolean completed) {
        isCompleted = completed;
    }
}