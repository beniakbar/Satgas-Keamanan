package com.satgaskeamanan.app.models;

import com.google.gson.annotations.SerializedName;

public class AdminPresensiModel {

    @SerializedName("id")
    private int id;

    @SerializedName("check_in")
    private String checkIn;

    @SerializedName("check_out")
    private String checkOut;

    @SerializedName("date")
    private String date;

    // Alias untuk getCheckIn() jika digunakan sebagai timestamp
    public String getTimestamp() {
        return checkIn;
    }

    public int getId() { return id; }
    public String getCheckIn() { return checkIn; }
    public String getCheckOut() { return checkOut; }
    public String getDate() { return date; }
}
