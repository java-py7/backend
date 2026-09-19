package com.vendorcoinpay.screens;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatButton;
import androidx.fragment.app.Fragment;
import com.vendorcoinpay.R;
import com.vendorcoinpay.api.ApiClient;
import com.vendorcoinpay.api.ApiService;
import com.vendorcoinpay.models.User;
import com.vendorcoinpay.models.UserProfileUpdateRequest;
import com.vendorcoinpay.storage.SessionManager;
import java.text.NumberFormat;
import java.util.Locale;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProfileFragment extends Fragment {
    private TextView tvProfileFullName, tvProfileUsername, tvProfileUserCode, tvProfilePhone, tvProfileBalance;
    private LinearLayout btnEditProfile;
    private AppCompatButton btnLogout;
    private SessionManager sessionManager;
    private ApiService apiService;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        sessionManager = new SessionManager(requireContext());
        apiService = ApiClient.getService(requireContext());

        tvProfileFullName = view.findViewById(R.id.tvProfileFullName);
        tvProfileUsername = view.findViewById(R.id.tvProfileUsername);
        tvProfileUserCode = view.findViewById(R.id.tvProfileUserCode);
        tvProfilePhone = view.findViewById(R.id.tvProfilePhone);
        tvProfileBalance = view.findViewById(R.id.tvProfileBalance);
        btnEditProfile = view.findViewById(R.id.btnEditProfile);
        btnLogout = view.findViewById(R.id.btnLogout);

        populateUserData();
        setupListeners();

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        fetchFreshProfile();
    }

    private void populateUserData() {
        tvProfileFullName.setText(sessionManager.getFullName());
        tvProfileUsername.setText("@" + sessionManager.getUsername());
        tvProfileUserCode.setText("User Code: " + sessionManager.getUserCode());
        tvProfilePhone.setText(sessionManager.getPhone());
        String formatted = NumberFormat.getNumberInstance(Locale.US).format(sessionManager.getCachedBalance());
        tvProfileBalance.setText(formatted + " Coins");
    }

    private void fetchFreshProfile() {
        apiService.getCurrentUser().enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                if (isAdded() && response.isSuccessful() && response.body() != null) {
                    User user = response.body();
                    sessionManager.updateUserData(user);
                    populateUserData();
                }
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {}
        });
    }

    private void setupListeners() {
        btnEditProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showEditProfileDialog();
            }
        });

        btnLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new AlertDialog.Builder(requireContext(), R.style.Theme_VendorCoinPay)
                    .setTitle("Log Out")
                    .setMessage("Are you sure you want to log out of Vendor Coin Pay?")
                    .setPositiveButton("LOG OUT", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            sessionManager.logout();
                            Intent intent = new Intent(requireActivity(), LoginActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);
                            requireActivity().finish();
                        }
                    })
                    .setNegativeButton("CANCEL", null)
                    .show();
            }
        });
    }

    private void showEditProfileDialog() {
        LinearLayout layout = new LinearLayout(requireContext());
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(48, 24, 48, 24);

        final EditText etName = new EditText(requireContext());
        etName.setHint("Full Name");
        etName.setText(sessionManager.getFullName());
        etName.setTextColor(getResources().getColor(R.color.text_primary));
        etName.setHintTextColor(getResources().getColor(R.color.text_muted));
        layout.addView(etName);

        final EditText etPhone = new EditText(requireContext());
        etPhone.setHint("Phone Number");
        etPhone.setText(sessionManager.getPhone());
        etPhone.setTextColor(getResources().getColor(R.color.text_primary));
        etPhone.setHintTextColor(getResources().getColor(R.color.text_muted));
        layout.addView(etPhone);

        new AlertDialog.Builder(requireContext(), R.style.Theme_VendorCoinPay)
            .setTitle("Edit Profile")
            .setView(layout)
            .setPositiveButton("SAVE", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    String newName = etName.getText().toString().trim();
                    String newPhone = etPhone.getText().toString().trim();

                    if (newName.length() < 2) {
                        Toast.makeText(requireContext(), "Name must be at least 2 characters.", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    UserProfileUpdateRequest req = new UserProfileUpdateRequest(newName, newPhone);
                    apiService.updateProfile(req).enqueue(new Callback<User>() {
                        @Override
                        public void onResponse(Call<User> call, Response<User> response) {
                            if (isAdded() && response.isSuccessful() && response.body() != null) {
                                sessionManager.updateUserData(response.body());
                                populateUserData();
                                Toast.makeText(requireContext(), "Profile updated successfully.", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(requireContext(), "Failed to update profile.", Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onFailure(Call<User> call, Throwable t) {
                            if (isAdded()) {
                                Toast.makeText(requireContext(), "Error connecting to server.", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }
            })
            .setNegativeButton("CANCEL", null)
            .show();
    }
}
