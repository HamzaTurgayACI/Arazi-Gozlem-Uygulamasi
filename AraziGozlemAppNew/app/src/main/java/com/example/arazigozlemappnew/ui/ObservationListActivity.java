package com.example.arazigozlemappnew.ui;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.arazigozlemappnew.databinding.ActivityObservationListBinding;
import com.example.arazigozlemappnew.viewmodel.ObservationViewModel;

public class ObservationListActivity extends AppCompatActivity {
    private ActivityObservationListBinding binding;
    private ObservationViewModel observationViewModel;
    private ObservationAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityObservationListBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Initialize ViewModel
        observationViewModel = new ViewModelProvider(this).get(ObservationViewModel.class);

        // Set up RecyclerView
        binding.rvObservations.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ObservationAdapter();
        binding.rvObservations.setAdapter(adapter);

        // Observe the LiveData
        observationViewModel.getAllObservations().observe(this, observations -> {
            if (observations != null) {
                adapter.setObservations(observations);
            }
        });

        // Set item click listener
        adapter.setOnItemClickListener(observation -> {
            Intent intent = new Intent(this, ObservationDetailActivity.class);
            intent.putExtra("observation_id", observation.getId());
            startActivity(intent);
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}