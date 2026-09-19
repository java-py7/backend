package com.vendorcoinpay.screens;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatButton;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import com.vendorcoinpay.MainActivity;
import com.vendorcoinpay.R;
import com.vendorcoinpay.adapters.TransactionAdapter;
import com.vendorcoinpay.api.ApiClient;
import com.vendorcoinpay.api.ApiService;
import com.vendorcoinpay.models.Transaction;
import com.vendorcoinpay.models.Wallet;
import com.vendorcoinpay.storage.SessionManager;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HomeFragment extends Fragment {
    private SwipeRefreshLayout swipeRefreshHome;
    private TextView tvHomeGreeting;
    private TextView tvHomeUserCode;
    private TextView tvHomeBalance;
    private ImageView ivRefreshBalance;
    private AppCompatButton btnHomePay;
    private LinearLayout btnHomeScanQr;
    private LinearLayout btnHomeMyQr;
    private TextView tvHomeViewAll;
    private TextView tvHomeEmptyTxns;
    private RecyclerView rvHomeRecentTransactions;
    private TransactionAdapter transactionAdapter;
    private SessionManager sessionManager;
    private ApiService apiService;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        sessionManager = new SessionManager(requireContext());
        apiService = ApiClient.getService(requireContext());

        swipeRefreshHome = view.findViewById(R.id.swipeRefreshHome);
        tvHomeGreeting = view.findViewById(R.id.tvHomeGreeting);
        tvHomeUserCode = view.findViewById(R.id.tvHomeUserCode);
        tvHomeBalance = view.findViewById(R.id.tvHomeBalance);
        ivRefreshBalance = view.findViewById(R.id.ivRefreshBalance);
        btnHomePay = view.findViewById(R.id.btnHomePay);
        btnHomeScanQr = view.findViewById(R.id.btnHomeScanQr);
        btnHomeMyQr = view.findViewById(R.id.btnHomeMyQr);
        tvHomeViewAll = view.findViewById(R.id.tvHomeViewAll);
        tvHomeEmptyTxns = view.findViewById(R.id.tvHomeEmptyTxns);
        rvHomeRecentTransactions = view.findViewById(R.id.rvHomeRecentTransactions);

        rvHomeRecentTransactions.setLayoutManager(new LinearLayoutManager(requireContext()));
        transactionAdapter = new TransactionAdapter(requireContext());
        transactionAdapter.setOnTransactionClickListener(new TransactionAdapter.OnTransactionClickListener() {
            @Override
            public void onTransactionClick(Transaction transaction) {
                ReceiptActivity.start(requireContext(), transaction);
            }
        });
        rvHomeRecentTransactions.setAdapter(transactionAdapter);


        setupViews();
        setupListeners();

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        loadDataFromBackend();
    }

    private void setupViews() {
        tvHomeGreeting.setText("Hello, " + sessionManager.getFullName());
        tvHomeUserCode.setText(sessionManager.getUserCode());
        formatAndDisplayBalance(sessionManager.getCachedBalance());
    }

    private void setupListeners() {
        swipeRefreshHome.setColorSchemeResources(R.color.gold_primary);
        swipeRefreshHome.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                loadDataFromBackend();
            }
        });

        ivRefreshBalance.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                swipeRefreshHome.setRefreshing(true);
                loadDataFromBackend();
            }
        });

        btnHomePay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (getActivity() instanceof MainActivity) {
                    ((MainActivity) getActivity()).switchTab(R.id.nav_pay);
                }
            }
        });

        btnHomeScanQr.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(requireActivity(), ScanQrActivity.class);
                startActivity(intent);
            }
        });

        btnHomeMyQr.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(requireActivity(), MyQrActivity.class);
                startActivity(intent);
            }
        });

        tvHomeViewAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (getActivity() instanceof MainActivity) {
                    ((MainActivity) getActivity()).switchTab(R.id.nav_history);
                }
            }
        });
    }

    private void loadDataFromBackend() {
        // 1. Fetch Authoritative Balance
        apiService.getWalletBalance().enqueue(new Callback<Wallet>() {
            @Override
            public void onResponse(Call<Wallet> call, Response<Wallet> response) {
                if (isAdded() && response.isSuccessful() && response.body() != null) {
                    int balance = response.body().getBalance();
                    sessionManager.updateBalance(balance);
                    formatAndDisplayBalance(balance);
                }
            }

            @Override
            public void onFailure(Call<Wallet> call, Throwable t) {}
        });

        // 2. Fetch Recent Transactions
        apiService.getTransactions("ALL", 4, 0).enqueue(new Callback<List<Transaction>>() {
            @Override
            public void onResponse(Call<List<Transaction>> call, Response<List<Transaction>> response) {
                if (isAdded()) {
                    swipeRefreshHome.setRefreshing(false);
                    if (response.isSuccessful() && response.body() != null) {
                        List<Transaction> txns = response.body();
                        if (txns.isEmpty()) {
                            tvHomeEmptyTxns.setVisibility(View.VISIBLE);
                            rvHomeRecentTransactions.setVisibility(View.GONE);
                        } else {
                            tvHomeEmptyTxns.setVisibility(View.GONE);
                            rvHomeRecentTransactions.setVisibility(View.VISIBLE);
                            transactionAdapter.setTransactions(txns);
                        }
                    }
                }
            }

            @Override
            public void onFailure(Call<List<Transaction>> call, Throwable t) {
                if (isAdded()) {
                    swipeRefreshHome.setRefreshing(false);
                }
            }
        });
    }

    private void formatAndDisplayBalance(int balance) {
        String formatted = NumberFormat.getNumberInstance(Locale.US).format(balance);
        tvHomeBalance.setText(formatted + " Coins");
    }
}
