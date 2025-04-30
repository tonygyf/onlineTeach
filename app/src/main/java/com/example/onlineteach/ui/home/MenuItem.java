package com.example.onlineteach.ui.home;

public class MenuItem {
    private int iconResId;
    private String title;

    public MenuItem(int iconResId, String title) {
        this.iconResId = iconResId;
        this.title = title;
    }

    public int getIconResId() {
        return iconResId;
    }

    public String getTitle() {
        return title;
    }
}