package com.comp30022.arrrrr.ar;

import android.view.View;
import android.widget.ImageView;

/**
 * Created by xiaoyuguo on 17/10/2017.
 */

public class ArUIHelper {

    public ArUIHelper(){
    }

    public void setVisibility (ImageView vis, ImageView invis1, ImageView invis2){
        vis.setVisibility(View.VISIBLE);
        invis1.setVisibility(View.INVISIBLE);
        invis2.setVisibility(View.INVISIBLE);
    }
}
