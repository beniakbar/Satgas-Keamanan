package com.satgaskeamanan.app.ui.admin.laporanlist;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.satgaskeamanan.app.R;
import com.satgaskeamanan.app.models.AdminLaporanModel;

import java.util.List;

public class AdminLaporanAdapter extends RecyclerView.Adapter<AdminLaporanAdapter.LaporanViewHolder> {

    private List<AdminLaporanModel> laporanList;
    private OnStatusChangeListener listener;

    public AdminLaporanAdapter(List<AdminLaporanModel> laporanList, OnStatusChangeListener listener) {
        this.laporanList = laporanList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public LaporanViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_laporan_admin, parent, false);
        return new LaporanViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull LaporanViewHolder holder, int position) {
        AdminLaporanModel laporan = laporanList.get(position);

        holder.tvPetugasName.setText(laporan.getPetugasName());
        holder.tvNote.setText(laporan.getNote());
        holder.tvStatus.setText(holder.itemView.getContext().getString(R.string.status_format, laporan.getStatus()));

        // Logika Tombol Aksi (Misal: Tombol untuk menutup laporan)
        if ("open".equals(laporan.getStatus()) || "in_progress".equals(laporan.getStatus())) {
            holder.btnAksi.setVisibility(View.VISIBLE);
            holder.btnAksi.setText(R.string.tutup_laporan);
            holder.btnAksi.setOnClickListener(v -> {
                // Panggil listener di Fragment untuk menjalankan PATCH request
                listener.onStatusUpdateClicked(laporan.getId(), "closed");
            });
        } else {
            holder.btnAksi.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return laporanList.size();
    }

    // Metode untuk memperbarui data setelah PATCH berhasil
    public void updateLaporan(AdminLaporanModel updatedLaporan) {
        for (int i = 0; i < laporanList.size(); i++) {
            if (laporanList.get(i).getId() == updatedLaporan.getId()) {
                laporanList.set(i, updatedLaporan);
                notifyItemChanged(i);
                break;
            }
        }
    }

    // ViewHolder (inner class)
    public static class LaporanViewHolder extends RecyclerView.ViewHolder {
        TextView tvPetugasName, tvNote, tvStatus;
        Button btnAksi; // Tombol untuk mengubah status

        public LaporanViewHolder(@NonNull View itemView) {
            super(itemView);
            tvPetugasName = itemView.findViewById(R.id.tv_petugas_name);
            tvNote = itemView.findViewById(R.id.tv_laporan_note);
            tvStatus = itemView.findViewById(R.id.tv_laporan_status);
            btnAksi = itemView.findViewById(R.id.btn_aksi_status);
        }
    }
}
