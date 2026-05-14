package com.example.magazynieruz_mobile.data;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface ProductDao {

    @Insert
    void insertProduct(Product product);

    @Query("SELECT * FROM products ORDER BY name ASC")
    List<Product> getAllProducts();

    @Query("SELECT * FROM products WHERE warehouseId = :warehouseId ORDER BY name ASC")
    List<Product> getProductsByWarehouse(int warehouseId);

    @Query("SELECT COUNT(*) FROM products")
    int getTotalProductCount();

    @Query("SELECT COUNT(*) FROM products WHERE quantity < 10")
    int getLowStockCount();

    @Query("SELECT * FROM products WHERE quantity < 10 ORDER BY quantity ASC")
    List<Product> getLowStockProducts();

    @Query("SELECT SUM(quantity * price) FROM products")
    double getTotalInventoryValue();

    @Query("SELECT * FROM products ORDER BY quantity DESC LIMIT 1")
    Product getTopProduct();

    @Query("SELECT w.name FROM warehouses w " +
           "INNER JOIN products p ON p.warehouseId = w.id " +
           "GROUP BY w.id " +
           "ORDER BY COUNT(p.id) DESC " +
           "LIMIT 1")
    String getTopWarehouseName();
}
