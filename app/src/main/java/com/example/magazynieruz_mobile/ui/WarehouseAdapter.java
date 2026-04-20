package com.example.magazynieruz_mobile.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.magazynieruz_mobile.R;
import com.example.magazynieruz_mobile.data.Warehouse;

import java.util.List;

public class WarehouseAdapter extends RecyclerView.Adapter<WarehouseAdapter.ViewHolder> {

    private List<Warehouse> warehouses;

    public WarehouseAdapter(List<Warehouse> warehouses) {
        this.warehouses = warehouses;
    }

    public void updateData(List<Warehouse> newData) {
        this.warehouses = newData;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_warehouse, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Warehouse warehouse = warehouses.get(position);
        holder.textName.setText(warehouse.name);
        holder.textAddress.setText(warehouse.address);
    }

    @Override
    public int getItemCount() {
        return warehouses.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView textName;
        TextView textAddress;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            textName = itemView.findViewById(R.id.textWarehouseName);
            textAddress = itemView.findViewById(R.id.textWarehouseAddress);
        }
    }
}
