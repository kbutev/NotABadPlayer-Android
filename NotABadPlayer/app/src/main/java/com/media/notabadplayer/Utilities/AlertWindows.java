package com.media.notabadplayer.Utilities;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;

public class AlertWindows {
    public static void showAlert(Context context,
                                 int title, int description, int actionName, DialogInterface.OnClickListener action)
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        
        if (title != 0)
        {
            builder.setTitle(title);
        }

        if (description != 0)
        {
            builder.setMessage(description);
        }  
        
        builder.setCancelable(true);

        builder.setPositiveButton(
                actionName,
                action);
        
        AlertDialog alert = builder.create();
        alert.show();
    }

    public static void showAlert(Context context,
                                 String title, String description, String actionName, DialogInterface.OnClickListener action)
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);

        if (title != null)
        {
            builder.setTitle(title);
        }

        if (description != null)
        {
            builder.setMessage(description);
        }

        builder.setCancelable(true);

        builder.setPositiveButton(
                actionName,
                action);

        AlertDialog alert = builder.create();
        alert.show();
    }

    public static void showAlert(Context context,
                                 int title, int description,
                                 int actionName, DialogInterface.OnClickListener action,
                                 int cancelName)
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);

        if (title != 0)
        {
            builder.setTitle(title);
        }

        if (description != 0)
        {
            builder.setMessage(description);
        }

        builder.setCancelable(true);

        builder.setPositiveButton(
                actionName,
                action);
        
        builder.setNegativeButton(
                cancelName,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
        
        AlertDialog alert = builder.create();
        alert.show();
    }

    public static void showAlert(Context context,
                                 String title, String description,
                                 String actionName, DialogInterface.OnClickListener action,
                                 String cancelName)
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);

        if (title != null)
        {
            builder.setTitle(title);
        }

        if (description != null)
        {
            builder.setMessage(description);
        }

        builder.setCancelable(true);

        builder.setPositiveButton(
                actionName,
                action);

        builder.setNegativeButton(
                cancelName,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });

        AlertDialog alert = builder.create();
        alert.show();
    }
}
