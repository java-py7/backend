package com.vendorcoinpay.screens;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatButton;
import androidx.fragment.app.Fragment;
import com.vendorcoinpay.MainActivity;
import com.vendorcoinpay.R;
import com.vendorcoinpay.api.ApiClient;
import com.vendorcoinpay.api.ApiService;
import com.vendorcoinpay.models.PaymentRequest;
import com.vendorcoinpay.models.PaymentResponse;
import com.vendorcoinpay.models.User;
import com.vendorcoinpay.storage.SessionManager;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PayFragment extends Fragment {
    private EditText etReceiverIdentifier;
    private LinearLayout btnPayScanQrShortcut;
    private LinearLayout layoutReceiverPreview;
    private TextView tvPreviewReceiverName;
    private TextView tvPreviewReceiverCode;
    private EditText etPaymentAmount;
    private TextView chipAmount50, chipAmount100, chipAmount200, chipAmount500;
    private EditText etPaymentNote;
    private AppCompatButton btnSubmitPayment;
    private ProgressBar pbPayLoading;

    private SessionManager sessionManager;
    private ApiService apiService;
    private User verifiedReceiver = null;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_pay, container, false);

        sessionManager = new SessionManager(requireContext());
        apiService = ApiClient.getService(requireContext());

        etReceiverIdentifier = view.findViewById(R.id.etReceiverIdentifier);
        btnPayScanQrShortcut = view.findViewById(R.id.btnPayScanQrShortcut);
        layoutReceiverPreview = view.findViewById(R.id.layoutReceiverPreview);
        tvPreviewReceiverName = view.findViewById(R.id.tvPreviewReceiverName);
        tvPreviewReceiverCode = view.findViewById(R.id.tvPreviewReceiverCode);
        etPaymentAmount = view.findViewById(R.id.etPaymentAmount);
        chipAmount50 = view.findViewById(R.id.chipAmount50);
        chipAmount100 = view.findViewById(R.id.chipAmount100);
        chipAmount200 = view.findViewById(R.id.chipAmount200);
        chipAmount500 = view.findViewById(R.id.chipAmount500);
        etPaymentNote = view.findViewById(R.id.etPaymentNote);
        btnSubmitPayment = view.findViewById(R.id.btnSubmitPayment);
        pbPayLoading = view.findViewById(R.id.pbPayLoading);

        setupChips();
        setupListeners();
        checkArguments();

        return view;
    }

    private void checkArguments() {
        if (getArguments() != null && getArguments().containsKey("receiver_code")) {
            String code = getArguments().getString("receiver_code");
            if (code != null && !code.isEmpty()) {
                etReceiverIdentifier.setText(code);
                verifyReceiver(code);
            }
        }
    }

    private void setupChips() {
        View.OnClickListener chipListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int add = 0;
                int id = v.getId();
                if (id == R.id.chipAmount50) add = 50;
                else if (id == R.id.chipAmount100) add = 100;
                else if (id == R.id.chipAmount200) add = 200;
                else if (id == R.id.chipAmount500) add = 500;

                String current = etPaymentAmount.getText().toString().trim();
                int val = 0;
                try {
                    val = Integer.parseInt(current);
                } catch (Exception ignored) {}
                etPaymentAmount.setText(String.valueOf(val + add));
            }
        };

        chipAmount50.setOnClickListener(chipListener);
        chipAmount100.setOnClickListener(chipListener);
        chipAmount200.setOnClickListener(chipListener);
        chipAmount500.setOnClickListener(chipListener);
    }

    private void setupListeners() {
        btnPayScanQrShortcut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(requireActivity(), ScanQrActivity.class);
                startActivity(intent);
            }
        });

        etReceiverIdentifier.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String input = s.toString().trim();
                if (input.length() >= 3) {
                    verifyReceiver(input);
                } else {
                    layoutReceiverPreview.setVisibility(View.GONE);
                    verifiedReceiver = null;
                }
            }
            @Override
            public void afterTextChanged(Editable s) {}
        });

        btnSubmitPayment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                validateAndShowConfirmDialog();
            }
        });
    }

    private void verifyReceiver(String identifier) {
        apiService.resolveUser(identifier).enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                if (isAdded() && response.isSuccessful() && response.body() != null) {
                    verifiedReceiver = response.body();
                    tvPreviewReceiverName.setText(verifiedReceiver.getFullName());
                    tvPreviewReceiverCode.setText(verifiedReceiver.getUserCode());
                    layoutReceiverPreview.setVisibility(View.VISIBLE);
                } else {
                    verifiedReceiver = null;
                    layoutReceiverPreview.setVisibility(View.GONE);
                }
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {
                verifiedReceiver = null;
                if (isAdded()) {
                    layoutReceiverPreview.setVisibility(View.GONE);
                }
            }
        });
    }

    private void validateAndShowConfirmDialog() {
        String identifier = etReceiverIdentifier.getText().toString().trim();
        String amountStr = etPaymentAmount.getText().toString().trim();
        final String note = etPaymentNote.getText().toString().trim();

        if (identifier.isEmpty()) {
            Toast.makeText(requireContext(), "Please enter recipient code or username.", Toast.LENGTH_SHORT).show();
            etReceiverIdentifier.requestFocus();
            return;
        }

        int amount = 0;
        try {
            amount = Integer.parseInt(amountStr);
        } catch (Exception ignored) {}

        if (amount <= 0) {
            Toast.makeText(requireContext(), "Please enter a valid coin amount greater than 0.", Toast.LENGTH_SHORT).show();
            etPaymentAmount.requestFocus();
            return;
        }

        final int finalAmount = amount;
        final String finalIdentifier = identifier;
        final String displayName = verifiedReceiver != null ? verifiedReceiver.getFullName() : identifier;
        final String displayCode = verifiedReceiver != null ? verifiedReceiver.getUserCode() : identifier;

        // Show Confirmation Dialog
        final Dialog dialog = new Dialog(requireContext());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_payment_confirm);
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        }

        TextView tvConfirmAmount = dialog.findViewById(R.id.tvConfirmAmount);
        TextView tvConfirmReceiverName = dialog.findViewById(R.id.tvConfirmReceiverName);
        TextView tvConfirmReceiverCode = dialog.findViewById(R.id.tvConfirmReceiverCode);
        TextView tvConfirmNote = dialog.findViewById(R.id.tvConfirmNote);
        AppCompatButton btnDoConfirm = dialog.findViewById(R.id.btnDoConfirmPayment);
        TextView btnCancel = dialog.findViewById(R.id.btnCancelPayment);

        tvConfirmAmount.setText(finalAmount + " Coins");
        tvConfirmReceiverName.setText(displayName);
        tvConfirmReceiverCode.setText("Receiver: " + displayCode);

        if (!note.isEmpty()) {
            tvConfirmNote.setText("Note: " + note);
            tvConfirmNote.setVisibility(View.VISIBLE);
        }

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        btnDoConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                processPayment(finalIdentifier, finalAmount, note);
            }
        });

        dialog.show();
    }

    private void processPayment(String identifier, final int amount, String note) {
        setLoading(true);

        PaymentRequest request = new PaymentRequest(identifier, amount, note);
        apiService.sendCoins(request).enqueue(new Callback<PaymentResponse>() {
            @Override
            public void onResponse(Call<PaymentResponse> call, Response<PaymentResponse> response) {
                setLoading(false);
                if (response.isSuccessful() && response.body() != null) {
                    PaymentResponse payResp = response.body();
                    sessionManager.updateBalance(payResp.getSenderNewBalance());

                    showSuccessDialog(payResp, note);

                    // Clear fields
                    etReceiverIdentifier.setText("");
                    etPaymentAmount.setText("");
                    etPaymentNote.setText("");
                    layoutReceiverPreview.setVisibility(View.GONE);
                    verifiedReceiver = null;
                } else {
                    String error = ApiClient.getErrorMessage(response);
                    Toast.makeText(requireContext(), error, Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<PaymentResponse> call, Throwable t) {
                setLoading(false);
                Toast.makeText(requireContext(), "Payment failed: Unable to connect to server.", Toast.LENGTH_LONG).show();
            }
        });
    }

    private void showSuccessDialog(final PaymentResponse payResp, final String paymentNote) {
        if (!isAdded()) return;

        final Dialog dialog = new Dialog(requireContext());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_payment_success);
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        }

        TextView tvSuccessAmount = dialog.findViewById(R.id.tvSuccessAmount);
        TextView tvSuccessReceiverName = dialog.findViewById(R.id.tvSuccessReceiverName);
        TextView tvSuccessReceiverCode = dialog.findViewById(R.id.tvSuccessReceiverCode);
        TextView tvSuccessRefId = dialog.findViewById(R.id.tvSuccessRefId);
        AppCompatButton btnViewReceipt = dialog.findViewById(R.id.btnViewReceipt);
        TextView btnSuccessDone = dialog.findViewById(R.id.btnSuccessDone);

        tvSuccessAmount.setText(payResp.getAmount() + " Coins");
        tvSuccessReceiverName.setText(payResp.getReceiverName());
        tvSuccessReceiverCode.setText(payResp.getReceiverCode());
        tvSuccessRefId.setText("Ref: " + payResp.getReferenceId());

        btnViewReceipt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                ReceiptActivity.start(
                    requireContext(),
                    payResp.getReferenceId(),
                    payResp.getAmount(),
                    sessionManager.getFullName(),
                    sessionManager.getUserCode(),
                    payResp.getReceiverName(),
                    payResp.getReceiverCode(),
                    payResp.getTimestamp(),
                    paymentNote,
                    "SENT"
                );
            }
        });

        btnSuccessDone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                if (getActivity() instanceof MainActivity) {
                    ((MainActivity) getActivity()).switchTab(R.id.nav_home);
                }
            }
        });

        dialog.show();
    }


    private void setLoading(boolean isLoading) {
        if (isLoading) {
            btnSubmitPayment.setEnabled(false);
            btnSubmitPayment.setText("");
            pbPayLoading.setVisibility(View.VISIBLE);
        } else {
            btnSubmitPayment.setEnabled(true);
            btnSubmitPayment.setText("PAY COINS");
            pbPayLoading.setVisibility(View.GONE);
        }
    }
}
