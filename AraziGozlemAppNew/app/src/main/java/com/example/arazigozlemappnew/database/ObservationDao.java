package com.example.arazigozlemappnew.database;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface ObservationDao {
    // CRUD Operasyonları
    @Insert
    void insert(Observation observation);

    @Update
    void update(Observation observation);

    @Delete
    void delete(Observation observation);

    // Tüm gözlemleri LiveData ile getir (UI için)
    @Query("SELECT * FROM observations ORDER BY timestamp DESC")
    LiveData<List<Observation>> getAllObservationsLive();

    // ID ile bir gözlemi LiveData olarak getir (UI için)
    @Query("SELECT * FROM observations WHERE id = :id LIMIT 1")
    LiveData<Observation> getObservationById(int id);



    // Tüm gözlemleri senkron getir (arka plan işlemleri için)
    @Query("SELECT * FROM observations ORDER BY timestamp DESC")
    List<Observation> getAllObservationsSync();

    // ID ile tek gözlemi senkron getir (arka plan işlemleri için)
    @Query("SELECT * FROM observations WHERE id = :id LIMIT 1")
    Observation getObservationByIdSync(int id);
}
