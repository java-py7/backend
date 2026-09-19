package com.vendorcoinpay.models;

import com.google.gson.annotations.SerializedName;

public class PaymentRequest {
    @SerializedName("receiver_identifier")
    private String receiverIdentifier;

    @SerializedName("amount")
    private int amount;

    @SerializedName("note")
    private String note;

    public PaymentRequest(String receiverIdentifier, int amount, String note) {
        this.receiverIdentifier = receiverIdentifier;
        this.amount = amount;
        this.note = note != null ? note : "";
    }

    public String getReceiverIdentifier() { return receiverIdentifier; }
    public void setReceiverIdentifier(String receiverIdentifier) { this.receiverIdentifier = receiverIdentifier; }

    public int getAmount() { return amount; }
    public void setAmount(int amount) { this.amount = amount; }

    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }
}
