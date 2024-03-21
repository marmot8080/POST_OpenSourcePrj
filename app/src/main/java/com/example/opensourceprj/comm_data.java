package com.example.opensourceprj;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface comm_data {
    @FormUrlEncoded
    @POST("dustsensor/commtest/")
    Call<String> post(
            @Field("user") String user,
            @Field("data") String data);

    @POST("dustsensor/commtest_json/")
    Call<postdata> post_json(
            @Body postdata pd
    );

    @GET("dustsensor/commtest_get/")
    Call<String> get(
            @Query("user") String user,
            @Query("data") String data);
}
