package com.example.padelandmore.network;

import java.util.Map;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.DELETE;
import retrofit2.http.PUT;
import retrofit2.http.Path;

public interface ApiService {

    @POST("/api/auth/register")
    Call<Map<String, Object>> register(@Body Map<String, Object> body);

    @POST("/api/auth/login")
    Call<Map<String, Object>> login(@Body Map<String, Object> body);

    @GET("/api/users/{uid}")
    Call<Map<String, Object>> getUser(@Path("uid") String uid);

    @GET("/api/users/{uid}/role")
    Call<Map<String, Object>> getUserRole(@Path("uid") String uid);

    @GET("/api/{collection}")
    Call<Map<String, Object>> getCollection(@Path("collection") String collection);

    @PUT("/api/{collection}/{id}")
    Call<Map<String, Object>> updateCollectionItem(@Path("collection") String collection, @Path("id") String id, @Body Map<String, Object> body);

    @DELETE("/api/{collection}/{id}")
    Call<Map<String, Object>> deleteCollectionItem(@Path("collection") String collection, @Path("id") String id);

    @POST("/api/{collection}")
    Call<Map<String, Object>> postToCollection(@Path("collection") String collection, @Body Map<String, Object> body);

}

