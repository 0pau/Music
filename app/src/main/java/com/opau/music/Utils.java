package com.opau.music;

import android.animation.ValueAnimator;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.HardwareRenderer;
import android.graphics.RecordingCanvas;
import android.graphics.RenderEffect;
import android.graphics.RenderNode;
import android.graphics.Shader;
import android.renderscript.RenderScript;
import android.view.View;
import android.widget.LinearLayout;

public class Utils {
    public static String formatMsDuration(long duration) {
        int all_sec = (int)duration / 1000;
        int all_m = all_sec / 60;
        all_sec -= (all_m*60);

        String formatted_min = String.valueOf(all_m);
        if (all_m < 10) {
            formatted_min = "0" + formatted_min;
        }

        String formatted_sec = String.valueOf(all_sec);
        if (all_sec < 10) {
            formatted_sec = "0" + formatted_sec;
        }

        return formatted_min + ":" + formatted_sec;
    };

    public static void animateViewHeightFromWrapToMatch(View v, int duration) {
        int f = 0;
        int t = 0;

        v.measure(v.getWidth(), LinearLayout.LayoutParams.WRAP_CONTENT);
        f = v.getMeasuredHeight();
        v.measure(v.getWidth(), LinearLayout.LayoutParams.MATCH_PARENT);
        t = v.getMeasuredHeight();

        ValueAnimator anim = ValueAnimator.ofInt(f, t);
        anim.setDuration(duration);

        anim.start();

    }
}
