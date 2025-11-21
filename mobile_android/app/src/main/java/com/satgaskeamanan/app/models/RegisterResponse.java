package com.satgaskeamanan.app.models;

import com.google.gson.annotations.SerializedName;

public class RegisterResponse {
    @SerializedName("message")
    private String message;

    @SerializedName("status")
    private boolean status;

    public String getMessage() {
        return message;
    }
}
