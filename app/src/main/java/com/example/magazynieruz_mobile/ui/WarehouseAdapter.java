package com.example.magazynieruz_mobile.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.example.magazynieruz_mobile.R;
import com.example.magazynieruz_mobile.data.Warehouse;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class WarehouseAdapter extends RecyclerView.Adapter<WarehouseAdapter.ViewHolder> {

    private List<Warehouse> warehouses;

    public WarehouseAdapter(List<Warehouse> warehouses) {
        this.warehouses = new ArrayList<>(warehouses);
        setHasStableIds(true);
    }

    public void updateData(List<Warehouse> newData) {
        List<Warehouse> oldList = warehouses;
        List<Warehouse> newList = new ArrayList<>(newData);
        DiffUtil.DiffResult diff = DiffUtil.calculateDiff(new DiffUtil.Callback() {
            @Override public int getOldListSize() { return oldList.size(); }
            @Override public int getNewListSize() { return newList.size(); }

            @Override
            public boolean areItemsTheSame(int oldPos, int newPos) {
                return oldList.get(oldPos).id == newList.get(newPos).id;
            }

            @Override
            public boolean areContentsTheSame(int oldPos, int newPos) {
                Warehouse a = oldList.get(oldPos);
                Warehouse b = newList.get(newPos);
                return Objects.equals(a.name, b.name) && Objects.equals(a.address, b.address);
            }
        });
        warehouses = newList;
        diff.dispatchUpdatesTo(this);
    }

    @Override
    public long getItemId(int position) {
        return warehouses.get(position).id;
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
