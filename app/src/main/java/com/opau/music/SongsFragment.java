package com.opau.music;

import android.annotation.SuppressLint;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageDecoder;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

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

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment SongsFragment.
     */
    // TODO: Rename and change types and number of parameters
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

        ListView lv = (ListView) v.findViewById(R.id.songlist);
        lv.setAdapter(new SongAdapter(getContext(), 0, songs));

        TextView sc = v.findViewById(R.id.songCount);
        sc.setText(getResources().getQuantityString(R.plurals.song_count, songs.size(), songs.size()));
    }

    class Song {
        public long id;
        public String title;
        public String artist;
        public String album;
        public String duration;
        public Uri albumart;

        public Song(long id, String title, String album, String artist, Uri art, String duration) {
            this.id = id;
            this.title = title;
            this.artist = artist;
            this.albumart = art;
            this.album = album;
            this.duration = duration;
        }
    }

    class SongAdapter extends ArrayAdapter<Song> {

        public SongAdapter(@NonNull Context context, int resource, ArrayList<Song> songs) {
            super(context, 0, songs);
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {

            Song song = getItem(position);
            convertView = getLayoutInflater().inflate(R.layout.song_item, parent, false);

            TextView title = convertView.findViewById(R.id.song_title);
            title.setText(song.title);

            String sub = song.artist + " • " + song.album + " • " + formatMsDuration(song.duration);

            TextView artist = convertView.findViewById(R.id.song_artist);
            artist.setText(sub);

            ImageView art = convertView.findViewById(R.id.albumArt);
            if (song.albumart != null) {
                ImageDecoder.Source source = ImageDecoder.createSource(getContext().getContentResolver(), song.albumart);
                Bitmap bmp = null;
                try {
                    bmp = ImageDecoder.decodeBitmap(source);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                if (bmp != null) {
                    art.setImageBitmap(bmp);
                }

            }

            return convertView;
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