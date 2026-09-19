package com.vendorcoinpay.screens;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import com.vendorcoinpay.R;
import com.vendorcoinpay.adapters.TransactionAdapter;
import com.vendorcoinpay.api.ApiClient;
import com.vendorcoinpay.api.ApiService;
import com.vendorcoinpay.models.Transaction;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HistoryFragment extends Fragment {
    private TextView btnFilterAll, btnFilterSent, btnFilterReceived;
    private SwipeRefreshLayout swipeRefreshHistory;
    private RecyclerView rvTransactions;
    private TextView tvHistoryEmpty;
    private ProgressBar pbHistoryLoading;
    private TransactionAdapter adapter;
    private ApiService apiService;

    private String currentFilter = "ALL";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_history, container, false);

        apiService = ApiClient.getService(requireContext());

        btnFilterAll = view.findViewById(R.id.btnFilterAll);
        btnFilterSent = view.findViewById(R.id.btnFilterSent);
        btnFilterReceived = view.findViewById(R.id.btnFilterReceived);
        swipeRefreshHistory = view.findViewById(R.id.swipeRefreshHistory);
        rvTransactions = view.findViewById(R.id.rvTransactions);
        tvHistoryEmpty = view.findViewById(R.id.tvHistoryEmpty);
        pbHistoryLoading = view.findViewById(R.id.pbHistoryLoading);

        rvTransactions.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new TransactionAdapter(requireContext());
        adapter.setOnTransactionClickListener(new TransactionAdapter.OnTransactionClickListener() {
            @Override
            public void onTransactionClick(Transaction transaction) {
                ReceiptActivity.start(requireContext(), transaction);
            }
        });
        rvTransactions.setAdapter(adapter);

        setupFilters();
        setupSwipeRefresh();


        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        loadTransactions(currentFilter);
    }

    private void setupFilters() {
        btnFilterAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setFilterTab("ALL");
            }
        });

        btnFilterSent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setFilterTab("SENT");
            }
        });

        btnFilterReceived.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setFilterTab("RECEIVED");
            }
        });
    }

    private void setFilterTab(String filter) {
        currentFilter = filter;

        // Reset all tabs to inactive state
        btnFilterAll.setBackgroundTintList(ContextCompat.getColorStateList(requireContext(), R.color.bg_surface));
        btnFilterAll.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_secondary));

        btnFilterSent.setBackgroundTintList(ContextCompat.getColorStateList(requireContext(), R.color.bg_surface));
        btnFilterSent.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_secondary));

        btnFilterReceived.setBackgroundTintList(ContextCompat.getColorStateList(requireContext(), R.color.bg_surface));
        btnFilterReceived.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_secondary));

        // Highlight selected tab
        if ("ALL".equals(filter)) {
            btnFilterAll.setBackgroundTintList(ContextCompat.getColorStateList(requireContext(), R.color.gold_primary));
            btnFilterAll.setTextColor(ContextCompat.getColor(requireContext(), R.color.bg_dark));
        } else if ("SENT".equals(filter)) {
            btnFilterSent.setBackgroundTintList(ContextCompat.getColorStateList(requireContext(), R.color.gold_primary));
            btnFilterSent.setTextColor(ContextCompat.getColor(requireContext(), R.color.bg_dark));
        } else if ("RECEIVED".equals(filter)) {
            btnFilterReceived.setBackgroundTintList(ContextCompat.getColorStateList(requireContext(), R.color.gold_primary));
            btnFilterReceived.setTextColor(ContextCompat.getColor(requireContext(), R.color.bg_dark));
        }

        loadTransactions(filter);
    }

    private void setupSwipeRefresh() {
        swipeRefreshHistory.setColorSchemeResources(R.color.gold_primary);
        swipeRefreshHistory.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                loadTransactions(currentFilter);
            }
        });
    }

    private void loadTransactions(String filter) {
        pbHistoryLoading.setVisibility(View.VISIBLE);
        tvHistoryEmpty.setVisibility(View.GONE);

        apiService.getTransactions(filter, 50, 0).enqueue(new Callback<List<Transaction>>() {
            @Override
            public void onResponse(Call<List<Transaction>> call, Response<List<Transaction>> response) {
                if (isAdded()) {
                    pbHistoryLoading.setVisibility(View.GONE);
                    swipeRefreshHistory.setRefreshing(false);
                    if (response.isSuccessful() && response.body() != null) {
                        List<Transaction> list = response.body();
                        if (list.isEmpty()) {
                            tvHistoryEmpty.setVisibility(View.VISIBLE);
                            rvTransactions.setVisibility(View.GONE);
                        } else {
                            tvHistoryEmpty.setVisibility(View.GONE);
                            rvTransactions.setVisibility(View.VISIBLE);
                            adapter.setTransactions(list);
                        }
                    }
                }
            }

            @Override
            public void onFailure(Call<List<Transaction>> call, Throwable t) {
                if (isAdded()) {
                    pbHistoryLoading.setVisibility(View.GONE);
                    swipeRefreshHistory.setRefreshing(false);
                    tvHistoryEmpty.setText("Unable to load transaction history.");
                    tvHistoryEmpty.setVisibility(View.VISIBLE);
                }
            }
        });
    }
}
