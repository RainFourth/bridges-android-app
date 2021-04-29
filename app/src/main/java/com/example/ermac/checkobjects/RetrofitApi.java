package com.example.ermac.checkobjects;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitApi {

    private static final String BASE_URL = "http://137.135.170.56:9123/"; // your base URL

     static final String BASE_URL_2 = "https://pastebin.com/raw/";

    private static Retrofit retrofit = null;

    private static Retrofit retrofitForObtainingObjsList = null;


    public static Retrofit getClient() {
        if (retrofit == null) {
            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit;
    }
}
