package com.example.magazynieruz_mobile.data;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(
    tableName = "products",
    foreignKeys = @ForeignKey(
        entity = Warehouse.class,
        parentColumns = "id",
        childColumns = "warehouseId",
        onDelete = ForeignKey.CASCADE
    ),
    indices = @Index("warehouseId")
)
public class Product {

    @PrimaryKey(autoGenerate = true)
    public int id;

    public String name;

    public int quantity;

    public double price;

    public int warehouseId;
}
