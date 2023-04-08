package com.opau.music;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import android.annotation.SuppressLint;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        SongsFragment songs = new SongsFragment();
        Fragment active = songs;

        FragmentManager manager = getSupportFragmentManager();

        manager.beginTransaction().add(R.id.frame, songs, "songs").commit();

        FrameLayout fl = findViewById(R.id.frame);

        BottomNavigation nav = findViewById(R.id.nav);
        nav.setOnItemSelectedListener(index -> {
            switch (index) {
                case 0:
                    manager.beginTransaction().hide(active).show(songs).commit();
                    break;
            }
        });
    }
}