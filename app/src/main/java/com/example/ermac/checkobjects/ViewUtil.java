package com.example.ermac.checkobjects;

import android.app.Activity;
import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;
import android.widget.Toast;

public class ViewUtil {
    public static Context appContext;

    public static String getTxt(Activity activity, int viewId){
        return ((TextView)activity.findViewById(viewId)).getText().toString();
    }

    public static void showBackBtn(AppCompatActivity activity, boolean show){
        activity.getSupportActionBar().setDisplayHomeAsUpEnabled(show);
        activity.getSupportActionBar().setDisplayShowHomeEnabled(show);
    }

    public static void toastShort(Context context, String txt){
        Toast.makeText(context, txt, Toast.LENGTH_SHORT).show();
    }

    public static void toastLong(Context context, String txt){
        Toast.makeText(context, txt, Toast.LENGTH_SHORT).show();
    }
}
