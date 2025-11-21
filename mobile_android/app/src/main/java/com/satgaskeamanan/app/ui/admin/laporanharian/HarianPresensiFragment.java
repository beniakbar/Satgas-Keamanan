package com.satgaskeamanan.app.ui.admin.laporanharian;

import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.satgaskeamanan.app.R;
import com.satgaskeamanan.app.api.APIClient;
import com.satgaskeamanan.app.api.APIService;
import com.satgaskeamanan.app.models.HarianPresensiReportModel;
import com.satgaskeamanan.app.models.PetugasStatusPresensiModel;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HarianPresensiFragment extends Fragment {

    private TextView tvReportDate, tvTotalPetugas, tvPetugasHadir, tvPetugasBelumHadir;
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
        tvTotalPetugas = view.findViewById(R.id.tv_total_petugas);
        tvPetugasHadir = view.findViewById(R.id.tv_petugas_hadir);
        tvPetugasBelumHadir = view.findViewById(R.id.tv_petugas_belum_hadir);
        recyclerView = view.findViewById(R.id.rv_harian_presensi);
        progressBar = view.findViewById(R.id.progress_bar); // Pastikan ada ProgressBar di XML

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        apiService = APIClient.getAPIService(requireContext());

        fetchHarianPresensiReport();
    }

    private void fetchHarianPresensiReport() {
        progressBar.setVisibility(View.VISIBLE);

        apiService.getHarianPresensiReport().enqueue(new Callback<HarianPresensiReportModel>() {
            @Override
            public void onResponse(@NonNull Call<HarianPresensiReportModel> call, @NonNull Response<HarianPresensiReportModel> response) {
                progressBar.setVisibility(View.GONE);

                if (response.isSuccessful() && response.body() != null) {
                    HarianPresensiReportModel report = response.body();
                    updateSummaryUI(report);

                    List<PetugasStatusPresensiModel> dataList = report.getData();
                    adapter = new HarianPresensiAdapter(requireContext(), dataList);
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

    private void updateSummaryUI(HarianPresensiReportModel report) {
        tvReportDate.setText(getString(R.string.laporan_tanggal, report.getReportDate()));
        tvTotalPetugas.setText(getString(R.string.total_petugas, report.getTotalPetugas()));
        tvPetugasHadir.setText(getString(R.string.petugas_hadir, report.getPetugasHadir()));
        tvPetugasBelumHadir.setText(getString(R.string.petugas_belum_hadir, report.getPetugasBelumHadir()));
    }
}
