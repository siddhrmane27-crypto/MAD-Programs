package com.example.lostandfounditems.models;

public class ItemModel {
    private String itemId;
    private String title;
    private String description;
    private String category;
    private String type; // "Lost" or "Found"
    private String location;
    private String imageUrl;
    private String userId;
    private String userEmail;
    private long timestamp;
    private boolean resolved; // ✅ Mark as resolved/returned
    private boolean verified; // ✅ Admin verification status

    public ItemModel() {}

    public ItemModel(String itemId, String title, String description, String category,
                     String type, String location, String imageUrl, String userId,
                     String userEmail, long timestamp) {
        this.itemId = itemId;
        this.title = title;
        this.description = description;
        this.category = category;
        this.type = type;
        this.location = location;
        this.imageUrl = imageUrl;
        this.userId = userId;
        this.userEmail = userEmail;
        this.timestamp = timestamp;
        this.resolved = false;
        this.verified = false; // Default to unverified
    }

    public String getItemId() { return itemId; }
    public void setItemId(String itemId) { this.itemId = itemId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getUserEmail() { return userEmail; }
    public void setUserEmail(String userEmail) { this.userEmail = userEmail; }

    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }

    public boolean isResolved() { return resolved; }
    public void setResolved(boolean resolved) { this.resolved = resolved; }

    public boolean isVerified() { return verified; }
    public void setVerified(boolean verified) { this.verified = verified; }
}
