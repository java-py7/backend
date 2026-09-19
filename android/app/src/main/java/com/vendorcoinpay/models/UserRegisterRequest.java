package com.vendorcoinpay.models;

import com.google.gson.annotations.SerializedName;

public class UserRegisterRequest {
    @SerializedName("full_name")
    private String fullName;

    @SerializedName("username")
    private String username;

    @SerializedName("phone")
    private String phone;

    @SerializedName("password")
    private String password;

    @SerializedName("confirm_password")
    private String confirmPassword;

    public UserRegisterRequest(String fullName, String username, String phone, String password, String confirmPassword) {
        this.fullName = fullName;
        this.username = username;
        this.phone = phone;
        this.password = password;
        this.confirmPassword = confirmPassword;
    }
}
