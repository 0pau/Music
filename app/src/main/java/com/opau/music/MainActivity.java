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

    Fragment active;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        SongsFragment songs = new SongsFragment();
        ArtistsFragment artists = new ArtistsFragment();
        PlaylistsFragment playlists = new PlaylistsFragment();
        active = songs;

        FragmentManager manager = getSupportFragmentManager();

        manager.beginTransaction().add(R.id.frame, songs, "songs").commit();
        manager.beginTransaction().add(R.id.frame, artists, "songs").hide(artists).commit();
        manager.beginTransaction().add(R.id.frame, playlists, "songs").hide(playlists).commit();

        FrameLayout fl = findViewById(R.id.frame);

        BottomNavigation nav = findViewById(R.id.nav);
        nav.setOnItemSelectedListener(index -> {
            switch (index) {
                case 0:
                    manager.beginTransaction().hide(active).show(songs).commit();
                    active = songs;
                    break;
                case 1:
                    manager.beginTransaction().hide(active).show(artists).commit();
                    active = artists;
                    break;
                case 2:
                    //albums
                    break;
                case 3:
                    manager.beginTransaction().hide(active).show(playlists).commit();
                    active = playlists;
                    break;
            }
        });
    }
}