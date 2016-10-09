package com.example.tuannguyen.ass2.model;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.drawable.Drawable;
import android.net.Uri;

import com.example.tuannguyen.ass2.R;

import java.io.File;
import java.util.Date;

/**
 * Created by tuannguyen on 31/08/20 */
public class Clipping {
    private String mReferencedPath;
    private String mText;
    private Date mCreatedDate;
    private long mId;
    private Bitmap mImage;

    //construct
    public Clipping(String referencedPath, String text, long id)
    {
        mReferencedPath = referencedPath;
        mText = text;
        mCreatedDate = new Date();
        mId = id;
    }


    public Clipping(String referencedPath, String text, int createdDate, long id)
    {
        mReferencedPath = referencedPath;
        mText = text;
        mCreatedDate = new Date(createdDate * 1000);
        mId = id;
    }

    //getters
    public long getId() {
        return mId;
    }

    public String getReferencedPath() {
        return mReferencedPath;
    }

    public Bitmap getImage(Context context)
    {
        if (mImage == null)
        {
            mImage = getImageBitmap(context);
        }
        return mImage;
    }

    private Bitmap getImageBitmap(Context context) {
        Bitmap bitmap = null;
        File imageFile = null;
        if (mReferencedPath != null)
            imageFile = new File(mReferencedPath);
        //if user did not provide any image or if the file does not exist
        if (imageFile == null || !imageFile.exists()) {
            bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.unavailable);
            return bitmap;
        } else {
            bitmap = BitmapFactory.decodeFile(mReferencedPath);
            return bitmap;
        }
    }

    //returns notes
    public String getText() {
        return mText;
    }

    public Uri getImageUri()
    {
        if (mReferencedPath == null)
            return null;
        File imageFile = new File(mReferencedPath);
        if (!imageFile.exists())

            return null;
        else
            return Uri.fromFile(imageFile);
    }

    public Date getCreatedDate() {
        return mCreatedDate;
    }

    public Bitmap getThumbnail(Context context)
    {
        Bitmap bitmap = getImage(context);
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        float scaleWidth = 100.0f / width;
        float scaleHeight = 100.0f / height;
        // CREATE A MATRIX FOR THE MANIPULATION
        Matrix matrix = new Matrix();
        // RESIZE THE BIT MAP
        matrix.postScale(scaleWidth, scaleHeight);

        // "RECREATE" THE NEW BITMAP
        Bitmap newBitmap = Bitmap.createBitmap(bitmap, 0, 0, width, height, matrix, false);
        return newBitmap ;
    }
}
