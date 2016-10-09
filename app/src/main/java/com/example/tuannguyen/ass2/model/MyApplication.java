package com.example.tuannguyen.ass2.model;

import android.app.Application;

/**
 * Created by tuannguyen on 31/08/2015.
 */

//custom application, which is responsible for managing the scrapbook model object
public class MyApplication extends Application {
    private static ScrapbookModel mScrapbookModel;

    @Override
    public void onCreate() {
        super.onCreate();
        mScrapbookModel = new ScrapbookModel(getApplicationContext());
    }

    public ScrapbookModel getScrapbookModel() {
        return mScrapbookModel;
    }
}
