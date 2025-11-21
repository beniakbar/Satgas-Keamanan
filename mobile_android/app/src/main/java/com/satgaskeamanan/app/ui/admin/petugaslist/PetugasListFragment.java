package com.satgaskeamanan.app.ui.admin.petugaslist;

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
import com.satgaskeamanan.app.models.PetugasModel;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PetugasListFragment extends Fragment {

    private RecyclerView recyclerView;
    private AdminPetugasAdapter adapter;
    private APIService apiService;

    public PetugasListFragment() {
        super(R.layout.fragment_petugas_list);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        apiService = APIClient.getAPIService(requireContext());

        recyclerView = view.findViewById(R.id.rv_petugas_admin);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        fetchPetugasList();
    }

    private void fetchPetugasList() {
        apiService.getDaftarPetugas().enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<List<PetugasModel>> call, @NonNull Response<List<PetugasModel>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<PetugasModel> petugasList = response.body();
                    adapter = new AdminPetugasAdapter(petugasList);
                    recyclerView.setAdapter(adapter);
                } else if (response.code() == 403) {
                    Toast.makeText(requireContext(), "Akses Ditolak. Anda bukan Admin.", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(requireContext(), "Gagal memuat data petugas: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<PetugasModel>> call, @NonNull Throwable t) {
                Toast.makeText(requireContext(), "Error Jaringan: Gagal terhubung ke server.", Toast.LENGTH_LONG).show();
            }
        });
    }
}
