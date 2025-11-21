package com.satgaskeamanan.app;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

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

public class LaporanActivity extends AppCompatActivity {

    private static final int REQUEST_CAMERA_PERMISSION = 200;
    private static final int REQUEST_IMAGE_CAPTURE = 201;

    private EditText etTitle, etDescription;
    private ImageView imgPreview;
    private Button btnAmbilFoto, btnKirim;

    private String currentPhotoPath;
    private APIService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_laporan);

        // Init Views
        etTitle = findViewById(R.id.etTitle);
        etDescription = findViewById(R.id.etDescription);
        imgPreview = findViewById(R.id.imgLaporanPreview);
        btnAmbilFoto = findViewById(R.id.btnAmbilFotoLaporan);
        btnKirim = findViewById(R.id.btnKirimLaporan);

        apiService = APIClient.getAPIService(this);

        // Actions
        btnAmbilFoto.setOnClickListener(v -> checkPermissionAndOpenCamera());
        btnKirim.setOnClickListener(v -> uploadLaporan());
    }

    // --- CAMERA LOGIC ---

    private void checkPermissionAndOpenCamera() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA_PERMISSION);
        } else {
            openCamera();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CAMERA_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openCamera();
            } else {
                Toast.makeText(this, "Izin kamera diperlukan", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void openCamera() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (intent.resolveActivity(getPackageManager()) != null) {
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                Toast.makeText(this, "Error creating file", Toast.LENGTH_SHORT).show();
            }

            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(this, getPackageName() + ".provider", photoFile);
                intent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(intent, REQUEST_IMAGE_CAPTURE);
            }
        }
    }

    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(new Date());
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile("LAPORAN_" + timeStamp + "_", ".jpg", storageDir);
        currentPhotoPath = image.getAbsolutePath();
        return image;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == Activity.RESULT_OK) {
            File f = new File(currentPhotoPath);
            imgPreview.setImageURI(Uri.fromFile(f));
            imgPreview.setScaleType(ImageView.ScaleType.CENTER_CROP);
        }
    }

    // --- UPLOAD LOGIC ---

    private void uploadLaporan() {
        String title = etTitle.getText().toString().trim();
        String desc = etDescription.getText().toString().trim();

        if (title.isEmpty() || desc.isEmpty()) {
            Toast.makeText(this, "Judul dan Deskripsi wajib diisi", Toast.LENGTH_SHORT).show();
            return;
        }

        if (currentPhotoPath == null) {
            Toast.makeText(this, "Harap sertakan foto bukti", Toast.LENGTH_SHORT).show();
            return;
        }

        File file = new File(currentPhotoPath);

        // Prepare RequestBody
        RequestBody titleBody = RequestBody.create(MediaType.parse("text/plain"), title);
        RequestBody descBody = RequestBody.create(MediaType.parse("text/plain"), desc);
        
        // Placeholder for coordinates (assuming 0,0 for now or get from LocationService if implemented)
        RequestBody latBody = RequestBody.create(MediaType.parse("text/plain"), "0.0");
        RequestBody longBody = RequestBody.create(MediaType.parse("text/plain"), "0.0");

        RequestBody requestFile = RequestBody.create(MediaType.parse("image/jpeg"), file);
        MultipartBody.Part body = MultipartBody.Part.createFormData("photo", file.getName(), requestFile);

        // Call API - Updated with 5 arguments
        apiService.kirimLaporan(latBody, longBody, titleBody, descBody, body).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(LaporanActivity.this, "Laporan Berhasil Dikirim!", Toast.LENGTH_LONG).show();
                    finish();
                } else {
                    Toast.makeText(LaporanActivity.this, "Gagal: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {
                Toast.makeText(LaporanActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
