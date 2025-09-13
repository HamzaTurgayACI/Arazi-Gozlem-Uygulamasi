package com.example.arazigozlemappnew.ui

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import com.example.arazigozlemappnew.databinding.ActivityMainBinding
import com.example.arazigozlemappnew.viewmodel.ObservationViewModel
import com.example.arazigozlemappnew.database.Observation
import com.google.android.gms.location.LocationServices
import com.mapbox.geojson.Point
import com.mapbox.maps.CameraOptions
import com.mapbox.maps.Style
import com.mapbox.maps.plugin.annotation.AnnotationConfig
import com.mapbox.maps.plugin.annotation.annotations
import com.mapbox.maps.plugin.annotation.generated.PointAnnotationManager
import com.mapbox.maps.plugin.annotation.generated.PointAnnotation
import com.mapbox.maps.plugin.annotation.generated.PointAnnotationOptions
import com.mapbox.maps.plugin.annotation.generated.createPointAnnotationManager
import com.mapbox.maps.plugin.gestures.gestures
import com.mapbox.maps.plugin.locationcomponent.location

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var viewModel: ObservationViewModel
    private var selectedPoint: Point? = null
    private val fusedLocationClient by lazy { LocationServices.getFusedLocationProviderClient(this) }

    // Marker ile Observation eşleşmesi için map
    private val annotationObservationMap = mutableMapOf<PointAnnotation, Observation>()

    // Konum izni için launcher
    private val locationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) moveToCurrentLocation()
        else showToast("Konum izni reddedildi")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel = ViewModelProvider(this)[ObservationViewModel::class.java]

        binding.fabCurrentLocation.setOnClickListener { checkLocationPermission() }
        binding.fabAddObservation.setOnClickListener { addObservation() }
        binding.fabListObservations.setOnClickListener {
            startActivity(Intent(this, ObservationListActivity::class.java))
        }

        initializeMap()
    }

    private fun initializeMap() {
        binding.mapView.getMapboxMap().loadStyleUri(Style.MAPBOX_STREETS) {
            val annotationPlugin = binding.mapView.annotations
            val annotationManager = annotationPlugin.createPointAnnotationManager(AnnotationConfig())

            // Mevcut gözlemleri haritaya ekle + marker tıklama listener'ı ayarla
            loadObservations(annotationManager)

            // Konum eklentisi aktif
            binding.mapView.location.updateSettings {
                enabled = true
                pulsingEnabled = true
            }

            // Haritaya tıklama olayı: Marker ekle ve formu aç
            binding.mapView.gestures.addOnMapClickListener { point ->
                selectedPoint = point

                // Marker ekle
                val markerOptions = PointAnnotationOptions().withPoint(point)
                annotationManager.create(markerOptions)

                // Formu aç
                val intent = Intent(this, ObservationFormActivity::class.java).apply {
                    putExtra("latitude", point.latitude())
                    putExtra("longitude", point.longitude())
                }
                startActivity(intent)

                true
            }

            // Marker'a tıklama listener'ı: (YALNIZCA BİR KEZ EKLE!)
            annotationManager.addClickListener { clickedAnnotation ->
                val obs = annotationObservationMap[clickedAnnotation]
                if (obs != null) {
                    showObservationDetailDialog(obs)
                    true
                } else false
            }
        }
    }

    // Konum izni kontrolü ve talebi
    private fun checkLocationPermission() {
        when {
            ContextCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED -> moveToCurrentLocation()
            else -> locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    // Konuma git ve kamerayı oraya taşı
    private fun moveToCurrentLocation() {
        try {
            fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
                if (location != null) {
                    val point = Point.fromLngLat(location.longitude, location.latitude)
                    binding.mapView.getMapboxMap().setCamera(
                        CameraOptions.Builder()
                            .center(point)
                            .zoom(14.0)
                            .build()
                    )
                    selectedPoint = point
                } else {
                    showToast("Konum alınamadı")
                }
            }
        } catch (e: SecurityException) {
            showToast("Konum izni hatası: ${e.message}")
        }
    }

    // Sağ alttaki butondan gözlem ekle
    private fun addObservation() {
        selectedPoint?.let { point ->
            val intent = Intent(this, ObservationFormActivity::class.java).apply {
                putExtra("latitude", point.latitude())
                putExtra("longitude", point.longitude())
            }
            startActivity(intent)
        } ?: showToast("Lütfen haritadan konum seçin")
    }

    // Haritaya mevcut gözlemleri marker olarak ekle ve marker ile gözlem eşle
    private fun loadObservations(annotationManager: PointAnnotationManager) {
        viewModel.getAllObservations().observe(this) { observations ->
            annotationManager.deleteAll()
            annotationObservationMap.clear()
            observations.forEach { obs ->
                val point = Point.fromLngLat(obs.longitude, obs.latitude)
                val options = PointAnnotationOptions()
                    .withPoint(point)
                    .withTextField(obs.title)
                val annotation = annotationManager.create(options)
                annotationObservationMap[annotation] = obs
            }
        }
    }

    // Marker tıklandığında gösterilecek dialog
    private fun showObservationDetailDialog(observation: Observation) {
        AlertDialog.Builder(this)
            .setTitle(observation.title)
            .setMessage(
                "Kategori: ${observation.category}\n" +
                        "Açıklama: ${observation.description}\n" +
                        "Konum: ${observation.latitude}, ${observation.longitude}"
            )
            .setPositiveButton("Kapat", null)
            .setNegativeButton("Detaya Git") { _, _ ->
                openObservationDetailActivity(observation)
            }
            .show()
    }

    private fun openObservationDetailActivity(observation: Observation) {
        val intent = Intent(this, ObservationDetailActivity::class.java)
        intent.putExtra("observation_id", observation.id)
        startActivity(intent)
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    override fun onStart() {
        super.onStart()
        binding.mapView.onStart()
    }

    override fun onStop() {
        super.onStop()
        binding.mapView.onStop()
    }

    override fun onDestroy() {
        super.onDestroy()
        binding.mapView.onDestroy()
    }
}


