package com.opau.music;

import android.animation.LayoutTransition;
import android.animation.ValueAnimator;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.LayoutAnimationController;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class NowPlayingSwipeListener implements View.OnTouchListener {

    boolean expanded = false;

    private final GestureDetector gestureDetector;
    LinearLayout view;
    View content;
    View head;
    MainActivity parent;

    int maxHeight = 500;
    int minHeight = 0;
    int maxDistance = 0;

    public NowPlayingSwipeListener (Context c, LinearLayout v, View content, View head, MainActivity parentActivity) {
        gestureDetector = new GestureDetector(c, new GestureListener());
        view = v;
        view.measure(0, 0);
        minHeight = v.getMeasuredHeight();
        this.content = content;
        this.head = head;
        parent = parentActivity;
        ((BottomNavigation)parent.findViewById(R.id.nav)).getLayoutTransition().enableTransitionType(LayoutTransition.CHANGING);
    }

    public void measure() {
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        return gestureDetector.onTouchEvent(event);
    }

    private final class GestureListener extends GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onScroll(@Nullable MotionEvent e1, @NonNull MotionEvent e2, float distanceX, float distanceY) {
            view.getLayoutTransition().disableTransitionType(LayoutTransition.CHANGING);
            if (distanceY > 0 && !expanded) {
                view.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, view.getHeight()+(int)distanceY));
            }
            if (distanceY < 0 && expanded) {
                view.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, view.getHeight()+(int)distanceY));
                content.setAlpha(content.getAlpha()-0.1f);
            }
            maxDistance = Math.abs((int)distanceY);
            return super.onScroll(e1, e2, distanceX, distanceY);
        }

        @Override
        public boolean onDown(MotionEvent e) {
            return true;
        }

        @Override
        public boolean onSingleTapUp(@NonNull MotionEvent e) {
            if (!expanded) {
                expand();
            }
            return super.onSingleTapUp(e);
        }



        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            try {
                if (velocityY > 7000 && expanded) {
                    collapse();
                } else if (velocityY < -7000 && !expanded) {
                    expand();
                } else {
                    if (expanded) {
                        expand();
                    } else {
                        collapse();
                    }
                }

                parent.nowPlayingExpanded = expanded;


            } catch (Exception exception) {
                exception.printStackTrace();
            }
            return false;
        }
    }

    public void expand() {
        expanded = true;
        head.setVisibility(View.GONE);
        view.getLayoutTransition().enableTransitionType(LayoutTransition.CHANGING);
        view.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        content.setVisibility(View.VISIBLE);
        content.animate().setDuration(300).alpha(1f).start();
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.weight = 1;
        parent.findViewById(R.id.nav).setLayoutParams(lp);
    }

    public void collapse() {
        expanded = false;
        content.setVisibility(View.GONE);
        head.setVisibility(View.VISIBLE);
        content.animate().setDuration(1).alpha(0f).start();
        view.getLayoutTransition().enableTransitionType(LayoutTransition.CHANGING);
        view.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.weight = 0;
        parent.findViewById(R.id.nav).setLayoutParams(lp);
    }
}
