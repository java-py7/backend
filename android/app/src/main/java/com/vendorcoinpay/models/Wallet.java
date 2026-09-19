package com.vendorcoinpay.models;

import com.google.gson.annotations.SerializedName;

public class Wallet {
    @SerializedName("user_id")
    private int userId;

    @SerializedName("user_code")
    private String userCode;

    @SerializedName("balance")
    private int balance;

    @SerializedName("currency")
    private String currency;

    @SerializedName("updated_at")
    private String updatedAt;

    public Wallet() {}

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public String getUserCode() { return userCode; }
    public void setUserCode(String userCode) { this.userCode = userCode; }

    public int getBalance() { return balance; }
    public void setBalance(int balance) { this.balance = balance; }

    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }

    public String getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(String updatedAt) { this.updatedAt = updatedAt; }
}
