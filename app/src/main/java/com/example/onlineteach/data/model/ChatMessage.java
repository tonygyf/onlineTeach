package com.example.onlineteach.data.model;


public class ChatMessage {
    private String content;
    private boolean fromUser;
    private long timestamp;

    public ChatMessage(String content, boolean fromUser) {
        this.content = content;
        this.fromUser = fromUser;
        this.timestamp = System.currentTimeMillis();
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public boolean isFromUser() {
        return fromUser;
    }

    public void setFromUser(boolean fromUser) {
        this.fromUser = fromUser;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}