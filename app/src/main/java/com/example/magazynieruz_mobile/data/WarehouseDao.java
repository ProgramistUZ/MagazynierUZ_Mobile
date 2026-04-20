package com.example.magazynieruz_mobile.data;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface WarehouseDao {

    @Insert
    void insertWarehouse(Warehouse warehouse);

    @Query("SELECT * FROM warehouses ORDER BY name ASC")
    List<Warehouse> getAllWarehouses();

    @Query("SELECT * FROM warehouses WHERE id = :id LIMIT 1")
    Warehouse findById(int id);

    @Query("SELECT COUNT(*) FROM warehouses")
    int getWarehouseCount();
}
