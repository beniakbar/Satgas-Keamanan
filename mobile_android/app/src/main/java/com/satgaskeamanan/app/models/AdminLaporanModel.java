package com.satgaskeamanan.app.models;

import com.google.gson.annotations.SerializedName;

public class AdminLaporanModel {

    @SerializedName("id")
    private int id;

    // Data Petugas
    @SerializedName("petugas_name")
    private String petugasName;

    @SerializedName("petugas_email")
    private String petugasEmail;

    // Data Laporan
    @SerializedName("timestamp")
    private String timestamp;

    @SerializedName("latitude")
    private double latitude;

    @SerializedName("longitude")
    private double longitude;

    @SerializedName("location_note")
    private String locationNote;

    @SerializedName("note")
    private String note; // Judul / Keterangan Laporan

    @SerializedName("status")
    private String status; // 'open', 'in_progress', 'closed'

    @SerializedName("priority")
    private String priority;

    @SerializedName("photo")
    private String photoUrl; // URL ke foto dari Django

    // Getters
    public int getId() { return id; }
    public String getPetugasName() { return petugasName; }
    public String getPetugasEmail() { return petugasEmail; }
    public String getTimestamp() { return timestamp; }
    public double getLatitude() { return latitude; }
    public double getLongitude() { return longitude; }
    public String getLocationNote() { return locationNote; }
    public String getNote() { return note; }
    public String getStatus() { return status; }
    public String getPriority() { return priority; }
    public String getPhotoUrl() { return photoUrl; }

    // Setters (Opsional, tapi baik untuk testing/parsing manual)
    public void setStatus(String status) { this.status = status; }
}
