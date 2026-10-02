package com.example.camara_gui.data;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface PhotoDao {
    @Insert
    void insert(Photo photo);
    @Delete
    void delete(Photo photo);
    @Query("SELECT * FROM Photo")
    List<Photo> getAll();
}