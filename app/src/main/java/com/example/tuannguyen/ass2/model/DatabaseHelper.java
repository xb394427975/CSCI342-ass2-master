package com.example.tuannguyen.ass2.model;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by tuannguyen on 31/08/2015.
 */

//used for database connection
public class DatabaseHelper extends SQLiteOpenHelper {
    public static final String DB_NAME = "Scrapbook.db";
    public static final int VERSION = 1;
    public DatabaseHelper(Context context) {
        super(context, DB_NAME, null, VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(DatabaseContract.CollectionTable.CREATE_TABLE_STMT);
        db.execSQL(DatabaseContract.ClippingTable.CREATE_TABLE_STMT);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(DatabaseContract.CollectionTable.DROP_TABLE);
        db.execSQL(DatabaseContract.ClippingTable.DROP_TABLE);
        onCreate(db);
    }
}
