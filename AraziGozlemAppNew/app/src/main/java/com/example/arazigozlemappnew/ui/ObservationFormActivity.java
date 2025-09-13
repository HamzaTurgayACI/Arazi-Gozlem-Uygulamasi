package com.example.arazigozlemappnew.ui;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.lifecycle.ViewModelProvider;

import com.example.arazigozlemappnew.database.Observation;
import com.example.arazigozlemappnew.databinding.ActivityObservationFormBinding;
import com.example.arazigozlemappnew.viewmodel.ObservationViewModel;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ObservationFormActivity extends AppCompatActivity {
    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final int REQUEST_CAMERA_PERMISSION = 2;

    private ActivityObservationFormBinding binding;
    private ObservationViewModel observationViewModel;
    private double latitude, longitude;
    private String currentPhotoPath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityObservationFormBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Initialize ViewModel
        observationViewModel = new ViewModelProvider(this).get(ObservationViewModel.class);

        // Get location from intent
        latitude = getIntent().getDoubleExtra("latitude", 0);
        longitude = getIntent().getDoubleExtra("longitude", 0);

        // Set location text
        binding.tvLocation.setText(String.format(Locale.getDefault(),
                "Enlem: %.6f, Boylam: %.6f", latitude, longitude));

        // Set click listeners
        binding.btnTakePhoto.setOnClickListener(v -> checkCameraPermissionAndDispatch());
        binding.btnSave.setOnClickListener(v -> saveObservation());
    }

    // Kamera izni kontrol et, gerekirse iste
    private void checkCameraPermissionAndDispatch() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA_PERMISSION);
        } else {
            dispatchTakePictureIntent();
        }
    }

    // Kamera izni sonucu
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CAMERA_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                dispatchTakePictureIntent();
            } else {
                Toast.makeText(this, "Kamera izni gerekli!", Toast.LENGTH_SHORT).show();
            }
        }
    }

    // Fotoğraf çekme işlemi
    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                Toast.makeText(this, "Fotoğraf dosyası oluşturulamadı", Toast.LENGTH_SHORT).show();
            }

            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(this,
                        "com.example.arazigozlemappnew.fileprovider", // Manifest ile aynı!
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
            }
        }
    }

    // Fotoğraf dosyası oluştur
    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir("Pictures");
        File image = File.createTempFile(
                imageFileName,
                ".jpg",
                storageDir
        );

        currentPhotoPath = image.getAbsolutePath();
        return image;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            binding.ivPhoto.setImageURI(Uri.parse(currentPhotoPath));
        }
    }

    // Gözlem kaydet
    private void saveObservation() {
        String title = binding.etTitle.getText().toString().trim();
        String category = binding.etCategory.getText().toString().trim();
        String description = binding.etDescription.getText().toString().trim();

        if (title.isEmpty()) {
            Toast.makeText(this, "Başlık boş olamaz", Toast.LENGTH_SHORT).show();
            return;
        }

        if (currentPhotoPath == null) {
            Toast.makeText(this, "Lütfen bir fotoğraf çekin", Toast.LENGTH_SHORT).show();
            return;
        }

        Observation observation = new Observation(title, description, category,
                latitude, longitude, currentPhotoPath);

        observationViewModel.insert(observation);
        Toast.makeText(this, "Gözlem kaydedildi", Toast.LENGTH_SHORT).show();
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}
