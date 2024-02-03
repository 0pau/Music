package com.opau.music;

import android.annotation.SuppressLint;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.ImageDecoder;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import java.io.IOException;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Locale;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link SongsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SongsFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    View v;

    public SongsFragment() {
        // Required empty public constructor
    }

    public static SongsFragment newInstance(String param1, String param2) {
        SongsFragment fragment = new SongsFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        v = inflater.inflate(R.layout.fragment_songs, container, false);

        getSongList();
        return v;
    }

    void getSongList() {
        /*
        //lv.setAdapter();

        String[] projection = {MediaStore.Audio.Media._ID, MediaStore.Audio.Media.TITLE, MediaStore.Audio.Media.ARTIST, MediaStore.Audio.Media.ALBUM_ID, MediaStore.Audio.Media.ALBUM, MediaStore.Audio.Media.DURATION};
        Uri musicUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        Cursor musicCursor = getContext().getContentResolver().query(musicUri, projection, MediaStore.Audio.Media.IS_MUSIC + " != 0", null, "TITLE ASC");
        ArrayList<Song> songs = new ArrayList<>();

        Uri artworkUri = Uri.parse("content://media/external/audio/albumart");
        if (musicCursor != null && musicCursor.moveToFirst()) {
            // iterate through the results
            do {
                @SuppressLint("Range") long id = musicCursor.getLong(musicCursor.getColumnIndex(MediaStore.Audio.Media._ID));
                @SuppressLint("Range") long album_id = musicCursor.getLong(musicCursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID));
                @SuppressLint("Range") String title = musicCursor.getString(musicCursor.getColumnIndex(MediaStore.Audio.Media.TITLE));
                @SuppressLint("Range") String artist = musicCursor.getString(musicCursor.getColumnIndex(MediaStore.Audio.Media.ARTIST));
                @SuppressLint("Range") String album = musicCursor.getString(musicCursor.getColumnIndex(MediaStore.Audio.Media.ALBUM));
                @SuppressLint("Range") String duration = musicCursor.getString(musicCursor.getColumnIndex(MediaStore.Audio.Media.DURATION));

                Uri art_path = ContentUris.withAppendedId(artworkUri, album_id);

                Song s = new Song(id, title, album, artist, art_path, duration);
                songs.add(s);

            } while (musicCursor.moveToNext());
            musicCursor.close();
        }

        Collator c = Collator.getInstance(Locale.getDefault());
        c.setStrength(Collator.IDENTICAL);

        songs.sort(new SongComparator());

        ListView lv = (ListView) v.findViewById(R.id.songlist);
        lv.setAdapter(new SongAdapter(getContext(), 0, songs));

        TextView sc = v.findViewById(R.id.songCount);
        sc.setText(getResources().getQuantityString(R.plurals.song_count, songs.size(), songs.size()));
        */

        RecyclerView rc = (RecyclerView) v.findViewById(R.id.songlist);
        rc.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
        rc.setAdapter(new RecyclerViewAdapter());
    }

    class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.SongViewHolder> {
        @NonNull
        @Override
        public SongViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.song_item, parent, false);
            return new SongViewHolder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull SongViewHolder holder, int position) {

        }

        @Override
        public int getItemCount() {
            return 22;
        }

        public class SongViewHolder extends RecyclerView.ViewHolder {

            public SongViewHolder(@NonNull View itemView) {
                super(itemView);
            }
        }
    }

    String formatMsDuration(String duration) {
        int d = Integer.parseInt(duration);

        int all_sec = Math.round(d / 1000);
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
}