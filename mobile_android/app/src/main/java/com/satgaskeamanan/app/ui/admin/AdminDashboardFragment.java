package com.satgaskeamanan.app.ui.admin;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import com.satgaskeamanan.app.R;
import com.satgaskeamanan.app.api.APIClient;
import com.satgaskeamanan.app.ui.admin.laporanharian.HarianPresensiFragment;
import com.satgaskeamanan.app.ui.admin.laporanlist.LaporanListFragment;
import com.satgaskeamanan.app.ui.admin.petugaslist.PetugasListFragment;

public class AdminDashboardFragment extends Fragment implements View.OnClickListener {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_admin_dashboard, container, false);

        // Inisialisasi CardView
        CardView cardPetugas = view.findViewById(R.id.card_petugas);
        CardView cardLaporan = view.findViewById(R.id.card_laporan);
        CardView cardPresensi = view.findViewById(R.id.card_presensi);
        CardView cardLogout = view.findViewById(R.id.card_logout);

        // Set Listener untuk klik
        cardPetugas.setOnClickListener(this);
        cardLaporan.setOnClickListener(this);
        cardPresensi.setOnClickListener(this);
        cardLogout.setOnClickListener(this);

        return view;
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();

        if (id == R.id.card_petugas) {
            // Arahkan ke Daftar Petugas
            navigateToFragment(new PetugasListFragment());

        } else if (id == R.id.card_laporan) {
            // Arahkan ke Monitoring Laporan
            navigateToFragment(new LaporanListFragment());

        } else if (id == R.id.card_presensi) {
            // Arahkan ke Rekap Presensi Harian
            navigateToFragment(new HarianPresensiFragment());

        } else if (id == R.id.card_logout) {
            // Logika Logout
            APIClient.forceLogout(requireContext());
            Toast.makeText(requireContext(), "Anda telah logout.", Toast.LENGTH_SHORT).show();
        }
    }

    // Metode helper untuk memindahkan Fragment
    private void navigateToFragment(Fragment fragment) {
        getParentFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .addToBackStack(null) // Penting agar user bisa kembali ke dashboard
                .commit();
    }
}
