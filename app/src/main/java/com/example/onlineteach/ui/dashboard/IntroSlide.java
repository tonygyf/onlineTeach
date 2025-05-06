package com.example.onlineteach.ui.dashboard;

public class IntroSlide {

    private final int image;
    private final String title;
    private final String description;

    public IntroSlide(int image, String title, String description) {
        this.image = image;
        this.title = title;
        this.description = description;
    }

    public int getImage() {
        return image;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }
}