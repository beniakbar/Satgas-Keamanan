package com.satgaskeamanan.app;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.content.SharedPreferences;
import android.content.Intent;
import android.view.View; // Correct Import
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.satgaskeamanan.app.api.APIService;
import com.satgaskeamanan.app.api.APIClient;
import com.satgaskeamanan.app.models.LoginRequest;
import com.satgaskeamanan.app.models.TokenResponse;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {

    private EditText etEmail, etPassword;
    private Button btnLogin;
    private APIService apiService;
    public static final String PREFS_NAME = "SatgasPrefs";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        etEmail = findViewById(R.id.et_email);
        etPassword = findViewById(R.id.et_password);
        btnLogin = findViewById(R.id.btn_login);
        apiService = APIClient.getAPIService(this);

        // Cek status login saat aplikasi dibuka
        checkLoginStatus();

        btnLogin.setOnClickListener(v -> attemptLogin());

        // 1. Initialize the Register text view
        TextView tvRegister = findViewById(R.id.tv_register);

        // 2. Set the click listener using standard Android View
        tvRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Navigate to RegisterActivity
                Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivity(intent);
            }
        });
    }

    private void checkLoginStatus() {
        SharedPreferences settings = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        String accessToken = settings.getString("AccessToken", null);

        if (accessToken != null) {
            // Jika token ada, pengguna dianggap sudah login dan diarahkan ke Main Activity
            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        }
    }

    private void attemptLogin() {
        String email = etEmail.getText().toString();
        String password = etPassword.getText().toString();

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Email dan password wajib diisi.", Toast.LENGTH_SHORT).show();
            return;
        }

        LoginRequest loginRequest = new LoginRequest(email, password);

        apiService.login(loginRequest).enqueue(new Callback<TokenResponse>() {
            @Override
            public void onResponse(@NonNull Call<TokenResponse> call, @NonNull Response<TokenResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    // Login Berhasil
                    TokenResponse tokenResponse = response.body();
                    saveTokens(tokenResponse.getAccessToken(), tokenResponse.getRefreshToken());
                    Toast.makeText(LoginActivity.this, "Login Berhasil! Selamat datang.", Toast.LENGTH_SHORT).show();

                    // Pindah ke Main Activity (misalnya)
                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                    startActivity(intent);
                    finish();
                } else {
                    // Login Gagal (Misalnya: 401 Unauthorized)
                    Toast.makeText(LoginActivity.this, "Login Gagal. Cek kredensial Anda.", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<TokenResponse> call, @NonNull Throwable t) {
                // Kesalahan Jaringan
                Toast.makeText(LoginActivity.this, "Gagal terhubung ke server: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    // Fungsi untuk menyimpan token ke SharedPreferences
    private void saveTokens(String accessToken, String refreshToken) {
        SharedPreferences settings = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString("AccessToken", accessToken);
        editor.putString("RefreshToken", refreshToken);
        editor.apply();
    }
}
