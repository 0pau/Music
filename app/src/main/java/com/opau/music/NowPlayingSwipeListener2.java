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
import androidx.constraintlayout.widget.ConstraintLayout;

public class NowPlayingSwipeListener2 implements View.OnTouchListener{

    boolean expanded = false;
    ConstraintLayout view;
    View content;
    View head;
    MainActivity parent;
    boolean swipeStarted = false;
    float lastY = -1;
    float firstY = -1;
    int minHeight = 0;
    int maxHeight = -1;
    int navHeight = 0;

    public NowPlayingSwipeListener2(Context c, ConstraintLayout v, View content, View head, MainActivity parentActivity) {
        view = v;
        view.measure(0, 0);
        minHeight = v.getMeasuredHeight();
        this.content = content;
        this.head = head;
        parent = parentActivity;
        //((BottomNavigation)parent.findViewById(R.id.nav)).getLayoutTransition().enableTransitionType(LayoutTransition.CHANGING);
        minHeight = view.getMeasuredHeight();
        maxHeight = parentActivity.getWindowManager().getCurrentWindowMetrics().getBounds().height();
        //content.getLayoutParams().height = maxHeight;
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
            if (content.getAlpha() > 0) {
                content.setAlpha(content.getAlpha()-0.05f);
            }
            if (!swipeStarted) {
                swipeStarted = true;
                content.getLayoutParams().height = 0;
            }
            ((ConstraintLayout.LayoutParams)content.getLayoutParams()).bottomToBottom = 0;
            view.getLayoutParams().height = newHeight;
            view.requestLayout();
            LinearLayout.LayoutParams navLp = (LinearLayout.LayoutParams) parent.findViewById(R.id.nav).getLayoutParams();
            if (navLp.bottomMargin < 0) {
                navLp.bottomMargin += (int)distance/2;
            } else {
                navLp.bottomMargin = 0;
            }
            parent.findViewById(R.id.nav).setLayoutParams(navLp);
            //content.setAlpha(content.getAlpha()-0.1f);
        }
    }

    private void up(float distance) {
        int newHeight = (int) (view.getHeight()+distance);
        if (newHeight < maxHeight) {

            if (content.getAlpha() < 1) {
                content.setAlpha(content.getAlpha()+0.05f);
            }

            if (!swipeStarted) {
                swipeStarted = true;
                //
                //content.getLayoutParams().height = maxHeight;
                content.setVisibility(View.VISIBLE);
                content.getLayoutParams().height = maxHeight;
                ((ConstraintLayout.LayoutParams)content.getLayoutParams()).bottomToBottom = 1;
                content.requestLayout();
                content.animate().setDuration(300).alpha(1f).start();
            }
            LinearLayout.LayoutParams navLp = (LinearLayout.LayoutParams) parent.findViewById(R.id.nav).getLayoutParams();
            navLp.bottomMargin -= (int)distance/2;
            parent.findViewById(R.id.nav).setLayoutParams(navLp);
            parent.findViewById(R.id.nav).requestLayout();
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
        //content.setVisibility(View.VISIBLE);
        content.animate().setDuration(300).alpha(1f).start();
        /*
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.weight = -1;
        parent.findViewById(R.id.nav).setLayoutParams(lp);*/

        LinearLayout.LayoutParams navLp = (LinearLayout.LayoutParams) parent.findViewById(R.id.nav).getLayoutParams();
        navLp.bottomMargin = -1*(navHeight+parent.findViewById(R.id.nav).getMeasuredHeight());
        parent.findViewById(R.id.nav).setLayoutParams(navLp);
        parent.findViewById(R.id.nav).requestLayout();

        swipeStarted = false;
        //content.findViewById(R.id.albumArtLayout).setPadding(80,80,80,80);
    }

    public void collapse() {
        expanded = false;
        //content.setVisibility(View.INVISIBLE);
        content.animate().alpha(0).setDuration(250).start();
        head.setVisibility(View.VISIBLE);
        view.getLayoutTransition().enableTransitionType(LayoutTransition.CHANGING);
        view.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.weight = 0;
        parent.findViewById(R.id.nav).setLayoutParams(lp);
        swipeStarted = false;
    }
}
