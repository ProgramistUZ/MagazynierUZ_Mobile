package com.example.magazynieruz_mobile.ui;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
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
import com.example.magazynieruz_mobile.data.WarehouseDao;

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
        Handler mainHandler = new Handler(Looper.getMainLooper());
        String noData = getString(R.string.no_data);

        AppDatabase.databaseExecutor.execute(() -> {
            int totalProducts = productDao.getTotalProductCount();
            int lowStock = productDao.getLowStockCount();
            int totalWarehouses = warehouseDao.getWarehouseCount();
            double inventoryValue = productDao.getTotalInventoryValue();
            Product topProduct = productDao.getTopProduct();
            String topWarehouseName = productDao.getTopWarehouseName();

            mainHandler.post(() -> {
                if (!isAdded()) return;
                textTotalProducts.setText(String.valueOf(totalProducts));
                textLowStock.setText(String.valueOf(lowStock));
                textTotalWarehouses.setText(String.valueOf(totalWarehouses));
                textInventoryValue.setText(String.format(Locale.getDefault(), "%.2f zł", inventoryValue));
                textTopProduct.setText(topProduct != null ? topProduct.name : noData);
                textTopWarehouse.setText(topWarehouseName != null ? topWarehouseName : noData);
            });
        });
    }
}
