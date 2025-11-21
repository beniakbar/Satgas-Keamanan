package com.satgaskeamanan.app;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.satgaskeamanan.app.api.APIClient;
import com.satgaskeamanan.app.api.APIService;
import com.satgaskeamanan.app.models.RegisterRequest;
import com.satgaskeamanan.app.models.RegisterResponse;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RegisterActivity extends AppCompatActivity {

    private EditText etName, etEmail, etPassword, etConfirmPassword;
    private Button btnRegister;
    private TextView tvLoginLink;
    private APIService apiService; // Declare API Service

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // Initialize API Service
        apiService = APIClient.getAPIService(this);

        // Initialize UI components
        etName = findViewById(R.id.et_reg_name);
        etEmail = findViewById(R.id.et_reg_email);
        etPassword = findViewById(R.id.et_reg_password);
        etConfirmPassword = findViewById(R.id.et_reg_confirm_password);
        btnRegister = findViewById(R.id.btn_register);
        tvLoginLink = findViewById(R.id.tv_login_link);

        // Handle Register Button Click
        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                performRegistration();
            }
        });

        // Handle "Back to Login" Click
        tvLoginLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void performRegistration() {
        String name = etName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String confirmPassword = etConfirmPassword.getText().toString().trim();

        // Basic Validation
        if (name.isEmpty() || email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Semua kolom wajib diisi!", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!password.equals(confirmPassword)) {
            Toast.makeText(this, "Password dan Konfirmasi Password tidak cocok!", Toast.LENGTH_SHORT).show();
            return;
        }

        // --- API CALL LOGIC START ---
        RegisterRequest registerRequest = new RegisterRequest(name, email, password);

        apiService.register(registerRequest).enqueue(new Callback<RegisterResponse>() {
            @Override
            public void onResponse(@NonNull Call<RegisterResponse> call, @NonNull Response<RegisterResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    // Success
                    Toast.makeText(RegisterActivity.this, "Registrasi Berhasil! Silakan Login.", Toast.LENGTH_LONG).show();

                    // Close Register Activity and return to Login
                    finish();
                } else {
                    // Server error (e.g., email already taken)
                    Toast.makeText(RegisterActivity.this, "Registrasi Gagal: " + response.message(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<RegisterResponse> call, @NonNull Throwable t) {
                // Network error
                Toast.makeText(RegisterActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
        // --- API CALL LOGIC END ---
    }
}
