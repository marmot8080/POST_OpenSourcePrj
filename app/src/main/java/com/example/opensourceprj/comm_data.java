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
    @POST("dustsensor/sensing/")
    Call<String> post(
            @Field("sensor") String user,
            @Field("mac") String mac,
            @Field("receiver") String receiver,
            @Field("time") long time,
            @Field("otp") int otp,
            @Field("data") String data);

    @POST("dustsensor/commtest_json/")
    Call<postdata> post_json(
            @Body postdata pd
    );

    @GET("dustsensor/commtest_get/")
    Call<String> get(
            @Query("sensor") String user,
            @Query("mac") String mac,
            @Query("receiver") String receiver,
            @Query("time") long time,
            @Query("otp") int otp,
            @Query("data") String data);
}
