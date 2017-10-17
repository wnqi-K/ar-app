package com.comp30022.arrrrr.ar;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import com.comp30022.arrrrr.ArViewActivity;
import com.comp30022.arrrrr.MainViewActivity;
import com.comp30022.arrrrr.utils.Constants;

import java.util.logging.Logger;

/**
 * Created by xiaoyuguo on 17/10/2017.
 */

/**
 * This is a class that help manage ArViewActivity's UI
 * */

public class ArUIHelper {

    public ArUIHelper() {
    }

    /**
     * This method help control the visibility of Ar View Icon
     * */
    public void setVisibility(ImageView vis, ImageView invis1, ImageView invis2) {
        vis.setVisibility(View.VISIBLE);
        invis1.setVisibility(View.INVISIBLE);
        invis2.setVisibility(View.INVISIBLE);
    }

    /**
     * this method is called in ArViewActivity when
     * the POI is close enough to user
     * it pops up a dialog to ask user whether to exit the Ar View
     * */
    public void showAlert(Context context, final Activity activity, AugmentedPOI POI) {

        AlertDialog alertDialog = new AlertDialog.Builder(context).create();
        alertDialog.setTitle("Alert");
        alertDialog.setMessage(POI.getPoiName() + " is only " + ArViewActivity.DISTANCE_POP_UP_BOUND
                + " metres around you.\n"
                + Constants.ALTER_QUESTION);

        alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "No",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        activity.finish();
                    }
                });

        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "Yes",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        alertDialog.show();
    }

}
