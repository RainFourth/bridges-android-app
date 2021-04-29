package com.example.ermac.checkobjects;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;

public interface ServiceApi {

    @Multipart
    @POST("home/createreport")
    Call<ResponseBody> uploadDocs(
            @Part("ObjectName") RequestBody objectName,
            @Part("PartName") RequestBody partName,
            @Part("Description") RequestBody description,
            @Part MultipartBody.Part picture
    );

    @GET("AbJrPFQy")
    Call<ServerObj[]> getObjects();
}

