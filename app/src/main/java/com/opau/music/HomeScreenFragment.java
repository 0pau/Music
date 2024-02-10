package com.opau.music;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class HomeScreenFragment extends Fragment {

    public View v;
    public RecyclerView recyclerView;
    public RecyclerView.Adapter adapter;
    public FloatingActionButton fab;

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private String mParam1;
    private String mParam2;
    public static SongLibraryFragment newInstance(String param1, String param2) {
        SongLibraryFragment fragment = new SongLibraryFragment();
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

    LibraryManager getLibraryManager() {
        return ((App)getActivity().getApplication()).getLibraryManager();
    }

    PlaybackCoordinator getPlaybackCoordinator() {
        return ((App)getActivity().getApplication()).getPlaybackCoordinator();
    }

    public void refreshData() {
        adapter.notifyDataSetChanged();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        v = inflater.inflate(R.layout.recycler_layout, container, false);
        recyclerView = v.findViewById(R.id.recycler);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
        fab = v.findViewById(R.id.fab);
        return v;
    }
}
