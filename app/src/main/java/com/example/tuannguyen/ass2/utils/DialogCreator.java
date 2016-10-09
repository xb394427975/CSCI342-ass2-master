package com.example.tuannguyen.ass2.utils;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.View;

/**
 * Created by tuannguyen on 2/09/2015.
 */
public final class DialogCreator {
    public static void createDialog(Context context, String title, String message, View customView, String positiveButton, DialogInterface.OnClickListener positiveListener, String negativeButton, DialogInterface.OnClickListener negativeListener)
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        if (title != null)
            builder.setTitle(title);
        if (message != null)
            builder.setMessage(message);
        if (customView != null)
            builder.setView(customView);
        if (positiveButton != null)
        {
            builder.setPositiveButton(positiveButton, positiveListener);
        }
        if (negativeButton != null)
        {
            builder.setNegativeButton(negativeButton, negativeListener);
        }
        builder.create().show();
    }
}
