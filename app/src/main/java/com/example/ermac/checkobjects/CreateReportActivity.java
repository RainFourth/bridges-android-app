package com.example.ermac.checkobjects;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.ermac.checkobjects.confirmation.dialog.ConfirmationDialog;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;

import static com.example.ermac.checkobjects.ViewUtil.appContext;
import static com.example.ermac.checkobjects.ViewUtil.getTxt;
import static com.example.ermac.checkobjects.ViewUtil.showBackBtn;

public class CreateReportActivity extends AppCompatActivity implements Button.OnClickListener {

    private final int TYPE_PHOTO = 1;
    private final int REQUEST_CODE_PHOTO = 1;

    private final static String ENTRIES_KEY = "entries key";
    private final static String COUNT_KEY = "count key";
    private int count = 0;
    private ArrayList<Uri> entries;
    private RecyclerAdapter adapter;

    public static File directory = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "ObjectsPhotos");;
    private Uri outputFileUri;

    private DBHelper dbHelper;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main3);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        showBackBtn(this, true);
        toolbar.setNavigationOnClickListener(view -> onBackPressed());
        ((TextView) findViewById(R.id.info)).setText(ObjList.getAllNames());
        findViewById(R.id.btn_photo).setOnClickListener(this);
        findViewById(R.id.btn_send).setOnClickListener(this);

        if (savedInstanceState != null) {
            entries = savedInstanceState.getSerializable(ENTRIES_KEY) != null ?
                    (ArrayList<Uri>) savedInstanceState.getSerializable(ENTRIES_KEY) : new ArrayList<>();
            count = savedInstanceState != null ? savedInstanceState.getInt(COUNT_KEY) : 0;
        } else {
            entries = new ArrayList<>();
        }


        RecyclerView recyclerView = findViewById(R.id.recycler_view);//находим RecyclerView
        adapter = new RecyclerAdapter(entries);// создаем адаптер
        recyclerView.setAdapter(adapter);// устанавливаем для списка адаптер


        ((TextView) findViewById(R.id.path_info)).setText("Фото сохраняются в папку\n"
                + directory.toString()
                + "\n\nФайлы фотографий быстро удаляются из меню по трём точкам на первом экране приложения."
        );

        dbHelper = new DBHelper(this);
        appContext.startService(new Intent(appContext, ObjsSenderService.class));
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putSerializable(ENTRIES_KEY, entries);
        outState.putInt(COUNT_KEY, count);
        super.onSaveInstanceState(outState);
    }


    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        ObjList.removeLast();
        super.onBackPressed();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_photo:
                if (entries.size() != 0) {
                    Toast.makeText(this, "Удалите фото прежде чем сделать новое", Toast.LENGTH_LONG).show();
                    return;
                }
                createDirectory();
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                outputFileUri = generateFileUri(TYPE_PHOTO);
                intent.putExtra(MediaStore.EXTRA_OUTPUT, outputFileUri);
                startActivityForResult(intent, REQUEST_CODE_PHOTO);
                break;
            case R.id.btn_send:
                showSendAlertDialog();
                break;
        }
    }

    private void sendObjInfo() {
        ObjInfo info = new ObjInfo(ObjList.getFirstObjName(), ObjList.getPartsNames(), getTxt(this, R.id.descr),
                entries.size() > 0 ? entries.get(0) : null);

        dbHelper.addObjInfo(info);
        appContext.startService(new Intent(appContext, ObjsSenderService.class));
        //new SendingAsyncTask(this, directory).execute(info);
    }

    private static class SendingAsyncTask extends AsyncTask<ObjInfo, Void, Boolean> {
        private static final String TAG = "SendingAsyncTask";
        private Context context;
        private File directory;

        public SendingAsyncTask(Context context, File directory) {
            this.context = context;
            this.directory = directory;
        }

        @Override
        protected void onPreExecute() {
            //Toast.makeText(context, "Отправка...", Toast.LENGTH_SHORT).show();
        }

        @Override
        protected Boolean doInBackground(ObjInfo... objInfos) {
            try {
                Retrofit retrofit = RetrofitApi.getClient();
                ServiceApi serviceApi = retrofit.create(ServiceApi.class);

                ObjInfo objInfo = objInfos[0];

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
                if (!response.isSuccessful()) return false;
                if (response.code() != 200) {

                    return false;
                }
                if (response.errorBody() != null)
                {

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

        @Override
        protected void onProgressUpdate(Void... values) {

        }

        @Override
        protected void onPostExecute(Boolean success) {
            Toast.makeText(context, success ? "Отправлено успешно" : "Ошибка отправки", Toast.LENGTH_LONG).show();

        }
    }


    public static class ObjInfo {
        private String obj;
        private String part;
        private String description;
        private Uri fileUri;

        public ObjInfo(String obj, String part, String description, Uri fileUri) {
            this.obj = obj;
            this.part = part;
            this.description = description;
            this.fileUri = fileUri;
        }

        public String getObj() {
            return obj;
        }

        public String getPart() {
            return part;
        }

        public String getDescription() {
            return description;
        }

        public Uri getFileUri() {
            return fileUri;
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);

        if (requestCode == REQUEST_CODE_PHOTO) {
            if (resultCode == RESULT_OK) {
                entries.add(outputFileUri);
                adapter.notifyDataSetChanged();
            } else if (resultCode == RESULT_CANCELED) {
                Log.d("TAG", "Canceled");
            }

        }

    }


    private Uri generateFileUri(int type) {
        File file = null;
        switch (type) {
            case TYPE_PHOTO:
                file = new File(String.format("%s/photo_%s_%s.jpg", directory.getPath(), ++count,
                        new SimpleDateFormat("yyyy.MM.dd_HH:mm:ss", Locale.ENGLISH).format(new Date())));
                break;
        }
        Log.d("TAG", "fileName = " + file);
        if (Build.VERSION.SDK_INT >= 23)
            return FileProvider.getUriForFile(this, getApplicationContext().getPackageName() + ".checkobjects.fileprovider", file);
        else return Uri.fromFile(file);
    }

    private void createDirectory() {
        directory.mkdirs();
    }


    private class RecyclerAdapter extends RecyclerView.Adapter<RecyclerAdapter.ViewHolder> {
        private LayoutInflater inflater;
        private ArrayList<Uri> entries;

        RecyclerAdapter(ArrayList<Uri> entries) {
            this.entries = entries;
            this.inflater = LayoutInflater.from(CreateReportActivity.this);
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            final ImageView img;
            final TextView txt;

            ViewHolder(View view) {
                super(view);
                txt = view.findViewById(R.id.row_txt);
                img = view.findViewById(R.id.row_img);

                img.getLayoutParams().height = img.getLayoutParams().width = getDisplayDimensPx()[0];

            }
        }

        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = inflater.inflate(R.layout.rowlayout, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, final int position) {
            final int imgId = holder.img.getId();
            final int txtId = holder.txt.getId();
            View.OnLongClickListener onLongClickListener = v -> {
                if (v.getId() == imgId || v.getId() == txtId) {
                    showDeletePhotoAlertDialog(holder.getAdapterPosition(), holder.txt.getText().toString());
                    return true;
                }
                return false;
            };
            holder.img.setOnLongClickListener(onLongClickListener);
            holder.txt.setOnLongClickListener(onLongClickListener);

            holder.img.setImageURI(entries.get(position));
            holder.txt.setText(entries.get(position).getLastPathSegment());
        }

        @Override
        public int getItemCount() {
            return entries.size();
        }

    }


    int[] getDisplayDimensPx() {       //получить ширину и высоту дисплея в пикселях
        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        return new int[]{metrics.widthPixels, metrics.heightPixels};
    }

    private void showDeletePhotoAlertDialog(int idx, String name) {
        ConfirmationDialog.show(this, "Удалить " + name + "?", () -> {
            entries.remove(idx);
            adapter.notifyDataSetChanged();
        });
    }

    private void showSendAlertDialog() {
        ConfirmationDialog.show(this, "Отправить?", this::sendObjInfo);
    }

}