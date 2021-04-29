package com.example.ermac.checkobjects;

import java.io.File;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;

public final class Utils {
    public static RequestBody toRequestBody(String field) {
        return RequestBody.create(okhttp3.MultipartBody.FORM, field);
    }

    public static MultipartBody.Part toMultipartBodyPart(String fieldName, File file) {
        RequestBody requestFile =
                RequestBody.create(
                        MediaType.parse("image/*"),
                        file
                );
        return MultipartBody.Part.createFormData(fieldName, file.getName(), requestFile);
    }

    public static void addObjToSend(CreateReportActivity.ObjInfo objInfo){

    }
}
