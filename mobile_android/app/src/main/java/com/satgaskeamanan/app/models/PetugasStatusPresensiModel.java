package com.satgaskeamanan.app.models;

import com.google.gson.annotations.SerializedName;

public class PetugasStatusPresensiModel {

    @SerializedName("id")
    private int id;

    @SerializedName("full_name")
    private String fullName;

    @SerializedName("has_presensi_today")
    private boolean hasPresensiToday;

    @SerializedName("last_presensi")
    private AdminPresensiModel lastPresensi;

    // Default Constructor
    public PetugasStatusPresensiModel() {
    }

    // Constructor used potentially manually or by tests
    public PetugasStatusPresensiModel(String fullName, boolean hasPresensiToday, AdminPresensiModel lastPresensi) {
        this.fullName = fullName;
        this.hasPresensiToday = hasPresensiToday;
        this.lastPresensi = lastPresensi;
    }

    // --- Getters and Setters ---

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public boolean isHasPresensiToday() {
        return hasPresensiToday;
    }

    public void setHasPresensiToday(boolean hasPresensiToday) {
        this.hasPresensiToday = hasPresensiToday;
    }

    public AdminPresensiModel getLastPresensi() {
        return lastPresensi;
    }

    public void setLastPresensi(AdminPresensiModel lastPresensi) {
        this.lastPresensi = lastPresensi;
    }
}
