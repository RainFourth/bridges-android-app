package com.example.ermac.checkobjects;

import android.content.Context;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Cache;
import okhttp3.CacheControl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import static com.example.ermac.checkobjects.RetrofitApi.BASE_URL_2;

public class ObjList {
    private static List<Obj> objs;
    private static List<Integer> selection;

    private static List<Obj> objsTmp;

    static {
        objs = new ArrayList<>();
        selection = new ArrayList<>();
    }


    public static void Init(Context context, boolean useCache){

        objsTmp = new ArrayList<>();

            File cacheFile = new File(context.getCacheDir(), "JsonObjsCache");
            cacheFile.mkdirs();

            long cacheSz = 5 * 1024 * 1024; //5 MB
            Cache cache = new Cache(cacheFile, cacheSz);

            OkHttpClient okHttpClient = new OkHttpClient()
                    .newBuilder()
                    .cache(cache)
                    .addInterceptor(chain -> {
                                Request request = chain.request();
                                CacheControl cacheControl = useCache ?
                                        new CacheControl.Builder().onlyIfCached().build() : new CacheControl.Builder().noCache().build();
                                    request = request.newBuilder()
                                            .cacheControl(cacheControl)
                                            .build();
                                return chain.proceed(request);
                            }
                    )
                    .build();

            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL_2)
                    .addConverterFactory(GsonConverterFactory.create())
                    .client(okHttpClient)
                    .build();

            ServiceApi serviceApi = retrofit.create(ServiceApi.class);
            Call<ServerObj[]> callServerObjs = serviceApi.getObjects();

            callServerObjs.enqueue(new Callback<ServerObj[]>() {
                @Override
                public void onResponse(Call<ServerObj[]> call, Response<ServerObj[]> response) {
                    if (response.isSuccessful()){

                        ServerObj[] serverObjs = response.body();
                        objsTmp = ObjServerToObjConversion.parse(serverObjs);

                        if (objsTmp!=null && !objsTmp.isEmpty()) {
                            objs = objsTmp;
                            selection = new ArrayList<>();
                        }

                        ObjectSelectionActivity.adapter.notifyDataSetChanged();
                    } else {

                    }
                }

                @Override
                public void onFailure(Call<ServerObj[]> call, Throwable t) {
                    if (!useCache){
                        ((ObjectSelectionActivity)context).toast("Не удалось получить данные с сервера, используются сохранённые");
                        Init(context, true);
                    }
                }
            });



    }

    public static void removeLast(){
        if (selection.size()>0) selection.remove(selection.size()-1);
    }

    public static boolean isNothingSelected(){
        return selection.size()==0;
    }

    public static List<String> getLastObjsNamesList(){
        List<Obj> list = objs;
        for (int i = 0; i < selection.size(); i++) {
            list = list.get(selection.get(i)).getParts();
        }
        List<String> names = new ArrayList<>();
        for (Obj o : list) names.add(o.getName());
        return names;
    }

    public static Obj getLastObject(){
        List<Obj> list = objs;
        if (isNothingSelected()) return null;
        Obj last = null;
        for (int i = 0; i < selection.size(); i++) {
            last = list.get(selection.get(i));
            list = last.getParts();
        }
        return last;
    }

    public static void next(int number){
        selection.add(number);
    }

    public static String getAllNames() {
        StringBuilder info = new StringBuilder();
        List<Obj> list = objs;
        for (int i = 0; i < selection.size(); i++) {
            info.append(list.get(selection.get(i)).getName());
            if (i!= selection.size()-1) info.append(" ->\n");
            list = list.get(selection.get(i)).getParts();
        }
        return info.toString();
    }

    public static String getFirstObjName() {
        return objs.get(selection.get(0)).getName();
    }

    public static String getPartsNames() {
        StringBuilder info = new StringBuilder();
        List<Obj> list = objs.get(selection.get(0)).getParts();
        for (int i = 1; i < selection.size(); i++) {
            info.append(list.get(selection.get(i)).getName());
            if (i!= selection.size()-1) info.append(" ->\n");
            list = list.get(selection.get(i)).getParts();
        }
        return info.toString();
    }
}
