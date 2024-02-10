package com.opau.music;

import android.content.Context;
import android.media.MediaRouter;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetDialog;

import java.util.ArrayList;
import java.util.Collections;

public class PlaylistDialog extends CustomBottomSheetDialog {
    PlaybackCoordinator coordinator;
    public PlaylistDialog(@NonNull Context context, PlaybackCoordinator coordinator) {
        super(context);
        this.coordinator = coordinator;
        context.setTheme(R.style.Theme_Music_BottomSheetDialog);
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.playlist_bottom_sheet);
        RecyclerView rv = findViewById(R.id.playlistRecycler);

        PlaylistAdapter adapter = new PlaylistAdapter(getLayoutInflater());
        rv.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
        rv.setAdapter(adapter);
        rv.scrollToPosition(coordinator.getCurrentIndex());

        ItemTouchHelper.SimpleCallback touchCallback = new ItemTouchHelper.SimpleCallback(ItemTouchHelper.UP|ItemTouchHelper.DOWN, ItemTouchHelper.LEFT|ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                coordinator.swap(viewHolder.getAdapterPosition(), target.getAdapterPosition());
                adapter.notifyItemMoved(viewHolder.getAdapterPosition(), target.getAdapterPosition());
                return true;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                coordinator.removeAt(viewHolder.getAdapterPosition());
                adapter.notifyItemRemoved(viewHolder.getAdapterPosition());
            }

            };
        ItemTouchHelper touchHelper = new ItemTouchHelper(touchCallback);
        touchHelper.attachToRecyclerView(rv);

        coordinator.addEventListener(new PlaybackCoordinatorEventListener(){
            @Override
            void onTrackStarted(long track_id) {
                super.onTrackStarted(track_id);
                adapter.notifyDataSetChanged();
            }
        });
    }

    class PlaylistAdapter extends RecyclerView.Adapter<PlaylistAdapter.ViewHolder> {

        LayoutInflater inf;
        public PlaylistAdapter(LayoutInflater i) {
            inf = i;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = inf.inflate(R.layout.song_item, parent, false);
            ViewHolder vh = new ViewHolder(v);
            v.setOnClickListener((e)->{
                coordinator.jumpTo((int)v.getTag());
                //dismiss();
            });
            return vh;
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            holder.itemView.setTag(position);
            if (position < coordinator.getCurrentIndex()) {
                holder.itemView.setAlpha(0.5f);
            } else {
                holder.itemView.setAlpha(1f);
            }
            if (position == coordinator.getCurrentIndex()) {
                holder.itemView.findViewById(R.id.playingIndicator).setVisibility(View.VISIBLE);
            } else {
                holder.itemView.findViewById(R.id.playingIndicator).setVisibility(View.GONE);
            }

            SongData sd = (SongData) coordinator.libraryManager.getEntityForId(Entity.Type.SONG, coordinator.getCurrentPlaylist().get(position)).getData();
            ((TextView)holder.itemView.findViewById(R.id.songItemTitle)).setText(sd.title);
            ((TextView)holder.itemView.findViewById(R.id.songItemSubtitle)).setText(coordinator.libraryManager.getArtistNameForSongId(coordinator.getCurrentPlaylist().get(position)));
        }

        @Override
        public int getItemCount() {
            return coordinator.getPlaylistLength();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {

            public ViewHolder(@NonNull View itemView) {
                super(itemView);
            }
        }
    }

    public int getDrawableForDeviceType(int deviceType) {

        switch (deviceType) {
            case MediaRouter.RouteInfo.DEVICE_TYPE_SPEAKER:
                return R.drawable.speaker;
            case MediaRouter.RouteInfo.DEVICE_TYPE_BLUETOOTH:
                return R.drawable.bluetooth;
            case MediaRouter.RouteInfo.DEVICE_TYPE_TV:
                return R.drawable.tv;
        }

        return R.drawable.cast;
    }

}
