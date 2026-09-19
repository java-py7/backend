package com.vendorcoinpay.models;

import com.google.gson.annotations.SerializedName;

public class User {
    @SerializedName("id")
    private int id;

    @SerializedName("user_code")
    private String userCode;

    @SerializedName("full_name")
    private String fullName;

    @SerializedName("username")
    private String username;

    @SerializedName("phone")
    private String phone;

    @SerializedName("is_active")
    private boolean isActive;

    @SerializedName("balance")
    private int balance;

    @SerializedName("created_at")
    private String createdAt;

    public User() {}

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getUserCode() { return userCode != null ? userCode : ""; }
    public void setUserCode(String userCode) { this.userCode = userCode; }

    public String getFullName() { return fullName != null ? fullName : ""; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getUsername() { return username != null ? username : ""; }
    public void setUsername(String username) { this.username = username; }

    public String getPhone() { return phone != null ? phone : ""; }
    public void setPhone(String phone) { this.phone = phone; }

    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { isActive = active; }

    public int getBalance() { return balance; }
    public void setBalance(int balance) { this.balance = balance; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
}
