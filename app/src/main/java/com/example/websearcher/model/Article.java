package com.example.websearcher.model;

public class Article {
    private String id;
    private String url;
    private String userId;
    private String title;
    public String iconUrl;
    public int readingTime; // reading time in minutes
    private boolean isRead; // true if the article has been read

    public Article() {
    }

    public Article(String id, String userId, String url, String title, String imageUrl, int readingTime, boolean isRead) {
        this.id = id;
        this.userId = userId;
        this.url = url;
        this.title = title;
        this.iconUrl = imageUrl;
        this.readingTime = readingTime;
        this.isRead = isRead;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
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
