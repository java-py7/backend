package com.vendorcoinpay.adapters;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import com.vendorcoinpay.R;
import com.vendorcoinpay.models.Transaction;
import java.util.ArrayList;
import java.util.List;

public class TransactionAdapter extends RecyclerView.Adapter<TransactionAdapter.ViewHolder> {
    private final Context context;
    private final List<Transaction> transactions = new ArrayList<>();

    public TransactionAdapter(Context context) {
        this.context = context;
    }

    public void setTransactions(List<Transaction> newTransactions) {
        transactions.clear();
        if (newTransactions != null) {
            transactions.addAll(newTransactions);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_transaction, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Transaction txn = transactions.get(position);
        boolean isReceived = txn.isReceived();

        if (isReceived) {
            holder.tvTxnTitle.setText("Received from " + txn.getSenderName());
            holder.tvTxnUserCode.setText(txn.getSenderCode());
            holder.tvTxnAmount.setText("+" + txn.getAmount() + " Coins");
            holder.tvTxnAmount.setTextColor(ContextCompat.getColor(context, R.color.color_success));

            holder.ivTxnDirection.setImageResource(R.drawable.ic_arrow_down);
            holder.ivTxnDirection.setImageTintList(ColorStateList.valueOf(ContextCompat.getColor(context, R.color.color_success)));
            holder.layoutTxnIconBadge.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(context, R.color.color_success_bg)));
        } else {
            holder.tvTxnTitle.setText("Paid to " + txn.getReceiverName());
            holder.tvTxnUserCode.setText(txn.getReceiverCode());
            holder.tvTxnAmount.setText("-" + txn.getAmount() + " Coins");
            holder.tvTxnAmount.setTextColor(ContextCompat.getColor(context, R.color.color_expense));

            holder.ivTxnDirection.setImageResource(R.drawable.ic_arrow_up);
            holder.ivTxnDirection.setImageTintList(ColorStateList.valueOf(ContextCompat.getColor(context, R.color.color_expense)));
            holder.layoutTxnIconBadge.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(context, R.color.color_expense_bg)));
        }

        // Timestamp
        String timeStr = txn.getTimestamp();
        if (timeStr != null && timeStr.length() >= 16) {
            // Replace 'T' with space if ISO formatted
            timeStr = timeStr.replace("T", " ").substring(0, 16);
        }
        holder.tvTxnDate.setText(timeStr != null ? timeStr : "");
        holder.tvTxnStatus.setText(txn.getStatus());

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener != null) {
                    listener.onTransactionClick(txn);
                }
            }
        });
    }

    public interface OnTransactionClickListener {
        void onTransactionClick(Transaction transaction);
    }

    private OnTransactionClickListener listener;

    public void setOnTransactionClickListener(OnTransactionClickListener listener) {
        this.listener = listener;
    }

    @Override
    public int getItemCount() {

        return transactions.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        FrameLayout layoutTxnIconBadge;
        ImageView ivTxnDirection;
        TextView tvTxnTitle, tvTxnUserCode, tvTxnDate, tvTxnAmount, tvTxnStatus;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            layoutTxnIconBadge = itemView.findViewById(R.id.layoutTxnIconBadge);
            ivTxnDirection = itemView.findViewById(R.id.ivTxnDirection);
            tvTxnTitle = itemView.findViewById(R.id.tvTxnTitle);
            tvTxnUserCode = itemView.findViewById(R.id.tvTxnUserCode);
            tvTxnDate = itemView.findViewById(R.id.tvTxnDate);
            tvTxnAmount = itemView.findViewById(R.id.tvTxnAmount);
            tvTxnStatus = itemView.findViewById(R.id.tvTxnStatus);
        }
    }
}
