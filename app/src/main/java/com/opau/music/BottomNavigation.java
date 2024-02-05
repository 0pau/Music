package com.opau.music;

import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.annotation.ColorInt;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;

import com.google.android.material.internal.NavigationMenu;

public class BottomNavigation extends LinearLayout {

    int menu;
    Menu m;
    LinearLayout layout;
    OnItemSelectedListener listener;

    public BottomNavigation(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        layout = (LinearLayout) inflate(context, R.layout.menu_layout, this).findViewById(R.id.menu_layout);

        layout.setOrientation(this.getOrientation());
        TypedArray a = context.getTheme().obtainStyledAttributes(attrs, R.styleable.BottomNavigation,0,0);

        menu = a.getResourceId(R.styleable.BottomNavigation_menuResource, R.menu.main);

        MenuInflater mi = new MenuInflater(context);

        PopupMenu p = new PopupMenu(context, null);
        m = p.getMenu();
        mi.inflate(menu, m);

        for (int i = 0; i < m.size(); i++) {
            MenuItem itm = m.getItem(i);

            if (!itm.isVisible()) {
                continue;
            }

            View v = inflate(getContext(), R.layout.menu_item_layout, null);

            LinearLayout button = v.findViewById(R.id.button_layout);
            if (this.getOrientation() == VERTICAL) {
                button.setOrientation(HORIZONTAL);
                button.setGravity(Gravity.START|Gravity.CENTER_VERTICAL);
            }

            TextView tv = v.findViewById(R.id.item_title);
            tv.setText(itm.getTitle().toString());

            ImageView ic = v.findViewById(R.id.item_icon);
            ic.setImageDrawable(itm.getIcon());

            CardView cv = v.findViewById(R.id.card);
            int finalI = i;
            cv.setOnClickListener(view -> {
                selectItem(finalI);
            });

            v.setLayoutParams(new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT, 1.0f));
            layout.addView(v);
        }

        a.recycle();
    }

    @SuppressLint("ResourceType")
    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        selectItem(0);
    }

    void selectItem(int index) {
        unselectAll();
        View v = layout.getChildAt(index);
        v.setSelected(true);
        TextView tv = v.findViewById(R.id.item_title);
        tv.setSelected(true);
        ImageView iv = v.findViewById(R.id.item_icon);
        iv.setSelected(true);

        if (listener != null) {
            listener.onEvent(index);
        }

    }

    void unselectAll() {
        for (int i = 0; i < layout.getChildCount(); i++) {
            View v = layout.getChildAt(i);
            v.setSelected(false);
            TextView tv = v.findViewById(R.id.item_title);
            tv.setSelected(false);
            ImageView iv = v.findViewById(R.id.item_icon);
            iv.setSelected(false);
        }
    }

    public interface OnItemSelectedListener {
        void onEvent(int index);
    }

    public void setOnItemSelectedListener(OnItemSelectedListener l) {
        listener = l;
    }
}
