package com.vendorcoinpay.screens;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import com.vendorcoinpay.MainActivity;
import com.vendorcoinpay.R;
import com.vendorcoinpay.api.ApiClient;
import com.vendorcoinpay.api.ApiService;
import com.vendorcoinpay.models.TokenResponse;
import com.vendorcoinpay.models.UserRegisterRequest;
import com.vendorcoinpay.storage.SessionManager;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RegisterActivity extends AppCompatActivity {
    private EditText etFullName, etUsername, etPhone, etRegPassword, etConfirmPassword;
    private AppCompatButton btnRegister;
    private ProgressBar pbRegLoading;
    private TextView tvRegErrorMessage, tvGoToLogin;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        sessionManager = new SessionManager(this);

        etFullName = findViewById(R.id.etFullName);
        etUsername = findViewById(R.id.etUsername);
        etPhone = findViewById(R.id.etPhone);
        etRegPassword = findViewById(R.id.etRegPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        btnRegister = findViewById(R.id.btnRegister);
        pbRegLoading = findViewById(R.id.pbRegLoading);
        tvRegErrorMessage = findViewById(R.id.tvRegErrorMessage);
        tvGoToLogin = findViewById(R.id.tvGoToLogin);

        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                performRegistration();
            }
        });

        tvGoToLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void performRegistration() {
        String fullName = etFullName.getText().toString().trim();
        String username = etUsername.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();
        String password = etRegPassword.getText().toString().trim();
        String confirmPassword = etConfirmPassword.getText().toString().trim();

        if (fullName.length() < 2) {
            showError("Please enter your full name.");
            etFullName.requestFocus();
            return;
        }

        if (username.length() < 3) {
            showError("Username must be at least 3 characters.");
            etUsername.requestFocus();
            return;
        }

        if (phone.length() < 7) {
            showError("Please enter a valid phone number.");
            etPhone.requestFocus();
            return;
        }

        if (password.length() < 4) {
            showError("Password must be at least 4 characters.");
            etRegPassword.requestFocus();
            return;
        }

        if (!password.equals(confirmPassword)) {
            showError("Passwords do not match.");
            etConfirmPassword.requestFocus();
            return;
        }

        setLoading(true);
        tvRegErrorMessage.setVisibility(View.GONE);

        ApiService apiService = ApiClient.getService(this);
        UserRegisterRequest request = new UserRegisterRequest(fullName, username, phone, password, confirmPassword);

        apiService.register(request).enqueue(new Callback<TokenResponse>() {
            @Override
            public void onResponse(Call<TokenResponse> call, Response<TokenResponse> response) {
                setLoading(false);
                if (response.isSuccessful() && response.body() != null) {
                    TokenResponse tokenResp = response.body();
                    sessionManager.saveSession(tokenResp.getAccessToken(), tokenResp.getUser());

                    Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                } else {
                    String error = ApiClient.getErrorMessage(response);
                    showError(error);
                }
            }

            @Override
            public void onFailure(Call<TokenResponse> call, Throwable t) {
                setLoading(false);
                showError("Unable to connect to server. Please check your network connection.");
            }
        });
    }

    private void setLoading(boolean isLoading) {
        if (isLoading) {
            btnRegister.setEnabled(false);
            btnRegister.setText("");
            pbRegLoading.setVisibility(View.VISIBLE);
        } else {
            btnRegister.setEnabled(true);
            btnRegister.setText(R.string.btn_register);
            pbRegLoading.setVisibility(View.GONE);
        }
    }

    private void showError(String message) {
        tvRegErrorMessage.setText(message);
        tvRegErrorMessage.setVisibility(View.VISIBLE);
    }
}
