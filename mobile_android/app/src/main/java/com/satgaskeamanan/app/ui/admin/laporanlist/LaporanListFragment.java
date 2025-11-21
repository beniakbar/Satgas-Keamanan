package com.satgaskeamanan.app.ui.admin.laporanlist;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.satgaskeamanan.app.R;
import com.satgaskeamanan.app.api.APIClient;
import com.satgaskeamanan.app.api.APIService;

import com.satgaskeamanan.app.models.AdminLaporanModel;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

// Fragment harus mengimplementasikan Listener yang kita buat
public class LaporanListFragment extends Fragment implements OnStatusChangeListener {

    private RecyclerView recyclerView;
    private AdminLaporanAdapter adapter;
    private APIService apiService;
    private List<AdminLaporanModel> laporanList; // Simpan daftar laporan

    public LaporanListFragment() {
        super(R.layout.fragment_laporan_list); // Asumsi layout
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        apiService = APIClient.getAPIService(requireContext());

        recyclerView = view.findViewById(R.id.rv_laporan_admin);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        fetchLaporanList();
    }

    private void fetchLaporanList() {
        apiService.getDaftarLaporan().enqueue(new Callback<List<AdminLaporanModel>>() {
            @Override
            public void onResponse(@NonNull Call<List<AdminLaporanModel>> call, @NonNull Response<List<AdminLaporanModel>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    laporanList = response.body();
                    // Kirim 'this' sebagai listener ke adapter
                    adapter = new AdminLaporanAdapter(laporanList, LaporanListFragment.this);
                    recyclerView.setAdapter(adapter);
                } else {
                    Toast.makeText(requireContext(), "Gagal memuat daftar laporan.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<AdminLaporanModel>> call, @NonNull Throwable t) {
                Toast.makeText(requireContext(), "Error Jaringan: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    // --- Implementasi Listener Status Change ---
    @Override
    public void onStatusUpdateClicked(int laporanId, String newStatus) {
        // Buat body request: {"status": "closed"}
        Map<String, String> statusUpdate = new HashMap<>();
        statusUpdate.put("status", newStatus);

        apiService.updateLaporanStatus(laporanId, statusUpdate).enqueue(new Callback<AdminLaporanModel>() {
            @Override
            public void onResponse(@NonNull Call<AdminLaporanModel> call, @NonNull Response<AdminLaporanModel> response) {
                if (response.isSuccessful() && response.body() != null) {
                    // Update model lokal dan RecyclerView
                    adapter.updateLaporan(response.body());
                    Toast.makeText(requireContext(), "Status Laporan ID " + laporanId + " berhasil diupdate menjadi " + newStatus, Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(requireContext(), "Gagal update status: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<AdminLaporanModel> call, @NonNull Throwable t) {
                Toast.makeText(requireContext(), "Error jaringan saat update status.", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
