package com.example.magazynieruz_mobile.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.magazynieruz_mobile.R;
import com.example.magazynieruz_mobile.data.AppDatabase;
import com.example.magazynieruz_mobile.data.Product;
import com.example.magazynieruz_mobile.data.ProductDao;
import com.example.magazynieruz_mobile.data.Warehouse;
import com.example.magazynieruz_mobile.data.WarehouseDao;

import java.util.List;
import java.util.Locale;

public class HomeFragment extends Fragment {

    private static final String ARG_USERNAME = "username";

    public static HomeFragment newInstance(String username) {
        HomeFragment fragment = new HomeFragment();
        Bundle args = new Bundle();
        args.putString(ARG_USERNAME, username);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        String username = getArguments() != null ? getArguments().getString(ARG_USERNAME) : "";

        TextView textWelcome = view.findViewById(R.id.textWelcome);
        TextView textTotalProducts = view.findViewById(R.id.textTotalProducts);
        TextView textLowStock = view.findViewById(R.id.textLowStock);
        TextView textTopWarehouse = view.findViewById(R.id.textTopWarehouse);
        TextView textTopProduct = view.findViewById(R.id.textTopProduct);
        TextView textTotalWarehouses = view.findViewById(R.id.textTotalWarehouses);
        TextView textInventoryValue = view.findViewById(R.id.textInventoryValue);

        textWelcome.setText(getString(R.string.welcome_message, username));

        AppDatabase db = AppDatabase.getInstance(requireContext());
        ProductDao productDao = db.productDao();
        WarehouseDao warehouseDao = db.warehouseDao();

        textTotalProducts.setText(String.valueOf(productDao.getTotalProductCount()));
        textLowStock.setText(String.valueOf(productDao.getLowStockCount()));
        textTotalWarehouses.setText(String.valueOf(warehouseDao.getWarehouseCount()));

        double inventoryValue = productDao.getTotalInventoryValue();
        textInventoryValue.setText(String.format(Locale.getDefault(), "%.2f zł", inventoryValue));

        Product topProduct = productDao.getTopProduct();
        textTopProduct.setText(topProduct != null ? topProduct.name : getString(R.string.no_data));

        List<Warehouse> warehouses = warehouseDao.getAllWarehouses();
        if (!warehouses.isEmpty()) {
            int maxCount = 0;
            String topName = getString(R.string.no_data);
            for (Warehouse w : warehouses) {
                int count = productDao.getProductsByWarehouse(w.id).size();
                if (count > maxCount) {
                    maxCount = count;
                    topName = w.name;
                }
            }
            textTopWarehouse.setText(topName);
        } else {
            textTopWarehouse.setText(getString(R.string.no_data));
        }
    }
}
