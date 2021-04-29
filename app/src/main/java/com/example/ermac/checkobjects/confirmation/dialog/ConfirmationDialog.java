package com.example.ermac.checkobjects.confirmation.dialog;

import android.content.Context;
import android.support.v7.app.AlertDialog;

public class ConfirmationDialog {

    public static void show(Context context, String msg, PositiveAction action) {
        AlertDialog.Builder confirmationDialog = new AlertDialog.Builder(context);
        confirmationDialog.setMessage(msg);
        confirmationDialog.setNegativeButton("Отмена", (dialog, id) -> dialog.cancel());
        confirmationDialog.setPositiveButton("OK", (dialog, id) -> {
            action.run();
            dialog.cancel();
        });

        confirmationDialog.create().show();
    }

}
