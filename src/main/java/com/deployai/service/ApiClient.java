package com.deployai.service;

import com.deployai.config.AppConfig;
import com.deployai.config.SessionManager;
import com.google.gson.Gson;
import okhttp3.*;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class ApiClient {

    private static ApiClient instance;
    private final OkHttpClient httpClient;
    private final Gson gson;
    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");

    private ApiClient() {
        this.httpClient = new OkHttpClient.Builder()
                .connectTimeout(15, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(15, TimeUnit.SECONDS)
                .build();
        this.gson = new Gson();
    }

    public static ApiClient getInstance() {
        if (instance == null) {
            instance = new ApiClient();
        }
        return instance;
    }

    public String post(String endpoint, Object body) throws IOException {
        String json = gson.toJson(body);
        RequestBody requestBody = RequestBody.create(json, JSON);

        Request request = new Request.Builder()
                .url(AppConfig.API_BASE_URL + endpoint)
                .post(requestBody)
                .build();

        try (Response response = httpClient.newCall(request).execute()) {
            String responseBody = response.body() != null ? response.body().string() : "";
            if (!response.isSuccessful()) {
                throw new IOException("Error " + response.code() + ": " + responseBody);
            }
            return responseBody;
        }
    }

    public String postAuth(String endpoint, Object body) throws IOException {
        String json = gson.toJson(body);
        RequestBody requestBody = RequestBody.create(json, JSON);

        Request request = new Request.Builder()
                .url(AppConfig.API_BASE_URL + endpoint)
                .post(requestBody)
                .header("Authorization", "Bearer " + SessionManager.getInstance().getToken())
                .build();

        try (Response response = httpClient.newCall(request).execute()) {
            String responseBody = response.body() != null ? response.body().string() : "";
            if (!response.isSuccessful()) {
                throw new IOException("Error " + response.code() + ": " + responseBody);
            }
            return responseBody;
        }
    }

    public String getAuth(String endpoint) throws IOException {
        Request request = new Request.Builder()
                .url(AppConfig.API_BASE_URL + endpoint)
                .get()
                .header("Authorization", "Bearer " + SessionManager.getInstance().getToken())
                .build();

        try (Response response = httpClient.newCall(request).execute()) {
            String responseBody = response.body() != null ? response.body().string() : "";
            if (!response.isSuccessful()) {
                throw new IOException("Error " + response.code() + ": " + responseBody);
            }
            return responseBody;
        }
    }

    public Gson getGson() { return gson; }
}