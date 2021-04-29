package com.example.ermac.checkobjects;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;

import static com.example.ermac.checkobjects.CreateReportActivity.directory;
import static com.example.ermac.checkobjects.ViewUtil.appContext;
import static com.example.ermac.checkobjects.ViewUtil.toastLong;

public class ObjsSenderService extends Service {

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        //throw new UnsupportedOperationException("Not yet implemented");

        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        SendingAsyncTask task = new SendingAsyncTask(this, directory);
        task.execute();


        //toast(appContext, "Service finished");
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    private static class SendingAsyncTask extends AsyncTask<Void, Integer, Integer> {
        private static final String TAG = "SendingAsyncTask";

        private Context context;
        private File directory;
        private DBHelper dbHelper;

        public SendingAsyncTask(Context context, File directory) {
            this.context = context;
            this.directory = directory;
            dbHelper = new DBHelper(context);
        }

        @Override
        protected void onPreExecute() {
            //Toast.makeText(context, "Отправка...", Toast.LENGTH_SHORT).show();
        }

        @Override
        protected Integer doInBackground(Void... voids) {
            Log.e("NAGGGGGGGGGGGGGGGGGGGGG", "Service started");

            if (dbHelper.isEmpty()) return 0;
            //else publishProgress(1);

            publishProgress(1, dbHelper.size());


            int id;
            while ((id = dbHelper.getLastId()) != -1){
                //Log.e("NAGGGGGGGGGGGGGGGGGGGGG", "inside1");

                CreateReportActivity.ObjInfo objInfo = dbHelper.get(id);
                if (objInfo==null) {
                    Log.e("NAGGGGGGGGGGGGGGGGGGGGG", "obj with id = "+ id + " invalid and will be removed");
                    dbHelper.remove(id);
                    continue;
                }

                if (send(objInfo, directory)) {
                    Log.e("NAGGGGGGGGGGGGGGGGGGGGG", "insideIF  "+ dbHelper.size());
                    Log.e("NAGGGGGGGGGGGGGGGGGGGGG", "obj with id = "+ id + " successfully sent");
                    //Log.e("NAGGGGGGGGGGGGGGGGGGGGG", "insideIF  ");
                    dbHelper.remove(id);
                    Log.e("NAGGGGGGGGGGGGGGGGGGGGG", "insideIF  "+ dbHelper.size());
                } else {
                    try {
                        Thread.sleep(5000);
                        if (appContext==null) return 1;
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                //Log.e("NAGGGGGGGGGGGGGGGGGGGGG", "inside2");
            }



            //Log.e("NAGGGGGGGGGGGGGGGGGGGGG", "Service preFinished");
            return -1;
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            switch (values[0]){
                //case 0: Toast.makeText(context, "Попытка отправки из кэша...", Toast.LENGTH_LONG).show(); break;
                case 1: toastLong(context, "Отправка "+values[1]+" записей");
            }
        }

        @Override
        protected void onPostExecute(Integer success) {
            switch (success){
                case -1: Toast.makeText(context, "Отправлено успешно", Toast.LENGTH_LONG).show(); break;
                //case 0: Toast.makeText(context, "Нет записей на отправку", Toast.LENGTH_LONG).show(); break;
                case 1: Toast.makeText(context,"Ошибка отправки, повторная попытка будет при перезапуске приложения", Toast.LENGTH_LONG).show(); break;
            }

            Log.e("NAGGGGGGGGGGGGGGGGGGGGG", "Service finished");

        }
    }

    private static boolean send(CreateReportActivity.ObjInfo objInfo, File directory){
        try {
            Retrofit retrofit = RetrofitApi.getClient();
            ServiceApi serviceApi = retrofit.create(ServiceApi.class);

            //CreateReportActivity.ObjInfo objInfo = objInfos[0];

            //////
                /*DBCache dbCache = DBCache.getInstance(context.getApplicationContext());
                dbCache.open();
                dbCache.addObjInfo(objInfo);
                dbCache.close();*/
            //////

            File picture = new File(directory + "/" + objInfo.getFileUri().getLastPathSegment());
            RequestBody body = RequestBody.create(okhttp3.MediaType.parse("multipart/form-data"), picture);
            MultipartBody.Part image = MultipartBody.Part.createFormData("Picture", picture.getName(), body);

            Call<ResponseBody> call = serviceApi.uploadDocs(
                    Utils.toRequestBody(objInfo.getObj()),
                    Utils.toRequestBody(objInfo.getPart()),
                    Utils.toRequestBody(objInfo.getDescription()),
                    image
            );

            Response<ResponseBody> response = call.execute();
            if (!response.isSuccessful()) {
                Log.e("NAGGGGGGGGGGGGGGGGGGGGG", "response NOT successful");
                Log.e("NAGGGGGGGGGGGGGGGGGGGGG", ""+response.message());

                return false;
            }
            if (response.code() != 200) {
                Log.e("NAGGGGGGGGGGGGGGGGGGGGG", "response.code() != 200");
                return false;
            }
            if (response.errorBody() != null)
            {
                Log.e("NAGGGGGGGGGGGGGGGGGGGGG", "response.errorBody() != null");
                return false;
            }


        }  catch (IOException e) {
            e.printStackTrace();
            return false;
        }catch (Exception e){
            e.printStackTrace();
            //toast(context, "Сделайте фото");
            return false;
        }

        return true;
    }
}
