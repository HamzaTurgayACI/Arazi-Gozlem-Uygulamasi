package com.example.arazigozlemappnew.viewmodel;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.example.arazigozlemappnew.database.Observation;
import com.example.arazigozlemappnew.repository.ObservationRepository;

import java.util.List;

public class ObservationViewModel extends AndroidViewModel {
    private ObservationRepository repository;
    private LiveData<List<Observation>> allObservations;

    public ObservationViewModel(Application application) {
        super(application);
        repository = new ObservationRepository(application);
        allObservations = repository.getAllObservations();
    }

    public void insert(Observation observation) {
        repository.insert(observation);
    }

    public void update(Observation observation) {
        repository.update(observation);
    }

    public void delete(Observation observation) {
        repository.delete(observation);
    }

    public LiveData<List<Observation>> getAllObservations() {
        return allObservations;
    }

    // EKLEDİĞİMİZ FONKSİYON:
    public LiveData<Observation> getObservationById(int id) {
        return repository.getObservationById(id);
    }
}
