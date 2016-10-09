package com.example.tuannguyen.ass2.model;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Environment;
import android.util.Log;

import com.example.tuannguyen.ass2.model.DatabaseContract.*;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by tuannguyen on 31/08/2015.
 */
public class ScrapbookModel {
    private DatabaseHelper mDatabaseHelper;
    private Context mContext;


    //constructor
    public ScrapbookModel(Context context) {
        mContext = context;
        mDatabaseHelper = new DatabaseHelper(context);
        SQLiteDatabase db = mDatabaseHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM " + ClippingTable.TABLE_NAME, null);
        cursor.moveToFirst();
    }


    //returns all collections

    public List<Collection> getCollectionList() {
        SQLiteDatabase db = mDatabaseHelper.getReadableDatabase();
        List<Collection> collections = new ArrayList<Collection>();
        Cursor cursor = db.query(CollectionTable.TABLE_NAME, new String[]{CollectionTable.COLUMN_NAME_COLLECTION_NAME}, null, null, null, null, CollectionTable.COLUMN_NAME_COLLECTION_NAME);
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            String name = cursor.getString(cursor.getColumnIndex(CollectionTable.COLUMN_NAME_COLLECTION_NAME));
            collections.add(new Collection(name));
            cursor.moveToNext();
        }
        cursor.close();
        return collections;
    }

    //create a new collection
    public void createCollection(String name) {
        SQLiteDatabase db = mDatabaseHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(CollectionTable.COLUMN_NAME_COLLECTION_NAME, name);
        db.insert(CollectionTable.TABLE_NAME, null, values);
    }


    //add a clipping to a collection
    public void addClippingToCollection(long clippingIdx, String collectionName) {
        SQLiteDatabase db = mDatabaseHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(ClippingTable.COLUMN_NAME_COLLECTION_NAME, collectionName);
        db.update(ClippingTable.TABLE_NAME, values, ClippingTable._ID + "= ?", new String[]{"" + clippingIdx});
    }

    //edit a clipping
    public void editClipping(long id, String oldReferencedPath, String notes, InputStream is, boolean updateImage) throws IOException {
        SQLiteDatabase db = mDatabaseHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        Date currentDate = new Date();
        String date = currentDate.toString();
        String filePath = null;

        if (is != null)
        {
            try {
                //copy the new image to the stored location
                String copiedImageFileName  = id + " - " + date + ".jpg";
                filePath = copyFile(is, copiedImageFileName);
            } catch (IOException e) {
                e.printStackTrace();
            }
            is.close();
        }

        //if we want to update the image
        if (updateImage)
        {
            //delete the old file
            if (oldReferencedPath != null)
                deleteFile(oldReferencedPath);
            values.put(ClippingTable.COLUMN_NAME_IMAGE, filePath);
        }

        values.put(ClippingTable.COLUMN_NAME_NOTES, notes);
        db.update(ClippingTable.TABLE_NAME, values, ClippingTable._ID + "= ?", new String[]{"" + id});
    }

    //edit a collection name
    public void editCollection(String oldCollectionName, String newCollectionName)
    {
        createCollection(newCollectionName);
        SQLiteDatabase db = mDatabaseHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(ClippingTable.COLUMN_NAME_COLLECTION_NAME, newCollectionName);
        db.update(ClippingTable.TABLE_NAME, values, ClippingTable.COLUMN_NAME_COLLECTION_NAME + "= ?", new String[]{"" + oldCollectionName});
        db.delete(CollectionTable.TABLE_NAME, CollectionTable.COLUMN_NAME_COLLECTION_NAME + " = ?", new String[]{oldCollectionName});
    }

    //create a clipping
    public Clipping createClipping(String notes, InputStream is) throws IOException {
            SQLiteDatabase db = mDatabaseHelper.getWritableDatabase();
            ContentValues values = new ContentValues();
            Date currentDate = new Date();
            String date = currentDate.toString();

            String filePath = null;
            if (is != null)
            {
                try {
                    String copiedImageFileName  = date + ".jpg";
                    filePath = copyFile(is, copiedImageFileName);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                is.close();
            }

            values.put(ClippingTable.COLUMN_NAME_DATE_CREATED, currentDate.getTime() / 1000);
            values.put(ClippingTable.COLUMN_NAME_IMAGE, filePath);
            values.put(ClippingTable.COLUMN_NAME_NOTES, notes);
            long id = db.insert(ClippingTable.TABLE_NAME, null, values);
            Clipping clipping = new Clipping(filePath, notes, id);

            return clipping;
    }


    public void deleteCollection(String name) {
        SQLiteDatabase db = mDatabaseHelper.getReadableDatabase();
        Cursor cursor = db.query(ClippingTable.TABLE_NAME, new String[]{ClippingTable._ID}
                , "LOWER(" + ClippingTable.COLUMN_NAME_COLLECTION_NAME + ") = ?", new String[]{name.toLowerCase()} , null, null, null);
        cursor.moveToFirst();

        while (!cursor.isAfterLast())
        {
            int id = cursor.getInt(cursor.getColumnIndex(ClippingTable._ID));
            deleteClipping(id);
            cursor.moveToNext();
        }
        cursor.close();
        db = mDatabaseHelper.getWritableDatabase();
        db.delete(CollectionTable.TABLE_NAME, CollectionTable.COLUMN_NAME_COLLECTION_NAME + "=?", new String[]{name});
    }

    //delete a clipping
    public void deleteClipping(long id) {
        SQLiteDatabase db = mDatabaseHelper.getReadableDatabase();
        Cursor cursor = db.query(ClippingTable.TABLE_NAME, new String[]{ClippingTable.COLUMN_NAME_IMAGE}
                , ClippingTable._ID + " = ?", new String[]{id + ""} , null, null, null);
        if (cursor.getCount() != 0)
        {
            cursor.moveToFirst();
            String referencedFilePath = cursor.getString(cursor.getColumnIndex(ClippingTable.COLUMN_NAME_IMAGE));
            if (referencedFilePath != null)
                deleteFile(referencedFilePath);
        }
        cursor.close();
        db = mDatabaseHelper.getWritableDatabase();
        db.delete(ClippingTable.TABLE_NAME, ClippingTable._ID + "=?", new String[]{"" + id});
    }

    //get all clipping from a collection
    public List<Clipping> getClippingsFromCollection(String name) {
        SQLiteDatabase db = mDatabaseHelper.getReadableDatabase();
        List<Clipping> clippings = new ArrayList<Clipping>();
        String whereStatement = null;
        String[] whereArgs = null;
        if (name != null) {
            whereStatement = "LOWER(" + ClippingTable.COLUMN_NAME_COLLECTION_NAME + ") = ?";
            whereArgs = new String[]{name.toLowerCase()};
        }

        Cursor cursor = db.query(ClippingTable.TABLE_NAME, new String[]{ClippingTable.COLUMN_NAME_IMAGE, ClippingTable.COLUMN_NAME_NOTES, ClippingTable._ID}
                , whereStatement, whereArgs, null, null, ClippingTable._ID);

        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            String imageFile = cursor.getString(cursor.getColumnIndex(ClippingTable.COLUMN_NAME_IMAGE));
            String notes = cursor.getString(cursor.getColumnIndex(ClippingTable.COLUMN_NAME_NOTES));
            int id = cursor.getInt(cursor.getColumnIndex(ClippingTable._ID));
            clippings.add(new Clipping(imageFile, notes, id));
            cursor.moveToNext();
        }
        cursor.close();
        return clippings;
    }


    //search for a clipping based on the provided search string
    public List<Clipping> getClippingsFromSearchString(String searchString) {
        SQLiteDatabase db = mDatabaseHelper.getReadableDatabase();
        searchString = "%" + searchString + "%";
        List<Clipping> clippings = new ArrayList<Clipping>();
        Cursor cursor = db.query(ClippingTable.TABLE_NAME, new String[]{ClippingTable.COLUMN_NAME_IMAGE, ClippingTable.COLUMN_NAME_NOTES, ClippingTable._ID}
                , "LOWER(" + ClippingTable.COLUMN_NAME_NOTES + ") LIKE ?", new String[]{searchString.toLowerCase()}
                , null, null, ClippingTable._ID);

        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            String imageFile = cursor.getString(cursor.getColumnIndex(ClippingTable.COLUMN_NAME_IMAGE));
            String notes = cursor.getString(cursor.getColumnIndex(ClippingTable.COLUMN_NAME_NOTES));
            int id = cursor.getInt(cursor.getColumnIndex(ClippingTable._ID));
            clippings.add(new Clipping(imageFile, notes, id));
            cursor.moveToNext();
        }
        cursor.close();
        return clippings;
    }

    //checks if a collection exists
    public boolean findCollection(String name) {
        SQLiteDatabase db = mDatabaseHelper.getReadableDatabase();
        Cursor cursor = db.query(CollectionTable.TABLE_NAME, new String[]{CollectionTable.COLUMN_NAME_COLLECTION_NAME}, "LOWER(" + CollectionTable.COLUMN_NAME_COLLECTION_NAME + ") = ?", new String[]{name.toLowerCase()}, null, null, null);
        int count = cursor.getCount();
        cursor.close();
        return (count != 0);
    }

    //get a clipping
    public Clipping getClipping(long id)
    {
        SQLiteDatabase db = mDatabaseHelper.getReadableDatabase();
        Cursor cursor = db.query(ClippingTable.TABLE_NAME, new String[]{ClippingTable.COLUMN_NAME_IMAGE, ClippingTable.COLUMN_NAME_NOTES}
                , ClippingTable._ID + " = ?", new String[] { id + ""}, null, null, null);

        cursor.moveToFirst();
        String imageFile = cursor.getString(cursor.getColumnIndex(ClippingTable.COLUMN_NAME_IMAGE));
        String notes = cursor.getString(cursor.getColumnIndex(ClippingTable.COLUMN_NAME_NOTES));
        Clipping clipping = new Clipping(imageFile, notes, id);
        cursor.close();
        return clipping;
    }


    private String copyFile(InputStream is, String destFileName) throws IOException {
        //copy the file
        OutputStream out = null;
        String filePath;
        if (is == null)
        {
            return null;
        }
        try {
            //if sd card is available, create a copy of the image in the sd card
            if (isExternalStorageWritable()) {
                File externalDir = Environment.getExternalStorageDirectory();
                File savedDirectory = new File(externalDir, "scrapbook");
                if (!savedDirectory.exists()) {
                    savedDirectory.mkdirs();
                }
                File savedFile = new File(savedDirectory, destFileName);
                boolean d = savedFile.createNewFile();
                Log.d("Result of", d + "");
                out = new FileOutputStream(savedFile);
                filePath = savedFile.getAbsolutePath();

            } else {
                //if not, create the file in sandbox
                out = mContext.openFileOutput(destFileName, Context.MODE_PRIVATE);
                filePath = mContext.getFileStreamPath(destFileName).getAbsolutePath();
            }
            byte[] buffer = new byte[1024];
            int read;
            while ((read = is.read(buffer)) != -1) {
                out.write(buffer, 0, read);
            }
        } finally {
            if (out != null)
                out.close();
        }
        return filePath;
    }

    private void deleteFile(String fileName) {
        File imageFile = new File(fileName);
        if (imageFile.exists() && imageFile.isFile())
        {
            imageFile.delete();
        }
    }

    //check if the device has writable external storage
    public static boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        }
        return false;
    }


}

