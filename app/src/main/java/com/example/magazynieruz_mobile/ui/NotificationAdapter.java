package com.example.magazynieruz_mobile.ui;

import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.magazynieruz_mobile.R;
import com.example.magazynieruz_mobile.data.AppNotification;

import java.util.List;

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.VH> {

    private List<AppNotification> items;

    public NotificationAdapter(List<AppNotification> items) {
        this.items = items;
    }

    public void update(List<AppNotification> next) {
        this.items = next;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_notification, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        AppNotification n = items.get(position);
        holder.title.setText(n.title);
        holder.message.setText(n.message);
        holder.time.setText(DateUtils.getRelativeTimeSpanString(
                n.createdAt, System.currentTimeMillis(), DateUtils.MINUTE_IN_MILLIS));
    }

    @Override
    public int getItemCount() {
        return items == null ? 0 : items.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        final TextView title;
        final TextView message;
        final TextView time;

        VH(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.textNotifTitle);
            message = itemView.findViewById(R.id.textNotifMessage);
            time = itemView.findViewById(R.id.textNotifTime);
        }
    }
}
