package com.example.lostandfounditems.models;

public class UserModel {
    private String userId;
    private String name;
    private String email;
    private boolean isAdmin; // Admin role field

    public UserModel() {}

    public UserModel(String userId, String name, String email) {
        this.userId = userId;
        this.name = name;
        this.email = email;
        this.isAdmin = false; // Default to regular user
    }

    public UserModel(String userId, String name, String email, boolean isAdmin) {
        this.userId = userId;
        this.name = name;
        this.email = email;
        this.isAdmin = isAdmin;
    }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public boolean isAdmin() { return isAdmin; }
    public void setAdmin(boolean admin) { isAdmin = admin; }
}
