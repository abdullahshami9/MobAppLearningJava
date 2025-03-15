package com.example.firstapp301124;

/**
 * Model class for folders
 */
public class Folder {
    private int id;
    private int userId;
    private String name;
    private int colorId;
    private String createdAt;
    private String updatedAt;

    public Folder(int id, int userId, String name, int colorId, String createdAt, String updatedAt) {
        this.id = id;
        this.userId = userId;
        this.name = name;
        this.colorId = colorId;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    // Getters and setters
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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getColorId() {
        return colorId;
    }

    public void setColorId(int colorId) {
        this.colorId = colorId;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(String updatedAt) {
        this.updatedAt = updatedAt;
    }
} 