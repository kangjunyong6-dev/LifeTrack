package com.example.lifetrack.api;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ApiClient {

    // TODO: Replace with YOUR Supabase URL
    private static final String BASE_URL = "https://annfohikwzjftxpxxedc.supabase.co";

    // TODO: Replace with YOUR Supabase anon key
    private static final String SUPABASE_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImFubmZvaGlrd3pqZnR4cHh4ZWRjIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NzY0MDM5MzQsImV4cCI6MjA5MTk3OTkzNH0._r9kaysnx1Sw6p_OdBH6ADr2CnYvT9PMWts-Bn098p0";

    private static Retrofit retrofit = null;

    public static Retrofit getClient() {
        if (retrofit == null) {
            // Add headers required by Supabase [citation:6]
            OkHttpClient client = new OkHttpClient.Builder()
                    .addInterceptor(chain -> {
                        Request request = chain.request().newBuilder()
                                .addHeader("apikey", SUPABASE_KEY)
                                .addHeader("Authorization", "Bearer " + SUPABASE_KEY)
                                .addHeader("Content-Type", "application/json")
                                .build();
                        return chain.proceed(request);
                    })
                    .build();

            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .client(client)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit;
    }
}