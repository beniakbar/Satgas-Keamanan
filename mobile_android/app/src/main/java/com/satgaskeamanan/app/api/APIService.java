package com.satgaskeamanan.app.api;

import com.satgaskeamanan.app.models.TokenResponse;
import com.satgaskeamanan.app.models.PetugasModel;
import com.satgaskeamanan.app.models.AdminLaporanModel;
import com.satgaskeamanan.app.models.HarianPresensiReportModel;
import com.satgaskeamanan.app.models.UserModel;
import com.satgaskeamanan.app.models.LoginRequest;
import com.satgaskeamanan.app.models.RegisterRequest;
import com.satgaskeamanan.app.models.RegisterResponse;

import java.util.List;
import java.util.Map;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.Multipart;
import retrofit2.http.PATCH;
import retrofit2.http.Part;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface APIService {

    // ---------------------------
    // AUTENTIKASI
    // ---------------------------
    @POST("token/")
    Call<TokenResponse> login(@Body LoginRequest loginRequest);

    @FormUrlEncoded
    @POST("token/refresh/")
    Call<TokenResponse> refreshToken(
            @Field("refresh") String refreshToken
    );

    @Headers("No-Authentication: true")
    @POST("user/register/")
    Call<RegisterResponse> register(@Body RegisterRequest request);

    // ---------------------------
    // PROFIL PENGGUNA
    // ---------------------------
    @GET("user/profile/")
    Call<UserModel> getUserProfile();


    // ---------------------------
    // PRESENSI - PETUGAS
    // ---------------------------
    @Multipart
    @POST("presensi/")
    Call<Void> kirimPresensi(
            @Part("latitude") RequestBody latitude,
            @Part("longitude") RequestBody longitude,
            @Part("location_note") RequestBody locationNote,
            @Part("note") RequestBody note,
            @Part MultipartBody.Part selfiePhoto
    );


    // ---------------------------
    // LAPORAN - PETUGAS
    // ---------------------------
    @Multipart
    @POST("laporan/")
    Call<Void> kirimLaporan(
            @Part("latitude") RequestBody latitude,
            @Part("longitude") RequestBody longitude,
            @Part("location_note") RequestBody locationNote,
            @Part("note") RequestBody note,
            @Part MultipartBody.Part photo
    );

    // ---------------------------
    // ADMIN
    // ---------------------------
    @GET("admin/petugas/")
    Call<List<PetugasModel>> getDaftarPetugas();

    @GET("admin/laporan/")
    Call<List<AdminLaporanModel>> getDaftarLaporan();

    @PATCH("admin/laporan/{id}/")
    Call<AdminLaporanModel> updateLaporanStatus(
            @Path("id") int laporanId,
            @Body Map<String, String> statusUpdate
    );

    // Endpoint rekap presensi harian (sesuai konfirmasi pengguna)
    @GET("admin/laporan/harian/")
    Call<HarianPresensiReportModel> getHarianPresensiReport();
}
