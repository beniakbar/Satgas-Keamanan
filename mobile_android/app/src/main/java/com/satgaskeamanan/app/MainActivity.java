package com.satgaskeamanan.app;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import com.satgaskeamanan.app.LoginActivity;
import com.satgaskeamanan.app.R;
import com.satgaskeamanan.app.api.APIService;
import com.satgaskeamanan.app.api.APIClient;
import com.satgaskeamanan.app.models.UserModel;

import com.satgaskeamanan.app.ui.admin.AdminDashboardFragment;
import com.satgaskeamanan.app.ui.petugas.PetugasDashboardFragment;

public class MainActivity extends AppCompatActivity {

    private APIService apiService;
    // KRITIS: Gunakan konstanta dari APIClient untuk konsistensi
    // Pastikan LoginActivity juga menggunakan konstanta yang sama
    public static final String PREFS_NAME = "SatgasPrefs";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Penting: Pastikan R.layout.activity_main ada dan memiliki FrameLayout dengan id fragment_container
        setContentView(R.layout.activity_main);

        // Inisialisasi API service
        apiService = APIClient.getAPIService(this);

        // Memulai pengecekan otentikasi
        checkAuthenticationAndLoadProfile();
    }

    private void checkAuthenticationAndLoadProfile() {

        // 1. Cek Token Akses di SharedPreferences
        // Menggunakan PREFS_NAME yang konsisten
        SharedPreferences settings = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String accessToken = settings.getString("AccessToken", null);

        if (accessToken == null) {
            // Jika token TIDAK ADA, langsung arahkan ke LoginActivity
            redirectToLogin();
            return;
        }

        // 2. Jika token ada, coba muat profil
        loadUserProfile();
    }

    private void loadUserProfile() {
        apiService.getUserProfile().enqueue(new Callback<UserModel>() {
            @Override
            public void onResponse(@NonNull Call<UserModel> call, @NonNull Response<UserModel> response) {
                if (response.isSuccessful() && response.body() != null) {
                    UserModel user = response.body();

                    // PROFIL BERHASIL DIMUAT (200 OK)
                    if (user.isAdmin()) {
                        showAdminDashboard();
                    } else if (user.isPetugas()) {
                        showPetugasDashboard();
                    } else {
                        Toast.makeText(MainActivity.this, "Peran pengguna tidak dikenali.", Toast.LENGTH_LONG).show();
                        redirectToLogin();
                    }

                } else if (response.code() == 401 || response.code() == 403) {
                    // Jika 401/403 (Token tidak valid), paksa logout
                    Toast.makeText(MainActivity.this, "Sesi kedaluwarsa. Silakan login ulang.", Toast.LENGTH_LONG).show();

                    APIClient.forceLogout(MainActivity.this);

                } else {
                    // Handle error
                    Toast.makeText(MainActivity.this, "Gagal memuat profil: " + response.code(), Toast.LENGTH_LONG).show();
                    redirectToLogin();
                }
            }

            @Override
            public void onFailure(@NonNull Call<UserModel> call, @NonNull Throwable t) {
                // Handle kegagalan koneksi
                Toast.makeText(MainActivity.this, "Koneksi gagal: " + t.getMessage(), Toast.LENGTH_LONG).show();
                redirectToLogin();
            }
        });
    }

    private void redirectToLogin() {
        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
        // Flags ini memastikan LoginActivity menjadi Activity tunggal dan membersihkan stack
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish(); // Tutup MainActivity agar tidak kembali dengan tombol back
    }

    private void showAdminDashboard() {
        // Asumsi R.id.fragment_container adalah FrameLayout di activity_main.xml
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, new AdminDashboardFragment())
                .commit();
    }

    private void showPetugasDashboard() {
        // Asumsi R.id.fragment_container adalah FrameLayout di activity_main.xml
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, new PetugasDashboardFragment())
                .commit();
    }
}