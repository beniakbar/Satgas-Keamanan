package com.satgaskeamanan.app.ui.admin.petugaslist;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.satgaskeamanan.app.R; 
import com.satgaskeamanan.app.models.PetugasModel; 

import java.util.List;

public class AdminPetugasAdapter extends RecyclerView.Adapter<AdminPetugasAdapter.PetugasViewHolder> {

    private final List<PetugasModel> petugasList;

    public AdminPetugasAdapter(List<PetugasModel> petugasList) {
        this.petugasList = petugasList;
    }

    @NonNull
    @Override
    public PetugasViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflate layout item_petugas.xml
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_petugas, parent, false);
        return new PetugasViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PetugasViewHolder holder, int position) {
        PetugasModel petugas = petugasList.get(position);

        // Mengisi data ke TextViews
        String fullName = petugas.getFirstName();
        holder.tvPetugasName.setText(fullName);
        holder.tvPetugasEmail.setText(petugas.getEmail());

        // Format tanggal last login (opsional: tambahkan library seperti Joda-Time atau gunakan SimpleDateFormat)
        String lastLogin = formatLastLogin(petugas.getLastLogin());
        holder.tvLastLogin.setText("Terakhir Login: " + lastLogin);
    }

    @Override
    public int getItemCount() {
        return petugasList.size();
    }

    // --- Inner Class ViewHolder ---
    public static class PetugasViewHolder extends RecyclerView.ViewHolder {
        TextView tvPetugasName, tvPetugasEmail, tvLastLogin;

        public PetugasViewHolder(@NonNull View itemView) {
            super(itemView);
            // Hubungkan variabel dengan ID dari item_petugas.xml
            tvPetugasName = itemView.findViewById(R.id.tvPetugasName);
            tvPetugasEmail = itemView.findViewById(R.id.tvPetugasEmail);
            tvLastLogin = itemView.findViewById(R.id.tvLastLogin);
        }
    }

    // --- Fungsi Helper (Dummy, Anda bisa implementasi formatting date yang lebih baik) ---
    private String formatLastLogin(String isoDateString) {
        if (isoDateString == null) return "Belum pernah login";
        // Di sini Anda bisa menambahkan logika parsing dan formatting date/time
        // Sementara kembalikan potongan string saja
        try {
            return isoDateString.substring(0, 10) + " " + isoDateString.substring(11, 16);
        } catch (Exception e) {
            return isoDateString;
        }
    }
}
