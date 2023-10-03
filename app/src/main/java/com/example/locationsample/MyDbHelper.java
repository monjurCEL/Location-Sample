package com.example.locationsample;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

public class MyDbHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "mydatabase.db";
    private static final int DATABASE_VERSION = 1;

    public MyDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS my_table (" +
                "_id INTEGER PRIMARY KEY," +
                "outlet_name TEXT," +
                "base_latitude REAL," +
                "base_longitude REAL," +
                "forced_latitude REAL," +
                "forced_longitude REAL," +
                "distance_base_forced REAL," +
                "timestamp DATETIME," +
                "current_latitude REAL," +
                "current_longitude REAL," +
                "distance REAL" +
                ")");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS my_table");
        onCreate(db);
    }
}
