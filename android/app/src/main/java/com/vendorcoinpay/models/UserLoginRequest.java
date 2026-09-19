package com.vendorcoinpay.models;

import com.google.gson.annotations.SerializedName;

public class UserLoginRequest {
    @SerializedName("username_or_phone")
    private String usernameOrPhone;

    @SerializedName("password")
    private String password;

    public UserLoginRequest(String usernameOrPhone, String password) {
        this.usernameOrPhone = usernameOrPhone;
        this.password = password;
    }
}
