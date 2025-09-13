package com.example.arazigozlemappnew.repository;

import android.app.Application;
import android.os.AsyncTask;

import androidx.lifecycle.LiveData;

import com.example.arazigozlemappnew.database.AppDatabase;
import com.example.arazigozlemappnew.database.Observation;
import com.example.arazigozlemappnew.database.ObservationDao;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.List;

public class ObservationRepository {
    private final ObservationDao observationDao;
    private final LiveData<List<Observation>> allObservations;
    private final ExecutorService executorService;

    public ObservationRepository(Application application) {
        AppDatabase database = AppDatabase.getInstance(application);
        observationDao = database.observationDao();
        allObservations = observationDao.getAllObservationsLive();
        executorService = Executors.newSingleThreadExecutor();
    }

    public void insert(Observation observation) {
        executorService.execute(() -> observationDao.insert(observation));
    }

    public void update(Observation observation) {
        new UpdateObservationAsyncTask(observationDao).execute(observation);
    }

    public void delete(Observation observation) {
        new DeleteObservationAsyncTask(observationDao).execute(observation);
    }

    public LiveData<List<Observation>> getAllObservations() {
        return allObservations;
    }

    // EKLENEN KISIM: Belirli bir ID'ye sahip gözlemi getir
    public LiveData<Observation> getObservationById(int id) {
        return observationDao.getObservationById(id);
    }


    private static class UpdateObservationAsyncTask extends AsyncTask<Observation, Void, Void> {
        private ObservationDao observationDao;

        private UpdateObservationAsyncTask(ObservationDao observationDao) {
            this.observationDao = observationDao;
        }

        @Override
        protected Void doInBackground(Observation... observations) {
            observationDao.update(observations[0]);
            return null;
        }
    }

    private static class DeleteObservationAsyncTask extends AsyncTask<Observation, Void, Void> {
        private ObservationDao observationDao;

        private DeleteObservationAsyncTask(ObservationDao observationDao) {
            this.observationDao = observationDao;
        }

        @Override
        protected Void doInBackground(Observation... observations) {
            observationDao.delete(observations[0]);
            return null;
        }
    }
}
