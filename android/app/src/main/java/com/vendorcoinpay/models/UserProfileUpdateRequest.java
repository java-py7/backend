package com.vendorcoinpay.models;

import com.google.gson.annotations.SerializedName;

public class UserProfileUpdateRequest {
    @SerializedName("full_name")
    private String fullName;

    @SerializedName("phone")
    private String phone;

    public UserProfileUpdateRequest(String fullName, String phone) {
        this.fullName = fullName;
        this.phone = phone;
    }
}
