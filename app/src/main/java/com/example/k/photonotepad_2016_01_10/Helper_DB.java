package com.example.k.photonotepad_2016_01_10;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class Helper_DB extends SQLiteOpenHelper{

    public Helper_DB(Context context) {
        super(context,"db",null,1);
    }

    public void onCreate(SQLiteDatabase db) {
        Log.d("myLogs", "--- onCreate database ---");
        // создаем таблицу с полями
        //tODO сделать имя таблицы чтоб бралось из переменной

        db.execSQL("create table " + Activity_Note.tableName + " ("
                + "id integer primary key autoincrement,"
                + "name text,"
                + "sku text,"
                + "category text,"
                + "description text"

                + ");");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
