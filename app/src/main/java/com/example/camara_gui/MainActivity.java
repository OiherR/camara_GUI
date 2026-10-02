package com.example.camara_gui;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import android.Manifest;
import android.content.ContentValues;
import android.content.pm.PackageManager;
import android.net.Uri;

import android.provider.MediaStore;
import android.widget.Button;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.common.util.concurrent.ListenableFuture;

public class MainActivity extends AppCompatActivity {

    private static final int CAMERA_PERMISSION_CODE = 100;
    private static final int LOCATION_PERMISSION_CODE = 101;

    private PreviewView previewView;
    private ImageCapture imageCapture;

    private FusedLocationProviderClient fusedLocationClient;

    private boolean useFrontCamera = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        previewView = findViewById(R.id.previewView);

        Button buttonPhoto = findViewById(R.id.buttonPhoto);
        Button buttonSwitchCamera = findViewById(R.id.buttonSwitchCamera);

        fusedLocationClient =LocationServices.getFusedLocationProviderClient(this);

        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED) {

            startCamera();
        } else {
            ActivityCompat.requestPermissions(
                    this,
                    new String[]{Manifest.permission.CAMERA},
                    CAMERA_PERMISSION_CODE
            );
        }

        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
        ) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(
                    this,
                    new String[]{
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION
                    },
                    LOCATION_PERMISSION_CODE
            );
        }

        buttonPhoto.setOnClickListener(v -> getLocationAndTakePhoto());

        buttonSwitchCamera.setOnClickListener(v -> {
            useFrontCamera = !useFrontCamera;
            startCamera();
        });
    }
    private void startCamera() {

        ListenableFuture<ProcessCameraProvider> cameraProviderFuture =ProcessCameraProvider.getInstance(this);

        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider =
                        cameraProviderFuture.get();

                Preview preview =
                        new Preview.Builder().build();

                preview.setSurfaceProvider(
                        previewView.getSurfaceProvider()
                );

                imageCapture =
                        new ImageCapture.Builder().build();

                CameraSelector cameraSelector;

                if (useFrontCamera) {
                    cameraSelector =
                            CameraSelector.DEFAULT_FRONT_CAMERA;
                } else {
                    cameraSelector =
                            CameraSelector.DEFAULT_BACK_CAMERA;
                }

                cameraProvider.unbindAll();

                cameraProvider.bindToLifecycle(
                        this,
                        cameraSelector,
                        preview,
                        imageCapture
                );

            } catch (Exception e) {
                Toast.makeText(
                        this,
                        "Error al iniciar la cámara: " + e.getMessage(),
                        Toast.LENGTH_LONG
                ).show();
            }

        }, ContextCompat.getMainExecutor(this));
    }
    private void getLocationAndTakePhoto() {

        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED) {

            fusedLocationClient.getLastLocation()
                    .addOnSuccessListener(location -> {
                        if (location != null) {
                            double latitude =
                                    location.getLatitude();
                            double longitude =
                                    location.getLongitude();
                            takePhoto(
                                    latitude,
                                    longitude
                            );
                        } else {
                            takePhoto(
                                    null,
                                    null
                            );
                        }
                    });
        } else {
            takePhoto(
                    null,
                    null
            );
        }
    }
    private void takePhoto(Double latitude, Double longitude ) {

        if (imageCapture == null) {
            return;
        }
        String fileName =
                "IMG_" + System.currentTimeMillis();

        ContentValues contentValues =
                new ContentValues();

        contentValues.put(
                MediaStore.MediaColumns.DISPLAY_NAME,
                fileName
        );

        contentValues.put(
                MediaStore.MediaColumns.MIME_TYPE,
                "image/jpeg"
        );

        if (android.os.Build.VERSION.SDK_INT
                >= android.os.Build.VERSION_CODES.Q) {

            contentValues.put(
                    MediaStore.Images.Media.RELATIVE_PATH,
                    "Pictures/CameraApp"
            );
        }

        ImageCapture.OutputFileOptions outputOptions =
                new ImageCapture.OutputFileOptions.Builder(
                        getContentResolver(),
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                        contentValues
                ).build();

        imageCapture.takePicture(
                outputOptions,
                ContextCompat.getMainExecutor(this),

                new ImageCapture.OnImageSavedCallback() {
                    @Override
                    public void onImageSaved(
                            @NonNull ImageCapture.OutputFileResults outputFileResults) {

                        Uri savedUri =
                                outputFileResults.getSavedUri();

                        Toast.makeText(
                                MainActivity.this,

                                "Foto guardada\n"
                                        + "Lat: " + latitude
                                        + "\nLon: " + longitude
                                        + "\nURI: " + savedUri,

                                Toast.LENGTH_LONG
                        ).show();
                    }

                    @Override
                    public void onError(
                            @NonNull ImageCaptureException exception
                    ) {

                        Toast.makeText(
                                MainActivity.this,
                                "Error al guardar: "
                                        + exception.getMessage(),
                                Toast.LENGTH_LONG
                        ).show();
                    }
                }
        );
    }

    @Override
    public void onRequestPermissionsResult(
            int requestCode,
            @NonNull String[] permissions,
            @NonNull int[] grantResults
    ) {
        super.onRequestPermissionsResult(
                requestCode,
                permissions,
                grantResults
        );
        if (requestCode == CAMERA_PERMISSION_CODE) {
            if (grantResults.length > 0
                    && grantResults[0]
                    == PackageManager.PERMISSION_GRANTED) {
                startCamera();
            } else {
                Toast.makeText(
                        this,
                        "Se necesita permiso de cámara",
                        Toast.LENGTH_SHORT
                ).show();
            }
        }
    }
}