package com.satgaskeamanan.app.models;

import com.google.gson.annotations.SerializedName;

public class AdminPresensiModel {

    @SerializedName("id")
    private int id;

    @SerializedName("timestamp")
    private String timestamp;

    @SerializedName("latitude")
    private double latitude;

    @SerializedName("longitude")
    private double longitude;

    @SerializedName("location_note")
    private String locationNote;

    @SerializedName("note")
    private String note;

    @SerializedName("selfie_photo")
    private String selfiePhoto;
    
    @SerializedName("petugas_name")
    private String petugasName;

    @SerializedName("petugas_email")
    private String petugasEmail;

    // Default Constructor
    public AdminPresensiModel() {
    }

    // Getters
    public int getId() { return id; }
    public String getTimestamp() { return timestamp; }
    public double getLatitude() { return latitude; }
    public double getLongitude() { return longitude; }
    public String getLocationNote() { return locationNote; }
    public String getNote() { return note; }
    public String getSelfiePhoto() { return selfiePhoto; }
    public String getPetugasName() { return petugasName; }
    public String getPetugasEmail() { return petugasEmail; }

    // Setters
    public void setId(int id) { this.id = id; }
    public void setTimestamp(String timestamp) { this.timestamp = timestamp; }
    public void setLatitude(double latitude) { this.latitude = latitude; }
    public void setLongitude(double longitude) { this.longitude = longitude; }
    public void setLocationNote(String locationNote) { this.locationNote = locationNote; }
    public void setNote(String note) { this.note = note; }
    public void setSelfiePhoto(String selfiePhoto) { this.selfiePhoto = selfiePhoto; }
    public void setPetugasName(String petugasName) { this.petugasName = petugasName; }
    public void setPetugasEmail(String petugasEmail) { this.petugasEmail = petugasEmail; }
}
