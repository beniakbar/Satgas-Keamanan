package com.satgaskeamanan.app.models;

import com.google.gson.annotations.SerializedName;

public class UserModel {

    @SerializedName("id")
    private int id;

    @SerializedName("email")
    private String email;

    // Ambil status peran dari Django API
    @SerializedName("is_admin")
    private boolean isAdmin;

    @SerializedName("is_petugas")
    private boolean isPetugas;

    // =========================
    // Getter
    // =========================

    public int getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public boolean isAdmin() {
        return isAdmin;
    }

    public boolean isPetugas() {
        return isPetugas;
    }
}
