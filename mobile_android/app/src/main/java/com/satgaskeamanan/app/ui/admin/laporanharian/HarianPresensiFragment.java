package com.satgaskeamanan.app.ui.admin.laporanharian;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide; // Import Glide
import com.satgaskeamanan.app.R;
import com.satgaskeamanan.app.api.APIClient;
import com.satgaskeamanan.app.api.APIService;
import com.satgaskeamanan.app.models.AdminPresensiModel;
import com.satgaskeamanan.app.models.HarianPresensiReportModel;
import com.satgaskeamanan.app.models.PetugasStatusPresensiModel;

import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HarianPresensiFragment extends Fragment {

    private TextView tvReportDate, tvTotalPetugas, tvPetugasHadir, tvPetugasBelumHadir;
    private Button btnPilihTanggal;
    private RecyclerView recyclerView;
    private HarianPresensiAdapter adapter;
    private ProgressBar progressBar;
    private APIService apiService;

    public HarianPresensiFragment() {
        super(R.layout.fragment_harian_presensi);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Inisialisasi View
        tvReportDate = view.findViewById(R.id.tv_report_date);
        btnPilihTanggal = view.findViewById(R.id.btn_pilih_tanggal);
        tvTotalPetugas = view.findViewById(R.id.tv_total_petugas);
        tvPetugasHadir = view.findViewById(R.id.tv_petugas_hadir);
        tvPetugasBelumHadir = view.findViewById(R.id.tv_petugas_belum_hadir);
        recyclerView = view.findViewById(R.id.rv_harian_presensi);
        progressBar = view.findViewById(R.id.progress_bar);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        apiService = APIClient.getAPIService(requireContext());

        // Load data hari ini (default)
        fetchHarianPresensiReport(null);

        // Setup listener untuk tombol pilih tanggal
        btnPilihTanggal.setOnClickListener(v -> showDatePickerDialog());
    }

    private void showDatePickerDialog() {
        final Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                requireContext(),
                (view, year1, month1, dayOfMonth) -> {
                    String selectedDate = String.format(Locale.US, "%d-%02d-%02d", year1, month1 + 1, dayOfMonth);
                    fetchHarianPresensiReport(selectedDate);
                },
                year, month, day);
        
        datePickerDialog.show();
    }

    private void fetchHarianPresensiReport(@Nullable String date) {
        progressBar.setVisibility(View.VISIBLE);

        apiService.getHarianPresensiReport(date).enqueue(new Callback<HarianPresensiReportModel>() {
            @Override
            public void onResponse(@NonNull Call<HarianPresensiReportModel> call, @NonNull Response<HarianPresensiReportModel> response) {
                progressBar.setVisibility(View.GONE);

                if (response.isSuccessful() && response.body() != null) {
                    HarianPresensiReportModel report = response.body();
                    updateSummaryUI(report);

                    List<PetugasStatusPresensiModel> dataList = report.getData();
                    
                    // Inisialisasi adapter dengan Listener
                    adapter = new HarianPresensiAdapter(requireContext(), dataList, petugas -> {
                        // Ketika item diklik, tampilkan dialog detail
                        showDetailDialog(petugas);
                    });
                    
                    recyclerView.setAdapter(adapter);
                } else {
                    Toast.makeText(requireContext(), "Gagal memuat laporan harian: " + response.code(), Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<HarianPresensiReportModel> call, @NonNull Throwable t) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(requireContext(), "Error Jaringan: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void showDetailDialog(PetugasStatusPresensiModel petugas) {
        // Pastikan kita memiliki data presensi
        AdminPresensiModel presensi = petugas.getLastPresensi();
        if (presensi == null) return;

        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_detail_presensi, null);
        builder.setView(dialogView);

        AlertDialog dialog = builder.create();

        // Inisialisasi View di Dialog
        ImageView ivFoto = dialogView.findViewById(R.id.iv_detail_foto);
        TextView tvNama = dialogView.findViewById(R.id.tv_detail_nama);
        TextView tvWaktu = dialogView.findViewById(R.id.tv_detail_waktu);
        TextView tvLokasi = dialogView.findViewById(R.id.tv_detail_lokasi);
        TextView tvLokasiNote = dialogView.findViewById(R.id.tv_detail_lokasi_note);
        TextView tvNote = dialogView.findViewById(R.id.tv_detail_note);
        Button btnClose = dialogView.findViewById(R.id.btn_close_dialog);

        // Set Data
        tvNama.setText(petugas.getFullName());
        tvWaktu.setText("Waktu: " + presensi.getTimestamp());
        tvLokasi.setText(presensi.getLatitude() + ", " + presensi.getLongitude());
        
        // Handle null strings
        String locNote = presensi.getLocationNote();
        tvLokasiNote.setText( (locNote != null && !locNote.isEmpty()) ? locNote : "Tidak ada catatan lokasi" );

        String note = presensi.getNote();
        tvNote.setText( (note != null && !note.isEmpty()) ? note : "-" );

        // Load Foto dengan Glide
        String photoUrl = presensi.getSelfiePhoto();
        if (photoUrl != null && !photoUrl.isEmpty()) {
            Glide.with(this)
                    .load(photoUrl)
                    .placeholder(android.R.drawable.ic_menu_camera)
                    .error(android.R.drawable.stat_notify_error)
                    .into(ivFoto);
        } else {
            ivFoto.setImageResource(android.R.drawable.ic_menu_camera);
        }

        btnClose.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }

    private void updateSummaryUI(HarianPresensiReportModel report) {
        if (isAdded()) {
            tvReportDate.setText(getString(R.string.laporan_tanggal, report.getReportDate()));
            tvTotalPetugas.setText(getString(R.string.total_petugas, report.getTotalPetugas()));
            tvPetugasHadir.setText(getString(R.string.petugas_hadir, report.getPetugasHadir()));
            tvPetugasBelumHadir.setText(getString(R.string.petugas_belum_hadir, report.getPetugasBelumHadir()));
        }
    }
}
