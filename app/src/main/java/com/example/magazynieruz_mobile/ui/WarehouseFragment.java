package com.example.magazynieruz_mobile.ui;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.magazynieruz_mobile.R;
import com.example.magazynieruz_mobile.data.AppDatabase;
import com.example.magazynieruz_mobile.data.Warehouse;
import com.example.magazynieruz_mobile.data.WarehouseDao;

import org.osmdroid.api.IMapController;
import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;

import java.util.ArrayList;
import java.util.List;

public class WarehouseFragment extends Fragment {

    private WarehouseAdapter adapter;
    private WarehouseDao warehouseDao;
    private List<Warehouse> allWarehouses;
    private MapView mapView;
    private MyLocationNewOverlay locationOverlay;

    private final ActivityResultLauncher<String[]> locationPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), result -> {
                Boolean fine = result.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false);
                if (Boolean.TRUE.equals(fine)) {
                    enableMyLocation();
                }
            });

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        Configuration.getInstance().setUserAgentValue(requireContext().getPackageName());
        return inflater.inflate(R.layout.fragment_warehouse, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        warehouseDao = AppDatabase.getInstance(requireContext()).warehouseDao();
        allWarehouses = new ArrayList<>();

        setupMap(view);
        setupRecycler(view);
        requestLocationPermission();

        Handler mainHandler = new Handler(Looper.getMainLooper());
        AppDatabase.databaseExecutor.execute(() -> {
            List<Warehouse> loaded = warehouseDao.getAllWarehouses();
            mainHandler.post(() -> {
                if (!isAdded()) return;
                allWarehouses = loaded;
                if (!allWarehouses.isEmpty()) {
                    Warehouse first = allWarehouses.get(0);
                    mapView.getController().setCenter(new GeoPoint(first.latitude, first.longitude));
                }
                addWarehouseMarkers(allWarehouses);
                adapter.updateData(allWarehouses);
                setupSpinner(view);
            });
        });
    }

    private void setupMap(View view) {
        mapView = view.findViewById(R.id.mapView);
        mapView.setTileSource(TileSourceFactory.MAPNIK);
        mapView.setMultiTouchControls(true);

        IMapController controller = mapView.getController();
        controller.setZoom(12.0);
        controller.setCenter(new GeoPoint(51.9356, 15.5062)); // Zielona Góra
    }

    private void addWarehouseMarkers(List<Warehouse> warehouses) {
        mapView.getOverlays().removeIf(o -> o instanceof Marker);

        for (Warehouse w : warehouses) {
            Marker marker = new Marker(mapView);
            marker.setPosition(new GeoPoint(w.latitude, w.longitude));
            marker.setTitle(w.name);
            marker.setSnippet(w.address);
            marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
            mapView.getOverlays().add(marker);
        }

        if (locationOverlay != null) {
            mapView.getOverlays().add(locationOverlay);
        }
        mapView.invalidate();
    }

    private void setupRecycler(View view) {
        RecyclerView recycler = view.findViewById(R.id.recyclerWarehouses);
        recycler.setLayoutManager(new LinearLayoutManager(requireContext()));
        recycler.setHasFixedSize(true);
        adapter = new WarehouseAdapter(new ArrayList<>(allWarehouses));
        recycler.setAdapter(adapter);
    }

    private void setupSpinner(View view) {
        Spinner spinner = view.findViewById(R.id.spinnerWarehouses);
        List<String> spinnerItems = new ArrayList<>();
        spinnerItems.add(getString(R.string.all_warehouses));
        for (Warehouse w : allWarehouses) {
            spinnerItems.add(w.name);
        }

        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(
                requireContext(), R.layout.item_spinner, spinnerItems);
        spinnerAdapter.setDropDownViewResource(R.layout.item_spinner_dropdown);
        spinner.setAdapter(spinnerAdapter);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View v, int position, long id) {
                if (position == 0) {
                    adapter.updateData(allWarehouses);
                    addWarehouseMarkers(allWarehouses);
                    if (!allWarehouses.isEmpty()) {
                        mapView.getController().setCenter(
                                new GeoPoint(allWarehouses.get(0).latitude, allWarehouses.get(0).longitude));
                    }
                } else {
                    Warehouse selected = allWarehouses.get(position - 1);
                    List<Warehouse> filtered = new ArrayList<>();
                    filtered.add(selected);
                    adapter.updateData(filtered);
                    addWarehouseMarkers(filtered);
                    mapView.getController().animateTo(
                            new GeoPoint(selected.latitude, selected.longitude));
                    mapView.getController().setZoom(15.0);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private void requestLocationPermission() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            enableMyLocation();
        } else {
            locationPermissionLauncher.launch(new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
            });
        }
    }

    private void enableMyLocation() {
        locationOverlay = new MyLocationNewOverlay(
                new GpsMyLocationProvider(requireContext()), mapView);
        locationOverlay.enableMyLocation();
        mapView.getOverlays().add(locationOverlay);
        mapView.invalidate();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mapView != null && !isHidden()) mapView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mapView != null) mapView.onPause();
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (mapView == null) return;
        if (hidden) {
            mapView.onPause();
        } else {
            mapView.onResume();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (locationOverlay != null) {
            locationOverlay.disableMyLocation();
        }
    }
}
