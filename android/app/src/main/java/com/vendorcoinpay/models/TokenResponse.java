package com.vendorcoinpay.models;

import com.google.gson.annotations.SerializedName;

public class TokenResponse {
    @SerializedName("access_token")
    private String accessToken;

    @SerializedName("token_type")
    private String tokenType;

    @SerializedName("user")
    private User user;

    public TokenResponse() {}

    public String getAccessToken() { return accessToken; }
    public String getTokenType() { return tokenType; }
    public User getUser() { return user; }
}
