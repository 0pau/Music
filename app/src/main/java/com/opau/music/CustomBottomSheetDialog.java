package com.opau.music;

import android.animation.ValueAnimator;
import android.content.Context;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;

import java.util.logging.Handler;

public class CustomBottomSheetDialog extends BottomSheetDialog {
    public CustomBottomSheetDialog(@NonNull Context context) {
        super(context);
    }

    @Override
    protected void onStart() {
        super.onStart();
        getWindow().setDimAmount(0.5f);
        Animation a = AnimationUtils.loadAnimation(getContext(), R.anim.slide_up);
        a.setInterpolator(Interpolators.easeOut);
        findViewById(com.google.android.material.R.id.design_bottom_sheet).setAnimation(a);
    }

    @Override
    public void dismiss() {
        findViewById(com.google.android.material.R.id.design_bottom_sheet).clearAnimation();
        Animation a = AnimationUtils.loadAnimation(getContext(), R.anim.slide_down);
        a.setInterpolator(Interpolators.easeInOut);
        a.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                dsup();
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        findViewById(com.google.android.material.R.id.design_bottom_sheet).setAnimation(a);

        ValueAnimator va = ValueAnimator.ofFloat(0.5f, 0f);
        va.start();
        va.addUpdateListener(animation -> getWindow().setDimAmount((float)animation.getAnimatedValue()));
    }

    private void dsup() {
        super.dismiss();
    }
}
