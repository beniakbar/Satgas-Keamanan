package com.satgaskeamanan.app;

import android.Manifest;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;
import androidx.core.content.ContextCompat;

import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.FusedLocationProviderClient;

import com.satgaskeamanan.app.api.APIClient;
import com.satgaskeamanan.app.api.APIService;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * PresensiActivity — full working version
 *
 * - Permissions: CAMERA & ACCESS_FINE_LOCATION
 * - Location: FusedLocationProviderClient, request updates until first fix then stop
 * - Camera: create file via getExternalFilesDir(), FileProvider for URI
 * - Upload: multipart fields must match backend:
 *      latitude, longitude, location_note, note, selfie_photo
 *
 * Paste this file and ensure manifest/provider and xml/file_paths exist.
 */
public class PresensiActivity extends AppCompatActivity {
    private static final String TAG = "PresensiActivity";

    private static final int REQ_PERMISSIONS = 100;
    private static final int REQ_IMAGE_CAPTURE = 101;

    // UI
    private TextView txtLocation;
    private ImageView imgSelfie;
    private EditText etNote;
    private Button btnAmbilFoto, btnKirim;

    // Location & Camera
    private FusedLocationProviderClient fusedLocationClient;
    private LocationCallback locationCallback;
    private LocationRequest locationRequest;
    private Location currentLocation;

    // Photo file path
    private String currentPhotoPath;

    // API
    private APIService apiService;

    // Required permissions
    private final String[] REQUIRED_PERMS = new String[]{
            Manifest.permission.CAMERA,
            Manifest.permission.ACCESS_FINE_LOCATION
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_presensi);

        // init UI
        txtLocation = findViewById(R.id.txtLocation);
        imgSelfie = findViewById(R.id.imgSelfie);
        etNote = findViewById(R.id.etNote);
        btnAmbilFoto = findViewById(R.id.btnAmbilFoto);
        btnKirim = findViewById(R.id.btnKirim);

        // init api & location
        apiService = APIClient.getAPIService(this);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        setupLocationRequest();
        setupLocationCallback();

        // check permissions
        if (!hasAllPermissions()) {
            ActivityCompat.requestPermissions(this, REQUIRED_PERMS, REQ_PERMISSIONS);
        } else {
            startLocationUpdates();
        }

        btnAmbilFoto.setOnClickListener(v -> dispatchTakePictureIntent());

        btnKirim.setOnClickListener(v -> {
            // protect against double-click
            btnKirim.setEnabled(false);
            new Handler(Looper.getMainLooper()).postDelayed(() -> btnKirim.setEnabled(true), 1500);

            if (currentLocation == null) {
                Toast.makeText(this, "Lokasi belum siap, tunggu sebentar...", Toast.LENGTH_SHORT).show();
                startLocationUpdates(); // try again
                return;
            }

            if (currentPhotoPath == null) {
                Toast.makeText(this, "Silakan ambil foto terlebih dahulu.", Toast.LENGTH_SHORT).show();
                return;
            }

            uploadPresensi();
        });
    }

    // ----------------- PERMISSIONS -----------------
    private boolean hasAllPermissions() {
        for (String p : REQUIRED_PERMS) {
            if (ContextCompat.checkSelfPermission(this, p) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQ_PERMISSIONS) {
            boolean granted = true;
            if (grantResults.length == 0) granted = false;
            for (int r : grantResults) {
                if (r != PackageManager.PERMISSION_GRANTED) {
                    granted = false;
                    break;
                }
            }
            if (granted) {
                startLocationUpdates();
            } else {
                Toast.makeText(this, "Izin lokasi/kamera diperlukan agar presensi berfungsi.", Toast.LENGTH_LONG).show();
            }
        }
    }

    // ----------------- LOCATION -----------------
    private void setupLocationRequest() {
        locationRequest = LocationRequest.create();
        // priority & interval
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(2000); // 2s
        locationRequest.setFastestInterval(1000);
    }

    private void setupLocationCallback() {
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                if (locationResult == null) return;
                currentLocation = locationResult.getLastLocation();
                if (currentLocation != null) {
                    txtLocation.setText(String.format(Locale.US, "Lat: %.6f | Lon: %.6f",
                            currentLocation.getLatitude(), currentLocation.getLongitude()));

                    // we got a fix — stop updates to save battery
                    try {
                        fusedLocationClient.removeLocationUpdates(locationCallback);
                    } catch (SecurityException se) {
                        Log.w(TAG, "removeLocationUpdates denied: " + se.getMessage());
                    }
                }
            }
        };
    }

    private void startLocationUpdates() {
        if (!hasAllPermissions()) return;
        try {
            fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper());
            // Also try to get last known quickly
            fusedLocationClient.getLastLocation()
                    .addOnSuccessListener(loc -> {
                        if (loc != null) {
                            currentLocation = loc;
                            txtLocation.setText(String.format(Locale.US, "Lat: %.6f | Lon: %.6f",
                                    loc.getLatitude(), loc.getLongitude()));
                        }
                    });
        } catch (SecurityException se) {
            Log.e(TAG, "startLocationUpdates SecurityException: " + se.getMessage());
        }
    }

    // ----------------- CAMERA (FileProvider) -----------------
    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        if (storageDir != null && !storageDir.exists()) storageDir.mkdirs();
        File image = File.createTempFile(imageFileName, ".jpg", storageDir);
        currentPhotoPath = image.getAbsolutePath();
        return image;
    }

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) == null) {
            Toast.makeText(this, "Kamera tidak tersedia di perangkat ini.", Toast.LENGTH_SHORT).show();
            return;
        }

        File photoFile = null;
        try {
            photoFile = createImageFile();
        } catch (IOException ex) {
            Log.e(TAG, "createImageFile failed: " + ex.getMessage());
            Toast.makeText(this, "Gagal membuat file foto.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (photoFile != null) {
            Uri photoURI = FileProvider.getUriForFile(this,
                    getPackageName() + ".provider",
                    photoFile);
            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
            takePictureIntent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);
            try {
                startActivityForResult(takePictureIntent, REQ_IMAGE_CAPTURE);
            } catch (ActivityNotFoundException e) {
                Toast.makeText(this, "Kamera tidak ditemukan.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQ_IMAGE_CAPTURE && resultCode == Activity.RESULT_OK) {
            if (currentPhotoPath != null) {
                File f = new File(currentPhotoPath);
                if (f.exists()) {
                    imgSelfie.setImageURI(Uri.fromFile(f));
                    Toast.makeText(this, "Foto tersimpan.", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "File foto tidak ditemukan.", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, "Path foto kosong.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    // ----------------- UPLOAD PRESENSI -----------------
    private void uploadPresensi() {
        if (currentLocation == null) {
            Toast.makeText(this, "Lokasi belum tersedia.", Toast.LENGTH_SHORT).show();
            startLocationUpdates();
            return;
        }

        if (currentPhotoPath == null) {
            Toast.makeText(this, "Silakan ambil foto terlebih dahulu.", Toast.LENGTH_SHORT).show();
            return;
        }

        File photoFile = new File(currentPhotoPath);
        if (!photoFile.exists()) {
            Toast.makeText(this, "File foto tidak ditemukan.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Prepare request bodies (ensure dot decimal using Locale.US)
        String latStr = String.format(Locale.US, "%.6f", currentLocation.getLatitude());
        String lonStr = String.format(Locale.US, "%.6f", currentLocation.getLongitude());
        String locationNote = "Lokasi otomatis";
        String noteText = etNote.getText() != null && !etNote.getText().toString().trim().isEmpty()
                ? etNote.getText().toString().trim() : "-";

        RequestBody latRB = RequestBody.create(MediaType.parse("text/plain"), latStr);
        RequestBody lonRB = RequestBody.create(MediaType.parse("text/plain"), lonStr);
        RequestBody locRB = RequestBody.create(MediaType.parse("text/plain"), locationNote);
        RequestBody noteRB = RequestBody.create(MediaType.parse("text/plain"), noteText);

        RequestBody fileRB = RequestBody.create(MediaType.parse("image/jpeg"), photoFile);
        MultipartBody.Part photoPart = MultipartBody.Part.createFormData("selfie_photo", photoFile.getName(), fileRB);

        // Call API
        Call<Void> call = apiService.kirimPresensi(latRB, lonRB, locRB, noteRB, photoPart);
        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> response) {
                if (response.isSuccessful()) {
                    // Success feedback
                    Toast.makeText(PresensiActivity.this, "Presensi berhasil dikirim!", Toast.LENGTH_LONG).show();
                    Log.d(TAG, "Presensi upload success: " + response.code());

                    // small delay so user sees toast
                    new Handler(Looper.getMainLooper()).postDelayed(() -> finish(), 900);
                } else {
                    // Read server error body if available
                    String err = "Kode: " + response.code();
                    try {
                        if (response.errorBody() != null) {
                            err = response.errorBody().string();
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "errorBody read failed: " + e.getMessage());
                    }
                    Log.e(TAG, "Presensi upload failed: " + err);
                    Toast.makeText(PresensiActivity.this, "Gagal upload: " + response.code(), Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {
                Log.e(TAG, "Presensi upload onFailure: " + t.getMessage(), t);
                Toast.makeText(PresensiActivity.this, "Gagal terhubung: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    // ----------------- cleanup -----------------
    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            fusedLocationClient.removeLocationUpdates(locationCallback);
        } catch (Exception ignored) { }
    }
}
