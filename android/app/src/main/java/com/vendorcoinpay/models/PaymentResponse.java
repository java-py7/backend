package com.vendorcoinpay.models;

import com.google.gson.annotations.SerializedName;

public class PaymentResponse {
    @SerializedName("success")
    private boolean success;

    @SerializedName("message")
    private String message;

    @SerializedName("reference_id")
    private String referenceId;

    @SerializedName("amount")
    private int amount;

    @SerializedName("sender_new_balance")
    private int senderNewBalance;

    @SerializedName("receiver_name")
    private String receiverName;

    @SerializedName("receiver_code")
    private String receiverCode;

    @SerializedName("timestamp")
    private String timestamp;

    public PaymentResponse() {}

    public boolean isSuccess() { return success; }
    public String getMessage() { return message != null ? message : ""; }
    public String getReferenceId() { return referenceId != null ? referenceId : ""; }
    public int getAmount() { return amount; }
    public int getSenderNewBalance() { return senderNewBalance; }
    public String getReceiverName() { return receiverName != null ? receiverName : ""; }
    public String getReceiverCode() { return receiverCode != null ? receiverCode : ""; }
    public String getTimestamp() { return timestamp != null ? timestamp : ""; }
}
