package com.example.magazynieruz_mobile.data;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "warehouses")
public class Warehouse {

    @PrimaryKey(autoGenerate = true)
    public int id;

    public String name;

    public String address;

    public double latitude;

    public double longitude;
}
