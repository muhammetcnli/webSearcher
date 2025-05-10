package com.example.websearcher.model;

public class Article {
    public String url;
    public String title;
    public String iconUrl;
    public int readingTime; // reading time in minutes
    private boolean isRead; // true if the article has been read

    // 🔥 Firebase için boş constructor gerekiyor
    public Article() {
    }

    public Article(String url, String title, String imageUrl, int readingTime, boolean isRead) {
        this.url = url;
        this.title = title;
        this.iconUrl = imageUrl;
        this.readingTime = readingTime;
        this.isRead = isRead;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public boolean isRead() {
        return isRead;
    }

    public void setRead(boolean read) {
        isRead = read;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getIconUrl() {
        return iconUrl;
    }

    public void setIconUrl(String iconUrl) {
        this.iconUrl = iconUrl;
    }

    public int getReadingTime() {
        return readingTime;
    }

    public void setReadingTime(int readingTime) {
        this.readingTime = readingTime;
    }
}
