package com.nordnetab.chcp.main.view;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;

import org.apache.cordova.CallbackContext;

/**
 * Created by Nikolay Demyankov on 29.12.15.
 * <p/>
 * Helper class to generate application update request dialog.
 */
public class AppUpdateRequestDialog {

    private final Context context;
    private final String message;
    private final String storeURL;
    private final CallbackContext callback;

    /**
     * Constructor.
     *
     * @param context  application context
     * @param message  message to show in the dialog
     * @param storeURL application package on the GooglePlay
     * @param callback JS callback
     */
    public AppUpdateRequestDialog(Context context, String message, String storeURL, CallbackContext callback) {
        this.context = context;
        this.message = message;
        this.storeURL = storeURL;
        this.callback = callback;
    }

    /**
     * Show dialog to the user.
     */
    public void show() {
        final AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(context);
        dialogBuilder.setCancelable(false);
        dialogBuilder.setMessage(message);
        dialogBuilder.setPositiveButton(context.getString(android.R.string.ok), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                callback.success();
                dialog.dismiss();

                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse(storeURL));
                context.startActivity(intent);
            }
        });
        dialogBuilder.setNegativeButton(context.getString(android.R.string.cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                callback.error("");
            }
        });

        dialogBuilder.show();
    }

}
