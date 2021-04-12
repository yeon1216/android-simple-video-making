package com.example.changeapp;


import okhttp3.MultipartBody;
import retrofit2.Call;
import retrofit2.http.Header;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;

public interface NaverApiInterface {

    @Multipart
    @POST("/v1/vision/face")
    Call<NaverRepo> naverRepo2(@Header("X-Naver-Client-Id") String id
            , @Header("X-Naver-Client-Secret") String secret
            , @Part MultipartBody.Part file);
}

