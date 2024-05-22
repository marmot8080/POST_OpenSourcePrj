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
            @Field("mode") String mode,
            @Field("mac") String mac,
            @Field("receiver") String receiver,
            @Field("time") String time,
            @Field("otp") String otp,
            @Field("key") String key,
            @Field("data") String data
    );
}
