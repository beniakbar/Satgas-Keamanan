package com.satgaskeamanan.app.models;

import com.google.gson.annotations.SerializedName;

import java.util.List;

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

    @SerializedName("location_note")
    private String locationNote;

    @SerializedName("note")
    private String note;

    @SerializedName("status")
    private String status; // 'open', 'in_progress', 'closed'

    @SerializedName("photo")
    private String photoUrl; // URL ke foto dari Django

    // Constructor, Getters, dan Setters...
    // (Tambahkan sesuai kebutuhan Anda)
    public int getId() { return id; }
    public String getPetugasName() { return petugasName; }
    public String getStatus() { return status; }
    public String getNote() { return note; }
}