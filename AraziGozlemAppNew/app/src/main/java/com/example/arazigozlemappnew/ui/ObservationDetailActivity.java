package com.example.arazigozlemappnew.ui;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.arazigozlemappnew.databinding.ActivityObservationDetailBinding;
import com.example.arazigozlemappnew.database.Observation;
import com.example.arazigozlemappnew.viewmodel.ObservationViewModel;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ObservationDetailActivity extends AppCompatActivity {
    private ActivityObservationDetailBinding binding;
    private ObservationViewModel observationViewModel;
    private int observationId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityObservationDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Get observation ID from intent
        observationId = getIntent().getIntExtra("observation_id", -1);
        if (observationId == -1) {
            finish();
            return;
        }

        // Initialize ViewModel
        observationViewModel = new ViewModelProvider(this).get(ObservationViewModel.class);

        // Gözlemi gözlemle ve arayüze aktar
        observationViewModel.getObservationById(observationId).observe(this, this::populateViews);
    }

    private void populateViews(Observation observation) {
        if (observation == null) {
            finish();
            return;
        }

        binding.tvTitle.setText(observation.getTitle());
        binding.tvCategory.setText("Kategori: " + observation.getCategory());
        binding.tvDescription.setText(observation.getDescription());
        binding.tvLocation.setText(String.format(Locale.getDefault(),
                "Enlem: %.6f, Boylam: %.6f",
                observation.getLatitude(), observation.getLongitude()));

        // Eğer zaman tutuluyorsa, göster
        try {
            long timestamp = observation.getTimestamp();
            if (timestamp > 0) {
                SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault());
                binding.tvDate.setText("Tarih: " + sdf.format(new Date(timestamp)));
            }
        } catch (Exception e) {
            binding.tvDate.setText("");
        }

        // Fotoğrafı göster
        if (observation.getPhotoPath() != null && !observation.getPhotoPath().isEmpty()) {
            File imgFile = new File(observation.getPhotoPath());
            if (imgFile.exists()) {
                Bitmap myBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
                binding.ivPhoto.setImageBitmap(myBitmap);
            } else {
                binding.ivPhoto.setImageResource(android.R.color.darker_gray); // Yedek görsel
            }
        } else {
            binding.ivPhoto.setImageResource(android.R.color.darker_gray);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}
