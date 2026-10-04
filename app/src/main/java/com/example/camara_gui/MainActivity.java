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

import com.example.camara_gui.data.AppDatabase;
import com.example.camara_gui.data.Photo;
import com.example.camara_gui.data.PhotoDao;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import android.content.Intent;
import com.example.camara_gui.imageList.PhotoActivity;

import android.app.AlertDialog;
import android.text.InputType;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import java.net.HttpURLConnection;
import java.net.URL;
public class MainActivity extends AppCompatActivity {

    private static final int CAMERA_PERMISSION_CODE = 100;
    private static final int LOCATION_PERMISSION_CODE = 101;

    private PreviewView previewView;
    private ImageCapture imageCapture;

    private FusedLocationProviderClient fusedLocationClient;

    private boolean useFrontCamera = false;
    private PhotoDao photoDao;
    private final ExecutorService databaseExecutor = Executors.newSingleThreadExecutor();

    private volatile boolean stopPing = false;
    private final ExecutorService pingExecutor =Executors.newSingleThreadExecutor();

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        previewView = findViewById(R.id.previewView);

        Button buttonPhoto = findViewById(R.id.buttonPhoto);
        Button buttonSwitchCamera = findViewById(R.id.buttonSwitchCamera);
        Button buttonList = findViewById(R.id.buttonList);

        buttonList.setOnClickListener(v -> {
            Intent intent = new Intent(
                    MainActivity.this,
                    PhotoActivity.class
            );
            startActivity(intent);
        });
        Button buttonPing = findViewById(R.id.buttonPing);

        buttonPing.setOnClickListener(v -> showPingDialog());

        fusedLocationClient =LocationServices.getFusedLocationProviderClient(this);

        AppDatabase database = AppDatabase.getDatabase(this);
        photoDao = database.photoDao();

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
                        getString(R.string.camera_error) + e.getMessage(),
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
                            @NonNull ImageCapture.OutputFileResults outputFileResults
                    ) {

                        Uri savedUri =
                                outputFileResults.getSavedUri();

                        if (savedUri == null) {
                            return;
                        }

                        Photo photo = new Photo(
                                savedUri.toString(),
                                System.currentTimeMillis(),
                                latitude,
                                longitude
                        );

                        databaseExecutor.execute(() -> {

                            photoDao.insert(photo);

                            int totalPhotos =
                                    photoDao.getAll().size();

                            runOnUiThread(() -> {

                                Toast.makeText(
                                        MainActivity.this,
                                        getString(R.string.photo_saved)
                                                + getString(R.string.photos_in_room) + totalPhotos
                                                + "\nLat: " + latitude
                                                + "\nLon: " + longitude,
                                        Toast.LENGTH_LONG
                                ).show();
                            });
                        });

                    }

                    @Override
                    public void onError(
                            @NonNull ImageCaptureException exception
                    ) {

                        Toast.makeText(
                                MainActivity.this,
                                getString(R.string.save_error)
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
                        getString(R.string.camera_permission_required),
                        Toast.LENGTH_SHORT
                ).show();
            }
        }
    }
    private void showPingDialog() {

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);

        int padding = 50;

        layout.setPadding(padding, padding, padding, padding );

        EditText inputAttempts = new EditText(this);
        inputAttempts.setHint(getString(R.string.number_of_tries));
        inputAttempts.setInputType(InputType.TYPE_CLASS_NUMBER);

        TextView textResults = new TextView(this);
        textResults.setText(
                getString(R.string.success) + ": 0\n" +
                getString(R.string.failure) + ": 0"
        );

        layout.addView(inputAttempts);
        layout.addView(textResults);

        AlertDialog dialog =
                new AlertDialog.Builder(this)
                        .setTitle(getString(R.string.google_ping))
                        .setView(layout)
                        .setPositiveButton(
                                getString(R.string.start),
                                null
                        )
                        .setNegativeButton(
                                getString(R.string.stop),
                                null
                        )
                        .create();

        dialog.show();

        dialog.getButton(AlertDialog.BUTTON_POSITIVE)
                .setOnClickListener(v -> {

                    String value =
                            inputAttempts.getText().toString();

                    if (value.isEmpty()) {
                        return;
                    }
                    int attempts = Integer.parseInt(value);
                    stopPing = false;
                    pingExecutor.execute(() -> {
                        int success = 0;
                        int failures = 0;
                        for (int i = 0; i < attempts; i++) {
                            if (stopPing) {
                                break;
                            }
                            try {
                                URL url =new URL("https://www.google.com");
                                HttpURLConnection connection =(HttpURLConnection) url.openConnection();
                                connection.setRequestMethod("GET");
                                connection.setConnectTimeout(3000);
                                connection.setReadTimeout(3000);
                                int responseCode = connection.getResponseCode();
                                if (responseCode >= 200 && responseCode < 400) {
                                    success++;
                                } else {
                                    failures++;
                                }
                                connection.disconnect();
                            } catch (Exception e) {
                                failures++;
                            }
                            int finalSuccess = success;
                            int finalFailures = failures;
                            runOnUiThread(() -> {
                                textResults.setText(
                                        getString(R.string.success)
                                                + ": "
                                                + finalSuccess
                                                + "\n"
                                                + getString(R.string.failure)
                                                + ": "
                                                + finalFailures
                                );
                            });
                        }
                    });
                });
        dialog.getButton(AlertDialog.BUTTON_NEGATIVE)
                .setOnClickListener(v -> {
                    stopPing = true;

                });
    }
}