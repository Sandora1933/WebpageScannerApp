package com.example.webpagescannerapp;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Url;

public interface RequestAPI {
    @GET
    Call<Integer> getMethod(@Url String url);
}
