package com.vendorcoinpay.models;

import com.google.gson.annotations.SerializedName;

public class Transaction {
    @SerializedName("id")
    private int id;

    @SerializedName("reference_id")
    private String referenceId;

    @SerializedName("sender_id")
    private int senderId;

    @SerializedName("sender_name")
    private String senderName;

    @SerializedName("sender_code")
    private String senderCode;

    @SerializedName("receiver_id")
    private int receiverId;

    @SerializedName("receiver_name")
    private String receiverName;

    @SerializedName("receiver_code")
    private String receiverCode;

    @SerializedName("amount")
    private int amount;

    @SerializedName("status")
    private String status;

    @SerializedName("note")
    private String note;

    @SerializedName("timestamp")
    private String timestamp;

    @SerializedName("type")
    private String type; // 'SENT' or 'RECEIVED'

    public Transaction() {}

    public int getId() { return id; }
    public String getReferenceId() { return referenceId; }
    public int getSenderId() { return senderId; }
    public String getSenderName() { return senderName != null ? senderName : "Unknown"; }
    public String getSenderCode() { return senderCode != null ? senderCode : ""; }
    public int getReceiverId() { return receiverId; }
    public String getReceiverName() { return receiverName != null ? receiverName : "Unknown"; }
    public String getReceiverCode() { return receiverCode != null ? receiverCode : ""; }
    public int getAmount() { return amount; }
    public String getStatus() { return status != null ? status : "COMPLETED"; }
    public String getNote() { return note != null ? note : ""; }
    public String getTimestamp() { return timestamp != null ? timestamp : ""; }
    public String getType() { return type != null ? type : "SENT"; }

    public boolean isReceived() {
        return "RECEIVED".equalsIgnoreCase(type);
    }
}
