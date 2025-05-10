package com.example.onlineteach.ui.home;

public class MenuItem {
    private int iconResId;
    private String title;
    private boolean showBadge;

    public MenuItem(int iconResId, String title) {
        this.iconResId = iconResId;
        this.title = title;
        this.showBadge = false;
    }

    public int getIconResId() {
        return iconResId;
    }

    public String getTitle() {
        return title;
    }

    public boolean isShowBadge() {
        return showBadge;
    }

    public void setShowBadge(boolean showBadge) {
        this.showBadge = showBadge;
    }
}