package com.example.tmanager.network;

import java.util.Map;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface ApiService {

    @POST("/api/auth/register")
    Call<Map<String, Object>> register(@Body Map<String, Object> body);

    @POST("/api/auth/login")
    Call<Map<String, Object>> login(@Body Map<String, Object> body);

    @POST("/api/{collection}")
    Call<Map<String, Object>> postToCollection(@Path("collection") String collection, @Body Map<String, Object> body);

}

