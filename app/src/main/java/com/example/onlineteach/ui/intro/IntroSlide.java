package com.example.onlineteach.ui.intro;

public class IntroSlide {
    private final String lottieFileName;
    private final String title;
    private final String description;

    public IntroSlide(String lottieFileName, String title, String description) {
        this.lottieFileName = lottieFileName;
        this.title = title;
        this.description = description;
    }

    public String getLottieFileName() {
        return lottieFileName;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }
}