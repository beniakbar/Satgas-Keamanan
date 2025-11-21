package com.satgaskeamanan.app.models;

import com.google.gson.annotations.SerializedName;

public class TokenResponse {

    @SerializedName("access")
    private String accessToken;

    @SerializedName("refresh")
    private String refreshToken;

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }
}
