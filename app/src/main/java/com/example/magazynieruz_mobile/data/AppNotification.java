package com.example.magazynieruz_mobile.data;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "notifications")
public class AppNotification {

    @PrimaryKey(autoGenerate = true)
    public int id;

    public String title;

    public String message;

    public long createdAt;
}
