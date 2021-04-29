package com.example.ermac.checkobjects;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.example.ermac.checkobjects.confirmation.dialog.ConfirmationDialog;

import java.io.File;

import static com.example.ermac.checkobjects.ViewUtil.appContext;
import static com.example.ermac.checkobjects.ViewUtil.showBackBtn;
import static com.example.ermac.checkobjects.ViewUtil.toastLong;

public class ObjectSelectionActivity extends AppCompatActivity {


    static RecyclerAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        toolbar.setNavigationOnClickListener(view->onBackPressed());

        ViewUtil.appContext = this.getApplicationContext();

        updateObjectsList();

        if (ObjList.isNothingSelected()){
            setTitle(R.string.title_activity_main);
        } else {
            setTitle(R.string.title_activity_main2);
            showBackBtn(this, true);
        }


        RecyclerView recyclerView = findViewById(R.id.recycler_view_objs);//находим RecyclerView
        adapter = new RecyclerAdapter();// создаем адаптер
        recyclerView.setAdapter(adapter);// устанавливаем для списка адаптер

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
            if (Build.VERSION.SDK_INT >= 23)
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);


        appContext.startService(new Intent(appContext, ObjsSenderService.class));
    }


    @Override
    protected void onResume() {
        super.onResume();
        if (ObjList.isNothingSelected()){
            setTitle(R.string.title_activity_main);
            showBackBtn(this, false);
        } else {
            setTitle(R.string.title_activity_main2);
            showBackBtn(this, true);
        }
    }

    @Override
    public void onBackPressed() {
        if (!ObjList.isNothingSelected()){
            ObjList.removeLast();
            adapter.notifyDataSetChanged();
        }
        if (ObjList.isNothingSelected()) { //не объединять в if else !!!
            setTitle(R.string.title_activity_main);
            showBackBtn(this, false);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the run bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle run bar item clicks here. The run bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        switch (id){
            case R.id.menu_del_all_photos:
                deleteAllPhotos();
                return true;
            case R.id.menu_upd_objects_list:
                updateObjectsList();
                return true;
            case R.id.menu_clear_db:
                clearDB();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void updateObjectsList() {
        //ObjList.init1();
        ObjList.Init(this, false);
    }

    public void deleteAllPhotos(){
        ConfirmationDialog.show(this, "Удалить фото?", () -> {
            try {
                deleteFile(new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "ObjectsPhotos"));
                toast("Удалено");
            } catch (Exception e) {
                toast("Ошибка");
            }
        });

    }

    public void clearDB(){
        ConfirmationDialog.show(this, "Очистить кэшированные записи?", () ->{
            toastLong(this, "Удалено "+new DBHelper(this).removeAll()+" записей");
        });
    }

    public static void deleteFile(File file) throws Exception{
        if (file.exists())
            if (file.isFile()) file.delete();
            else if (file.isDirectory()) {
                for(File f : file.listFiles()) deleteFile(f);
                file.delete();
            }
    }



     class RecyclerAdapter extends RecyclerView.Adapter<RecyclerAdapter.ViewHolder> {
        private LayoutInflater inflater;

        RecyclerAdapter() {
            this.inflater = LayoutInflater.from(ObjectSelectionActivity.this);
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            final TextView text;
            ViewHolder(View view){
                super(view);
                text = view.findViewById(R.id.text);
            }
        }

        @Override
        @NonNull
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = inflater.inflate(R.layout.list_item, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, final int position) {
            View.OnClickListener onClickListener = (view) -> {
                ObjList.next(position);
                setTitle(R.string.title_activity_main2);
                showBackBtn(ObjectSelectionActivity.this, true);
                if (ObjList.getLastObject().getParts()==null)
                    startActivity( new Intent(ObjectSelectionActivity.this, CreateReportActivity.class) );
                else {
                    this.notifyDataSetChanged();
                }
            };
            holder.text.setText(ObjList.getLastObjsNamesList().get(position));
            holder.text.setOnClickListener(onClickListener);
        }

        @Override
        public int getItemCount() { return ObjList.getLastObjsNamesList().size(); }
    }

    public void toast(String txt){
        Toast.makeText(this, txt, Toast.LENGTH_LONG).show();
    }
}


