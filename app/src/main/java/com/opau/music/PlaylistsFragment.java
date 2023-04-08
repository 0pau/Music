package com.opau.music;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link PlaylistsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class PlaylistsFragment extends Fragment {

    View v;
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public PlaylistsFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment PlaylistsFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static PlaylistsFragment newInstance(String param1, String param2) {
        PlaylistsFragment fragment = new PlaylistsFragment();
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
        v = inflater.inflate(R.layout.fragment_playlists, container, false);
        getPlaylists();
        return v;
    }

    void getPlaylists() {

        ArrayList<Playlist> playlists = new ArrayList<>();

        playlists.add(new Playlist("Favorites", PlaylistType.FAVORITES));
        ListView lv = v.findViewById(R.id.playlists_list);
        lv.setAdapter(new PlaylistAdapter(getContext(), 0, playlists));

    }

    class PlaylistAdapter extends ArrayAdapter<Playlist> {

        public PlaylistAdapter(@NonNull Context context, int resource, ArrayList<Playlist> lists) {
            super(context, 0, lists);
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {

            Playlist list = getItem(position);

            convertView = getLayoutInflater().inflate(R.layout.artist_item, null, false);

            TextView name = convertView.findViewById(R.id.artist_name);
            name.setText(list.name);

            ImageView iv = convertView.findViewById(R.id.albumArt);

            if (list.type == PlaylistType.FAVORITES) {
                iv.setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.favorites_playlist));
            }

            TextView count = convertView.findViewById(R.id.artist_song_count);

            return convertView;
        }
    }

    class Playlist {
        public String name;
        public PlaylistType type;

        public Playlist(String name, PlaylistType type) {
            this.name = name;
            this.type = type;
        }
    }

    enum PlaylistType {
        REGULAR,        //created by the user
        FAVORITES,      //songs marked as favorite
        MOST_LISTENED   //most listened songs
    }
}