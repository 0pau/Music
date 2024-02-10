package com.opau.music;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Random;

public class SongLibraryFragment extends HomeScreenFragment {

    ArrayList<Entity> entities = new ArrayList<>();
    @Override
    public void onStart() {
        fab.setImageResource(R.drawable.shuffle);
        fab.setOnClickListener((event)->{
            ArrayList<Long> playlist = new ArrayList<>();
            for (Entity e: entities) {
                playlist.add(((SongData)e.getData()).id);
            }
            playlist.sort((o1, o2) -> {
                Random r = new Random();
                return (r.nextBoolean())?1:-1;
            });
            getPlaybackCoordinator().queueList(playlist, 0);
        });
        fab.setVisibility(View.VISIBLE);
        adapter = new SongRecyclerAdapter();
        recyclerView.setAdapter(adapter);
        super.onStart();
        refreshData();
    }

    @Override
    public void refreshData() {
        entities = getLibraryManager().getEntities(Entity.Type.SONG);
        entities.sort(new EntityComparator());
        super.refreshData();
    }


    private class SongRecyclerAdapter extends RecyclerView.Adapter<SongRecyclerAdapter.ViewHolder> {

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int position) {
            View a = LayoutInflater.from(parent.getContext()).inflate(R.layout.song_item, parent, false);
            SongData sd = (SongData) entities.get(position).getData();
            ArtistData ad = (ArtistData) getLibraryManager().getEntityForId(Entity.Type.ARTIST, sd.artistID).getData();
            ((TextView)a.findViewById(R.id.songItemTitle)).setText(sd.title);
            ((TextView)a.findViewById(R.id.songItemSubtitle)).setText(ad.name +" • "+ Utils.formatMsDuration(sd.duration));

            try {
                ((ImageView)a.findViewById(R.id.albumArt)).setImageBitmap(getLibraryManager().getAlbumArtThumbnail(64, sd.albumID));
            } catch (Exception e) {
            }
            a.setTag(position);
            a.setOnClickListener((e)->{
                ArrayList<Long> queue = new ArrayList<>();
                for (Entity en: entities) {
                    queue.add(((SongData)en.getData()).id);
                }
                getPlaybackCoordinator().queueList(queue, (int)a.getTag());
            });

            return new ViewHolder(a);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        }

        @Override
        public int getItemCount() {
            return entities.size();
        }

        @Override
        public int getItemViewType(int position) {
            return position;
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            public ViewHolder(@NonNull View itemView) {
                super(itemView);
            }
        }
    }
}