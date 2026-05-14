package com.example.magazynieruz_mobile.data;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface NotificationDao {

    @Insert
    void insert(AppNotification notification);

    @Query("SELECT * FROM notifications ORDER BY createdAt DESC")
    List<AppNotification> getAll();

    @Query("DELETE FROM notifications")
    void clear();
}
