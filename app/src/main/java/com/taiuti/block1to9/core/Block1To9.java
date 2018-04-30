package com.taiuti.block1to9.core;

import android.app.Application;
import android.util.DisplayMetrics;

/**
 * Created by filippo on 09/04/18.
 * v 1.0
 */

public class Block1To9 extends Application {

    // used to scale and adjust for different screens
    final int WIDTH_PIXEL_REF = 1080;
    final int HEIGHT_PIXEL_REF = 1776;

    float scaleX, scaleY;

    public void calculateScale() {
        DisplayMetrics metrics = getResources().getDisplayMetrics();
        scaleX = (float) metrics.widthPixels / WIDTH_PIXEL_REF;
        scaleY = (float) metrics.heightPixels / HEIGHT_PIXEL_REF;

    }

    public float getScaleX() {
        return scaleX;
    }

    public float getScaleY() {
        return scaleY;
    }

}
