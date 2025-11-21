package com.satgaskeamanan.app.models;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class HarianPresensiReportModel {

    @SerializedName("report_date")
    private String reportDate;

    @SerializedName("total_petugas")
    private int totalPetugas;

    @SerializedName("petugas_hadir")
    private int petugasHadir;

    @SerializedName("petugas_belum_hadir")
    private int petugasBelumHadir;

    @SerializedName("data")
    private List<PetugasStatusPresensiModel> data;

    // --- Getters ---
    public String getReportDate() { return reportDate; }
    public int getTotalPetugas() { return totalPetugas; }
    public int getPetugasHadir() { return petugasHadir; }
    public int getPetugasBelumHadir() { return petugasBelumHadir; }
    public List<PetugasStatusPresensiModel> getData() { return data; }
}