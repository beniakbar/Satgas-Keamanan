package com.satgaskeamanan.app.ui.admin.laporanharian;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.satgaskeamanan.app.R;
import com.satgaskeamanan.app.models.AdminPresensiModel;
import com.satgaskeamanan.app.models.PetugasStatusPresensiModel;

import java.util.List;

public class HarianPresensiAdapter extends RecyclerView.Adapter<HarianPresensiAdapter.ViewHolder> {

    private final List<PetugasStatusPresensiModel> petugasList;
    private final Context context;
    private final OnItemClickListener listener;

    // Interface untuk menangani klik item
    public interface OnItemClickListener {
        void onItemClick(PetugasStatusPresensiModel petugas);
    }

    // Konstruktor sekarang menerima Listener
    public HarianPresensiAdapter(Context context, List<PetugasStatusPresensiModel> petugasList, OnItemClickListener listener) {
        this.context = context;
        this.petugasList = petugasList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_harian_presensi, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        PetugasStatusPresensiModel petugas = petugasList.get(position);

        holder.tvNama.setText(petugas.getFullName());

        if (petugas.isHasPresensiToday()) {
            // Status Hadir
            holder.tvStatus.setText(R.string.status_hadir);
            holder.tvStatus.setTextColor(Color.parseColor("#4CAF50")); // Hijau

            AdminPresensiModel lastPresensi = petugas.getLastPresensi();
            if (lastPresensi != null) {
                // Tampilkan waktu presensi terakhir hari ini
                holder.tvWaktu.setText(context.getString(R.string.presensi_terakhir, lastPresensi.getTimestamp()));
                
                // Enable Click Listener hanya jika data presensi ada
                holder.itemView.setOnClickListener(v -> listener.onItemClick(petugas));
                holder.itemView.setClickable(true);
                holder.itemView.setFocusable(true);
            } else {
                holder.tvWaktu.setText(R.string.presensi_data_tidak_tersedia);
                holder.itemView.setOnClickListener(null);
                holder.itemView.setClickable(false);
            }

        } else {
            // Status Tidak Hadir (Absen)
            holder.tvStatus.setText(R.string.status_absen);
            holder.tvStatus.setTextColor(Color.RED);

            // Tampilkan informasi login terakhir atau kosongkan
            holder.tvWaktu.setText(R.string.belum_presensi_hari_ini);
            
            holder.itemView.setOnClickListener(null);
            holder.itemView.setClickable(false);
        }
    }

    @Override
    public int getItemCount() {
        return petugasList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvNama, tvStatus, tvWaktu;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvNama = itemView.findViewById(R.id.tv_petugas_nama);
            tvStatus = itemView.findViewById(R.id.tv_status_hadir);
            tvWaktu = itemView.findViewById(R.id.tv_presensi_waktu);
        }
    }
}
