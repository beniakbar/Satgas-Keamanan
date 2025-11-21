package com.satgaskeamanan.app.models;

import com.google.gson.annotations.SerializedName;

public class PetugasModel {

    @SerializedName("id")
    private int id;

    @SerializedName("email")
    private String email;

    @SerializedName("first_name")
    private String firstName;

    @SerializedName("phone_number")
    private String phoneNumber;

    @SerializedName("last_login")
    private String lastLogin; // Format ISO 8601

    // Constructor, Getters, dan Setters...
    // (Tambahkan sesuai kebutuhan Anda)
    public String getEmail() { return email; }
    public String getFirstName() { return firstName; }
    public String getLastLogin() { return lastLogin; }
}
