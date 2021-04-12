package com.example.changeapp;


import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;

public interface UploadService {

    //프로필 이미지, 아이디랑 서버에 전송
    @Multipart
    @POST("/upload_file")
    Call<ResponseBody> uploadFile(
            @Part("id") RequestBody id,
            @Part MultipartBody.Part file);

//
//    //아이디, 파일번호(영상번호), 파일
    @Multipart
    @POST("/voiceupload")
    Call<ResponseBody> uploadVoice(
            @Part MultipartBody.Part file);
}
