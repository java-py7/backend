package com.vendorcoinpay.models;

import com.google.gson.annotations.SerializedName;

public class QRResolveResponse {
    @SerializedName("user_id")
    private int userId;

    @SerializedName("user_code")
    private String userCode;

    @SerializedName("full_name")
    private String fullName;

    @SerializedName("username")
    private String username;

    @SerializedName("phone")
    private String phone;

    @SerializedName("qr_payload")
    private String qrPayload;

    public QRResolveResponse() {}

    public int getUserId() { return userId; }
    public String getUserCode() { return userCode != null ? userCode : ""; }
    public String getFullName() { return fullName != null ? fullName : ""; }
    public String getUsername() { return username != null ? username : ""; }
    public String getPhone() { return phone != null ? phone : ""; }
    public String getQrPayload() { return qrPayload != null ? qrPayload : ""; }
}
