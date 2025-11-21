package models;

import com.google.gson.annotations.SerializedName;

public class TokenResponse {
    // Sesuaikan nama field dengan JSON response dari Django
    @SerializedName("refresh")
    private String refreshToken;

    @SerializedName("access")
    private String accessToken;

    public String getRefreshToken() {
        return refreshToken;
    }

    public String getAccessToken() {
        return accessToken;
    }
}