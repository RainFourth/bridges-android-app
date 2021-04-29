package com.example.ermac.checkobjects;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;

import com.example.ermac.checkobjects.CreateReportActivity;

public class DBHelper extends SQLiteOpenHelper {
    private static String dbPath = /*Environment.getExternalStorageDirectory()+"/"+*/"ObjsCacheDB";

    public static final String OBJS_TABLE = "objsTable";
    public static final String ID = "_id";
    public static final String OBJ_NAME = "objName";
    public static final String PART_NAME = "partName";
    public static final String DESCRIPTION = "descriotion";
    public static final String PHOTO_URI = "PhotoUri";


    public DBHelper(/*@androidx.annotation.Nullable*/ Context context/*, String dbPath*/) {
        super(context, dbPath, null, 1);
        //this.dbPath = dbPath;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        //создание таблицы с полями
        db.execSQL("CREATE TABLE " + OBJS_TABLE + "("
                + ID + " INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT UNIQUE,"
                + OBJ_NAME + " TEXT NOT NULL,"
                + PART_NAME + " TEXT NOT NULL,"
                + DESCRIPTION + " TEXT NOT NULL,"
                + PHOTO_URI + " TEXT NOT NULL" +
                ");"
        );
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    public void addObjInfo(CreateReportActivity.ObjInfo objInfo){
        try (SQLiteDatabase db = this.getWritableDatabase()) {
            ContentValues values = new ContentValues(4);
            values.put(OBJ_NAME, objInfo.getObj());
            values.put(PART_NAME, objInfo.getPart());
            values.put(DESCRIPTION, objInfo.getDescription());
            values.put(PHOTO_URI, objInfo.getFileUri().toString());

            db.insert(OBJS_TABLE, null, values);
        }
    }

    public int getLastId(){
        try (SQLiteDatabase db = this.getWritableDatabase();
        Cursor c = db.query(OBJS_TABLE, null, null, null, null, null, null)) {
            if (c.moveToLast()) return c.getInt(c.getColumnIndex(ID));
            else return -1;
        } catch (NullPointerException npe){
            return -1;
        }
    }

    public CreateReportActivity.ObjInfo get(int id){
        try (SQLiteDatabase db = this.getWritableDatabase();
             Cursor c = db.query(OBJS_TABLE, null, ID +"="+id, null, null, null, null)) {
            if (c.moveToLast() && c.getCount()==1)
                return new CreateReportActivity.ObjInfo(
                        c.getString(c.getColumnIndex(OBJ_NAME)),
                        c.getString(c.getColumnIndex(PART_NAME)),
                        c.getString(c.getColumnIndex(DESCRIPTION)),
                        Uri.parse(c.getString(c.getColumnIndex(PHOTO_URI)))
                );
            else return null;

        } catch (NullPointerException npe){
            return null;
        }
    }

    public void remove(int id){
        try (SQLiteDatabase db = this.getWritableDatabase()) {
            db.delete(OBJS_TABLE, ID +"="+id, null);
        } catch (NullPointerException npe){

        }
    }

    public boolean notEmpty(){
        try (SQLiteDatabase db = this.getWritableDatabase();
             Cursor c = db.query(OBJS_TABLE, null, null, null, null, null, null)){
            return c.getCount()!=0;
        } catch (NullPointerException npe){
            return false;
        }
    }

    public boolean isEmpty(){
        try (SQLiteDatabase db = this.getWritableDatabase();
             Cursor c = db.query(OBJS_TABLE, null, null, null, null, null, null)){
            return c.getCount()==0;
        } catch (NullPointerException npe){
            return true;
        }
    }

    public int size(){
        try (SQLiteDatabase db = this.getWritableDatabase();
             Cursor c = db.query(OBJS_TABLE, null, null, null, null, null, null)){
            return c.getCount();
        } catch (NullPointerException npe){
            return 0;
        }
    }

    public int removeAll(){
        try (SQLiteDatabase db = this.getWritableDatabase()) {
            return db.delete(OBJS_TABLE, null, null);
        } catch (NullPointerException npe){
            return 0;
        }
    }
}
