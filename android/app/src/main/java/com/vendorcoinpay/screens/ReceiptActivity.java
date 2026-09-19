package com.vendorcoinpay.screens;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.core.content.ContextCompat;
import com.vendorcoinpay.R;
import com.vendorcoinpay.api.ApiClient;
import com.vendorcoinpay.api.ApiService;
import com.vendorcoinpay.models.Transaction;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ReceiptActivity extends AppCompatActivity {
    public static final String EXTRA_REF_ID = "extra_ref_id";
    public static final String EXTRA_AMOUNT = "extra_amount";
    public static final String EXTRA_SENDER_NAME = "extra_sender_name";
    public static final String EXTRA_SENDER_CODE = "extra_sender_code";
    public static final String EXTRA_RECEIVER_NAME = "extra_receiver_name";
    public static final String EXTRA_RECEIVER_CODE = "extra_receiver_code";
    public static final String EXTRA_STATUS = "extra_status";
    public static final String EXTRA_TIMESTAMP = "extra_timestamp";
    public static final String EXTRA_NOTE = "extra_note";
    public static final String EXTRA_TYPE = "extra_type";

    private ImageView ivReceiptBack, ivReceiptShareTop, ivReceiptStatusIcon, btnCopyRefId;
    private TextView tvReceiptStatus, tvReceiptAmount, tvReceiptTypeBadge;
    private TextView tvReceiptRefId, tvReceiptDateTime;
    private TextView tvReceiptSenderName, tvReceiptSenderCode;
    private TextView tvReceiptReceiverName, tvReceiptReceiverCode;
    private TextView tvReceiptNote;
    private LinearLayout layoutReceiptNote;
    private AppCompatButton btnShareReceipt, btnDoneReceipt;

    private String refId, senderName, senderCode, receiverName, receiverCode, status, timestamp, note, type;
    private int amount;

    public static void start(Context context, Transaction txn) {
        Intent intent = new Intent(context, ReceiptActivity.class);
        intent.putExtra(EXTRA_REF_ID, txn.getReferenceId());
        intent.putExtra(EXTRA_AMOUNT, txn.getAmount());
        intent.putExtra(EXTRA_SENDER_NAME, txn.getSenderName());
        intent.putExtra(EXTRA_SENDER_CODE, txn.getSenderCode());
        intent.putExtra(EXTRA_RECEIVER_NAME, txn.getReceiverName());
        intent.putExtra(EXTRA_RECEIVER_CODE, txn.getReceiverCode());
        intent.putExtra(EXTRA_STATUS, txn.getStatus());
        intent.putExtra(EXTRA_TIMESTAMP, txn.getTimestamp());
        intent.putExtra(EXTRA_NOTE, txn.getNote());
        intent.putExtra(EXTRA_TYPE, txn.getType());
        context.startActivity(intent);
    }

    public static void start(Context context, String refId, int amount, String senderName, String senderCode,
                             String receiverName, String receiverCode, String timestamp, String note, String type) {
        Intent intent = new Intent(context, ReceiptActivity.class);
        intent.putExtra(EXTRA_REF_ID, refId);
        intent.putExtra(EXTRA_AMOUNT, amount);
        intent.putExtra(EXTRA_SENDER_NAME, senderName);
        intent.putExtra(EXTRA_SENDER_CODE, senderCode);
        intent.putExtra(EXTRA_RECEIVER_NAME, receiverName);
        intent.putExtra(EXTRA_RECEIVER_CODE, receiverCode);
        intent.putExtra(EXTRA_STATUS, "COMPLETED");
        intent.putExtra(EXTRA_TIMESTAMP, timestamp);
        intent.putExtra(EXTRA_NOTE, note);
        intent.putExtra(EXTRA_TYPE, type);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_receipt);

        bindViews();
        extractIntentData();
        renderReceipt();
        setupListeners();
    }

    private void bindViews() {
        ivReceiptBack = findViewById(R.id.ivReceiptBack);
        ivReceiptShareTop = findViewById(R.id.ivReceiptShareTop);
        ivReceiptStatusIcon = findViewById(R.id.ivReceiptStatusIcon);
        btnCopyRefId = findViewById(R.id.btnCopyRefId);

        tvReceiptStatus = findViewById(R.id.tvReceiptStatus);
        tvReceiptAmount = findViewById(R.id.tvReceiptAmount);
        tvReceiptTypeBadge = findViewById(R.id.tvReceiptTypeBadge);
        tvReceiptRefId = findViewById(R.id.tvReceiptRefId);
        tvReceiptDateTime = findViewById(R.id.tvReceiptDateTime);

        tvReceiptSenderName = findViewById(R.id.tvReceiptSenderName);
        tvReceiptSenderCode = findViewById(R.id.tvReceiptSenderCode);
        tvReceiptReceiverName = findViewById(R.id.tvReceiptReceiverName);
        tvReceiptReceiverCode = findViewById(R.id.tvReceiptReceiverCode);

        tvReceiptNote = findViewById(R.id.tvReceiptNote);
        layoutReceiptNote = findViewById(R.id.layoutReceiptNote);

        btnShareReceipt = findViewById(R.id.btnShareReceipt);
        btnDoneReceipt = findViewById(R.id.btnDoneReceipt);
    }

    private void extractIntentData() {
        Intent intent = getIntent();
        if (intent != null) {
            refId = intent.getStringExtra(EXTRA_REF_ID);
            amount = intent.getIntExtra(EXTRA_AMOUNT, 0);
            senderName = intent.getStringExtra(EXTRA_SENDER_NAME);
            senderCode = intent.getStringExtra(EXTRA_SENDER_CODE);
            receiverName = intent.getStringExtra(EXTRA_RECEIVER_NAME);
            receiverCode = intent.getStringExtra(EXTRA_RECEIVER_CODE);
            status = intent.getStringExtra(EXTRA_STATUS);
            timestamp = intent.getStringExtra(EXTRA_TIMESTAMP);
            note = intent.getStringExtra(EXTRA_NOTE);
            type = intent.getStringExtra(EXTRA_TYPE);
        }

        if (status == null || status.isEmpty()) status = "COMPLETED";
        if (type == null || type.isEmpty()) type = "SENT";
        if (senderName == null || senderName.isEmpty()) senderName = "User";
        if (receiverName == null || receiverName.isEmpty()) receiverName = "User";
        if (refId == null) refId = "TXN-UNKNOWN";
    }

    private void renderReceipt() {
        tvReceiptRefId.setText(refId);
        tvReceiptAmount.setText(amount + " Coins");

        boolean isReceived = "RECEIVED".equalsIgnoreCase(type);

        if (isReceived) {
            tvReceiptTypeBadge.setText("PAYMENT RECEIVED");
            tvReceiptTypeBadge.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.color_success_bg)));
            tvReceiptTypeBadge.setTextColor(ContextCompat.getColor(this, R.color.color_success));
            tvReceiptAmount.setTextColor(ContextCompat.getColor(this, R.color.color_success));
        } else {
            tvReceiptTypeBadge.setText("PAYMENT SENT");
            tvReceiptTypeBadge.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.color_expense_bg)));
            tvReceiptTypeBadge.setTextColor(ContextCompat.getColor(this, R.color.color_expense));
            tvReceiptAmount.setTextColor(ContextCompat.getColor(this, R.color.gold_primary));
        }

        tvReceiptStatus.setText("PAYMENT " + status.toUpperCase(Locale.US));
        if ("FAILED".equalsIgnoreCase(status)) {
            tvReceiptStatus.setTextColor(ContextCompat.getColor(this, R.color.color_expense));
            ivReceiptStatusIcon.setImageTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.color_expense)));
        } else {
            tvReceiptStatus.setTextColor(ContextCompat.getColor(this, R.color.color_success));
            ivReceiptStatusIcon.setImageTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.color_success)));
        }

        tvReceiptDateTime.setText(formatDateTime(timestamp));
        tvReceiptSenderName.setText(senderName);
        tvReceiptSenderCode.setText(senderCode != null && !senderCode.isEmpty() ? senderCode : "Sender");
        tvReceiptReceiverName.setText(receiverName);
        tvReceiptReceiverCode.setText(receiverCode != null && !receiverCode.isEmpty() ? receiverCode : "Receiver");

        if (note != null && !note.trim().isEmpty()) {
            tvReceiptNote.setText(note);
            layoutReceiptNote.setVisibility(View.VISIBLE);
        } else {
            layoutReceiptNote.setVisibility(View.GONE);
        }
    }

    private void setupListeners() {
        ivReceiptBack.setOnClickListener(v -> finish());
        btnDoneReceipt.setOnClickListener(v -> finish());

        btnCopyRefId.setOnClickListener(v -> copyReferenceId());

        View.OnClickListener shareListener = v -> shareDigitalReceipt();
        ivReceiptShareTop.setOnClickListener(shareListener);
        btnShareReceipt.setOnClickListener(shareListener);
    }

    private void copyReferenceId() {
        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        if (clipboard != null) {
            ClipData clip = ClipData.newPlainText("Transaction Reference", refId);
            clipboard.setPrimaryClip(clip);
            Toast.makeText(this, "Reference ID copied: " + refId, Toast.LENGTH_SHORT).show();
        }
    }

    private void shareDigitalReceipt() {
        String formattedDate = formatDateTime(timestamp);
        String directionTag = "RECEIVED".equalsIgnoreCase(type) ? "PAYMENT RECEIVED" : "PAYMENT SENT";

        StringBuilder sb = new StringBuilder();
        sb.append("═══════════════════════════\n");
        sb.append("   VENDOR COIN PAY RECEIPT\n");
        sb.append("═══════════════════════════\n");
        sb.append("Amount: ").append(amount).append(" Coins\n");
        sb.append("Status: ").append(status.toUpperCase(Locale.US)).append("\n");
        sb.append("Type: ").append(directionTag).append("\n");
        sb.append("Reference: ").append(refId).append("\n");
        sb.append("Date & Time: ").append(formattedDate).append("\n");
        sb.append("---------------------------\n");
        sb.append("Sent By: ").append(senderName).append(" (").append(senderCode).append(")\n");
        sb.append("Received By: ").append(receiverName).append(" (").append(receiverCode).append(")\n");
        if (note != null && !note.trim().isEmpty()) {
            sb.append("Note: ").append(note).append("\n");
        }
        sb.append("═══════════════════════════\n");
        sb.append("Verified Instant Virtual Coin Transfer\n");
        sb.append("Vendor Coin Pay\n");

        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Vendor Coin Pay Receipt - " + refId);
        shareIntent.putExtra(Intent.EXTRA_TEXT, sb.toString());
        startActivity(Intent.createChooser(shareIntent, "Share Receipt Via"));
    }

    private String formatDateTime(String raw) {
        if (raw == null || raw.trim().isEmpty()) {
            return new SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()).format(new Date());
        }
        // Normalize ISO format
        String clean = raw.replace("T", " ");
        if (clean.contains(".")) {
            clean = clean.substring(0, clean.indexOf("."));
        }
        SimpleDateFormat parser = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);
        try {
            Date date = parser.parse(clean);
            if (date != null) {
                return new SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.US).format(date);
            }
        } catch (ParseException ignored) {
        }
        return clean;
    }
}
