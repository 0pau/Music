package com.opau.music;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

public class MainActivity extends AppCompatActivity {

    Fragment active;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            Intent i = new Intent(this, PermissionAlert.class);
            i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(i);
            finish();
        }

        View loadIndicator = getLayoutInflater().inflate(R.layout.library_load_pb, null);
        loadIndicator.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        ((Toolbar)findViewById(R.id.toolbar2)).addView(loadIndicator);

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

        //loadIndicator.setVisibility(View.GONE);
    }
}