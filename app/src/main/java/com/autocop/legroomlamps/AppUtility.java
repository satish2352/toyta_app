package com.autocop.legroomlamps;

import android.content.Context;
import android.content.DialogInterface;
import android.text.Html;

import androidx.appcompat.app.AlertDialog;

public class AppUtility {
    public static void Alert(Context mContext) {
        try {
            AlertDialog.Builder dialog = new AlertDialog.Builder(mContext, R.style.AlertDialogCustom);
            dialog.setTitle((CharSequence) "Alert");
            dialog.setPositiveButton((CharSequence) "Ok", (DialogInterface.OnClickListener) new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            dialog.setMessage((CharSequence) Html.fromHtml(mContext.getResources().getString(R.string.alert)));
            dialog.create().show();
        } catch (Exception e) {
        }
    }

    public static void PowerAlert(Context mContext) {
        try {
            AlertDialog.Builder dialog = new AlertDialog.Builder(mContext, R.style.AlertDialogCustom);
            dialog.setTitle((CharSequence) "Alert");
            dialog.setPositiveButton((CharSequence) "Ok", (DialogInterface.OnClickListener) new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            dialog.setMessage((CharSequence) mContext.getResources().getString(R.string.power_alert));
            dialog.create().show();
        } catch (Exception e) {
        }
    }

    public static void AutomodeAlert(Context mContext) {
        try {
            AlertDialog.Builder dialog = new AlertDialog.Builder(mContext, R.style.AlertDialogCustom);
            dialog.setTitle((CharSequence) "Alert");
            dialog.setPositiveButton((CharSequence) "Ok", (DialogInterface.OnClickListener) new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            dialog.setMessage((CharSequence) mContext.getResources().getString(R.string.auto_alert));
            dialog.create().show();
        } catch (Exception e) {
        }
    }
}
