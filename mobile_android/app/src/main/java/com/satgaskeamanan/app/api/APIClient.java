package com.satgaskeamanan.app.api;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.satgaskeamanan.app.LoginActivity;
import com.satgaskeamanan.app.MainActivity;
import com.satgaskeamanan.app.models.TokenResponse;

import okhttp3.Authenticator;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.Route;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class APIClient {


    // private static final String BASE_URL = "http://10.0.2.2:8000/api/";

    private static final String BASE_URL = "http://192.168.18.11:8000/api/";

    private static Retrofit retrofit = null;
    private static APIService apiService = null;
    private static final String TAG = "APIClient";

    // ============================
    //  Membuat Retrofit Client
    // ============================
    public static Retrofit getClient(Context context) {
        if (retrofit == null) {

            Context appContext = context.getApplicationContext();

            // --- 1. Http Logging Interceptor ---
            HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor(
                    message -> Log.d(TAG, message));
            loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);

            // --- 2. Interceptor Tambah Token ---
            Interceptor authInterceptor = chain -> {
                SharedPreferences settings =
                        appContext.getSharedPreferences(MainActivity.PREFS_NAME, Context.MODE_PRIVATE);

                String token = settings.getString("AccessToken", null);

                Request original = chain.request();
                Request.Builder builder = original.newBuilder();

                if (token != null) {
                    builder.header("Authorization", "Bearer " + token);
                }

                String tokenPreview = "None";
                if (token != null) {
                    tokenPreview = token.length() > 10 ? token.substring(0, 10) + "..." : token;
                }

                Log.d(TAG, "Requesting: " + original.url() +
                        " | Token Exists: " + (token != null) +
                        " | Token: " + tokenPreview);

                return chain.proceed(builder.build());
            };

            // --- 3. Authenticator untuk Refresh Token ---
            Authenticator tokenAuthenticator = new Authenticator() {
                @Nullable
                @Override
                public Request authenticate(@Nullable Route route, @NonNull Response response) {
                    try {
                        Log.d(TAG, "Authentication Triggered for: " + response.request().url());

                        if (responseCount(response) >= 2) {
                            Log.e(TAG, "Max refresh attempts reached. Forcing logout.");
                            return null;
                        }

                        SharedPreferences settings =
                                appContext.getSharedPreferences(MainActivity.PREFS_NAME, Context.MODE_PRIVATE);

                        String refreshToken = settings.getString("RefreshToken", null);

                        if (refreshToken == null) {
                            Log.e(TAG, "Refresh Token is null. Forcing logout.");
                            forceLogout(appContext);
                            return null;
                        }

                        Retrofit refreshRetrofit = new Retrofit.Builder()
                                .baseUrl(BASE_URL)
                                .addConverterFactory(GsonConverterFactory.create())
                                .build();

                        APIService refreshService = refreshRetrofit.create(APIService.class);

                        retrofit2.Response<TokenResponse> refreshResponse =
                                refreshService.refreshToken(refreshToken).execute();

                        if (refreshResponse.isSuccessful() && refreshResponse.body() != null) {

                            TokenResponse tokenResponse = refreshResponse.body();

                            String newAccess = tokenResponse.getAccessToken();
                            String newRefresh = tokenResponse.getRefreshToken();

                            SharedPreferences.Editor editor = settings.edit();
                            editor.putString("AccessToken", newAccess);
                            editor.putString("RefreshToken", newRefresh);
                            editor.apply();

                            return response.request().newBuilder()
                                    .header("Authorization", "Bearer " + newAccess)
                                    .build();
                        }

                        Log.e(TAG, "Refresh failed: " + refreshResponse.code());
                        forceLogout(appContext);
                        return null;

                    } catch (Exception e) {
                        Log.e(TAG, "Exception during token refresh: " + e.getMessage(), e);
                        forceLogout(appContext);
                        return null;
                    }
                }

            };

            // Build OkHttp
            OkHttpClient client = new OkHttpClient.Builder()
                    .addInterceptor(authInterceptor)
                    .addInterceptor(loggingInterceptor)
                    .authenticator(tokenAuthenticator)
                    .build();

            // Build Retrofit
            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .client(client)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }

        return retrofit;
    }

    // ============================
    //  APIService Getter
    // ============================
    public static APIService getAPIService(Context context) {
        if (apiService == null) {
            apiService = getClient(context).create(APIService.class);
        }
        return apiService;
    }

    // ============================
    //   Hitung Response Chain
    // ============================
    private static int responseCount(Response response) {
        int count = 1;
        while ((response = response.priorResponse()) != null) {
            count++;
        }
        return count;
    }

    // ============================
    //  Paksa Logout
    // ============================
    public static void forceLogout(Context context) {

        SharedPreferences settings =
                context.getSharedPreferences(MainActivity.PREFS_NAME, Context.MODE_PRIVATE);

        SharedPreferences.Editor editor = settings.edit();
        editor.remove("AccessToken");
        editor.remove("RefreshToken");
        editor.apply();

        Intent intent = new Intent(context, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        context.startActivity(intent);
    }
}
