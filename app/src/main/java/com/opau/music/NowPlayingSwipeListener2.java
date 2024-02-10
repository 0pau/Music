package com.opau.music;

import android.animation.LayoutTransition;
import android.content.Context;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class NowPlayingSwipeListener2 implements View.OnTouchListener{

    boolean expanded = false;
    LinearLayout view;
    View content;
    View head;
    MainActivity parent;
    boolean swipeStarted = false;
    float lastY = -1;
    float firstY = -1;
    int minHeight = 0;
    int maxHeight = -1;
    int navHeight = 0;

    public NowPlayingSwipeListener2(Context c, LinearLayout v, View content, View head, MainActivity parentActivity) {
        view = v;
        view.measure(0, 0);
        minHeight = v.getMeasuredHeight();
        this.content = content;
        this.head = head;
        parent = parentActivity;
        ((BottomNavigation)parent.findViewById(R.id.nav)).getLayoutTransition().enableTransitionType(LayoutTransition.CHANGING);
        minHeight = view.getMeasuredHeight();
        maxHeight = parentActivity.getWindowManager().getCurrentWindowMetrics().getBounds().height();
        navHeight = parent.findViewById(R.id.nav).getMeasuredHeight();
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {

        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            view.getLayoutTransition().disableTransitionType(LayoutTransition.CHANGING);
        }

        if (event.getAction() == MotionEvent.ACTION_DOWN || event.getAction() == MotionEvent.ACTION_MOVE) {
            if (lastY == -1) {
                lastY = event.getRawY();
                firstY = lastY;
                return true;
            }
            if (event.getRawY() < lastY) {
                up(lastY-event.getRawY());
            } else {
                down(event.getRawY()-lastY);
            }
            lastY = event.getRawY();
        } else if (event.getAction() == MotionEvent.ACTION_UP) {
            end(firstY-event.getRawY());
        }

        return true;
    }

    private void down(float distance) {
        int newHeight = (int) (view.getHeight()-distance);
        if (newHeight > minHeight) {
            view.getLayoutParams().height = newHeight;
            view.requestLayout();
            content.setAlpha(content.getAlpha()-0.1f);
        }
    }

    private void up(float distance) {
        int newHeight = (int) (view.getHeight()+distance);
        if (newHeight < maxHeight-300) {
            view.getLayoutParams().height = newHeight;
            view.requestLayout();
        }
    }

    private void end(float maxDistance) {

        //click event!
        if (maxDistance < 20 && !expanded) {
            expand();
        }

        if (expanded) {
            if (maxDistance < -300) {
                collapse();
            } else {
                expand();
            }
        } else {
            if (maxDistance > 300) {
                expand();
            } else {
                collapse();
            }
        }
        lastY = -1;
    }

    public void expand() {
        expanded = true;
        head.setVisibility(View.GONE);
        view.getLayoutTransition().enableTransitionType(LayoutTransition.CHANGING);
        view.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        content.setVisibility(View.VISIBLE);
        content.animate().setDuration(300).alpha(1f).start();
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.weight = -1;
        parent.findViewById(R.id.nav).setLayoutParams(lp);
        swipeStarted = false;
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
        swipeStarted = false;
    }
}
