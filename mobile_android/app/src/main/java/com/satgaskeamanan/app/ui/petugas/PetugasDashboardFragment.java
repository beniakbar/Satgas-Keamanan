package com.satgaskeamanan.app.ui.petugas;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import com.satgaskeamanan.app.LoginActivity;
import com.satgaskeamanan.app.PresensiActivity;
import com.satgaskeamanan.app.LaporanActivity; // Uncommented this line
import com.satgaskeamanan.app.R;

import static android.content.Context.MODE_PRIVATE;

public class PetugasDashboardFragment extends Fragment {

    private CardView cardPresensi, cardLaporan;
    private Button btnLogout;
    private TextView tvWelcome;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_petugas_dashboard, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize Views
        cardPresensi = view.findViewById(R.id.card_presensi);
        cardLaporan = view.findViewById(R.id.card_laporan);
        btnLogout = view.findViewById(R.id.btn_logout_petugas);
        tvWelcome = view.findViewById(R.id.tv_welcome_petugas);

        // --- 1. ACTION: Go to Attendance (Presensi) ---
        cardPresensi.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), PresensiActivity.class);
            startActivity(intent);
        });

        // --- 2. ACTION: Go to Report (Laporan) ---
        cardLaporan.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), LaporanActivity.class);
            startActivity(intent);
        });

        // --- 3. ACTION: Logout ---
        btnLogout.setOnClickListener(v -> {
            // Clear SharedPreferences (Token)
            SharedPreferences settings = getActivity().getSharedPreferences("SatgasPrefs", MODE_PRIVATE);
            SharedPreferences.Editor editor = settings.edit();
            editor.clear();
            editor.apply();

            // Go back to Login
            Intent intent = new Intent(getActivity(), LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            getActivity().finish();
        });
    }
}
