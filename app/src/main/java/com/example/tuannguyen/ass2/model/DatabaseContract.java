package com.example.tuannguyen.ass2.model;

import android.provider.BaseColumns;

/**
 * Created by tuannguyen on 31/08/2015.
 */
public final class DatabaseContract {
    public DatabaseContract() {
    }

    //this class is designed to hold database information
    public static abstract class CollectionTable {
        public static final String TABLE_NAME = "Collection";
        public static final String COLUMN_NAME_COLLECTION_NAME = "name";
        public static final String CREATE_TABLE_STMT = "CREATE TABLE " + TABLE_NAME + " (" + COLUMN_NAME_COLLECTION_NAME + " TEXT PRIMARY KEY)";
        public static final String DROP_TABLE = "DROP TABLE IF EXISTS " + TABLE_NAME;
    }

    public static abstract class ClippingTable implements BaseColumns{
        public static final String TABLE_NAME = "Clipping";
        public static final String COLUMN_NAME_IMAGE = "image";
        public static final String COLUMN_NAME_NOTES = "notes";
        public static final String COLUMN_NAME_DATE_CREATED = "created_date";
        public static final String COLUMN_NAME_COLLECTION_NAME = "collection";
        public static final String CREATE_TABLE_STMT = "CREATE TABLE " + TABLE_NAME + " (" + _ID + " INTEGER PRIMARY KEY, " +
                COLUMN_NAME_IMAGE + " TEXT, " + COLUMN_NAME_NOTES + " TEXT NOT NULL, " +
                COLUMN_NAME_DATE_CREATED + " INTEGER NOT NULL, " + COLUMN_NAME_COLLECTION_NAME + " TEXT , " +
                "FOREIGN KEY(" + COLUMN_NAME_COLLECTION_NAME + ")" + " REFERENCES " + CollectionTable.TABLE_NAME + "("
                + CollectionTable.COLUMN_NAME_COLLECTION_NAME + "))";
        public static final String DROP_TABLE = "DROP TABLE IF EXISTS " + TABLE_NAME;
    }

}
