package com.opau.music;

import android.annotation.SuppressLint;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Locale;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ArtistsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ArtistsFragment extends Fragment {

    View v;

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public ArtistsFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment ArtistsFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static ArtistsFragment newInstance(String param1, String param2) {
        ArtistsFragment fragment = new ArtistsFragment();
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

        v = inflater.inflate(R.layout.fragment_artists, container, false);
        loadArtists();
        return v;
    }

    void loadArtists() {

        String[] projection = {MediaStore.Audio.Media.ARTIST, MediaStore.Audio.Media._ID};
        Uri musicUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        Cursor musicCursor = getContext().getContentResolver().query(musicUri, projection, MediaStore.Audio.Media.IS_MUSIC + " != 0", null, null);
        ArrayList<Artist> artists = new ArrayList<>();
        ArrayList<String> groupby = new ArrayList<>();

        if (musicCursor != null && musicCursor.moveToFirst()) {
            // iterate through the results
            do {

                @SuppressLint("Range") String name = musicCursor.getString(musicCursor.getColumnIndex(MediaStore.Audio.Media.ARTIST));
                if (!groupby.contains(name)) {
                    groupby.add(name);
                }

            } while (musicCursor.moveToNext());
        }

        Collator c = Collator.getInstance(Locale.getDefault());
        c.setStrength(Collator.IDENTICAL);
        Collections.sort(groupby, c);

        for (int i = 0; i < groupby.size(); i++) {

            String name = groupby.get(i);
            int songs = 0;

            musicCursor = getContext().getContentResolver().query(musicUri, projection, MediaStore.Audio.Media.IS_MUSIC + " != 0", null, null);
            if (musicCursor != null && musicCursor.moveToFirst()) {
                // iterate through the results
                do {
                    @SuppressLint("Range") String song_artist = musicCursor.getString(musicCursor.getColumnIndex(MediaStore.Audio.Media.ARTIST));
                    if (song_artist.equals(name)) {
                        songs++;
                    }
                } while (musicCursor.moveToNext());
                musicCursor.close();
            }

            Artist a = new Artist(songs, name);
            artists.add(a);

        }

        ListView lv = (ListView) v.findViewById(R.id.artists_list);
        lv.setAdapter(new ArtistAdapter(getContext(), 0, artists));

    }

    class ArtistAdapter extends ArrayAdapter<Artist> {

        public ArtistAdapter(@NonNull Context context, int resource, ArrayList<Artist> artists) {
            super(context, 0, artists);
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {

            Artist artist = getItem(position);

            convertView = getLayoutInflater().inflate(R.layout.artist_item, null, false);

            TextView name = convertView.findViewById(R.id.artist_name);
            name.setText(artist.name);
            TextView count = convertView.findViewById(R.id.artist_song_count);
            count.setText(getResources().getQuantityString(R.plurals.song_count, artist.songs, artist.songs));

            return convertView;
        }
    }

    class Artist {
        public int songs;
        public String name;

        public Artist(int songs, String name) {
            this.songs = songs;
            this.name = name;
        }
    }
}