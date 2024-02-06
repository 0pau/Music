package com.opau.music;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.animation.ValueAnimator;
import androidx.fragment.app.FragmentManager;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.window.OnBackInvokedDispatcher;

import java.util.ArrayList;
import java.util.Random;

public class MainActivity extends AppCompatActivity {

    Entity.Type currentView = Entity.Type.SONG;
    public boolean nowPlayingExpanded = false;
    NowPlayingSwipeListener swiper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (!Permissions.checkPermissions(this)) {
            Intent i = new Intent(this, PermissionAlert.class);
            i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(i);
            finish();
            return;
        }

        Intent svc=new Intent(this, MusicService.class);
        startService(svc);

        View loadIndicator = getLayoutInflater().inflate(R.layout.library_load_pb, null);
        loadIndicator.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        ((Toolbar)findViewById(R.id.toolbar2)).addView(loadIndicator);

        RecyclerView rc = findViewById(R.id.contentRecycler);
        rc.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        RecyclerViewAdapter adapter = new RecyclerViewAdapter();
        rc.setAdapter(adapter);

        BottomNavigation nav = findViewById(R.id.nav);
        nav.setOnItemSelectedListener((itm)->{
            currentView = Entity.Type.values()[itm];
            adapter.refresh();
        });

        ((App)getApplication()).getLibraryManager().updateLibrary();
        loadIndicator.setVisibility(View.GONE);
        adapter.refresh();
        int songs = ((App)getApplication()).getLibraryManager().getSongCount();
        ((Toolbar)findViewById(R.id.toolbar2)).setSubtitle(getResources().getQuantityString(R.plurals.song_count, songs, songs, songs));
        final Handler handler = new Handler(Looper.getMainLooper());
        handler.postDelayed(this::setupPcListener, 100);

        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        PlayerControlsFragment fragment = new PlayerControlsFragment();
        fragmentTransaction.add(R.id.fragmentContainerView, fragment);
        fragmentTransaction.commit();

        LinearLayout nowPlayingPanel = findViewById(R.id.nowPlayingPanel);
        swiper = new NowPlayingSwipeListener(this, nowPlayingPanel, findViewById(R.id.fragmentContainerView), findViewById(R.id.nowPlayingHead), this);
        nowPlayingPanel.setOnTouchListener(swiper);
    }

    PlaybackCoordinator getPlaybackCoordinator() {
        return ((App)getApplication()).getPlaybackCoordinator();
    }

    void setupPcListener() {
        getPlaybackCoordinator().addEventListener(new PlaybackCoordinatorEventListener() {
            @Override
            public void onTrackStarted(long track_id) {
                updateSongInfo();
                showNowPlayingPanel();
            }

            @Override
            public void onPlayStateChanged(boolean isPlaying) {
                ImageView nowPlayingPlayPauseBtn = findViewById(R.id.nowPlayingPlayPauseBtn);
                int pp_drawable_id = (getPlaybackCoordinator().isPlaying())?R.drawable.pause:R.drawable.play_arrow;
                nowPlayingPlayPauseBtn.setImageResource(pp_drawable_id);
            }
        });
    }

    public void pp(View v) {
        if (getPlaybackCoordinator().isPlaying()) {
            getPlaybackCoordinator().pause();
        } else {
            getPlaybackCoordinator().play();
        }
    }

    public void shufflePlay(View v) {
        ArrayList<Entity> entities = ((App)getApplication()).getLibraryManager().getSongs();
        ArrayList<Long> playlist = new ArrayList<>();
        for (Entity e: entities) {
            playlist.add(((SongData)e.getData()).id);
        }
        playlist.sort((o1, o2) -> {
            Random r = new Random();
            return (r.nextBoolean())?1:-1;
        });
        getPlaybackCoordinator().queueList(playlist, 0);
    }

    public void updateSongInfo() {
        SongData info = getPlaybackCoordinator().getCurrentlyPlaying();
        runOnUiThread(()->{
            LinearLayout nowPlayingDisplay = findViewById(R.id.nowPlayingDisplay);
            nowPlayingDisplay.animate().setDuration(200).alpha(0f).translationX(-100).setInterpolator(Interpolators.playbackPaneEaseIn).start();
            Handler h = new Handler(Looper.getMainLooper());
            h.postDelayed(()->{
                nowPlayingDisplay.setTranslationX(100);
                nowPlayingDisplay.animate().setDuration(200).setStartDelay(10).alpha(1f).translationX(0).setInterpolator(Interpolators.playbackPaneEaseOut).start();
                TextView tv = findViewById(R.id.nowPlayingTitle);
                tv.setText(info.title);
            },220);

        });
    }

    public void showNowPlayingPanel() {
        LinearLayout panel = findViewById(R.id.nowPlayingPanel);
        if (panel.getVisibility() != View.VISIBLE) {
            panel.measure(LinearLayout.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            int panelHeight = panel.getMeasuredHeight();
            panel.setTranslationY(panelHeight);
            panel.setVisibility(View.VISIBLE);
            panel.animate().setDuration(350).translationY(0).setInterpolator(Interpolators.easeOut).start();
            FrameLayout contentFrame = findViewById(R.id.contentFrame);
            ValueAnimator paddingAnim = ValueAnimator.ofInt(0, panelHeight);
            paddingAnim.addUpdateListener((va) -> {
                contentFrame.setPadding(0, contentFrame.getPaddingTop(), 0, (int) va.getAnimatedValue());
            });
            paddingAnim.setDuration(350).setInterpolator(Interpolators.easeOut);
            paddingAnim.start();
        }
    }

    class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.SongViewHolder> {

        int itmCount = 0;
        ArrayList<Entity> entities = new ArrayList<>();
        @NonNull
        @Override
        public SongViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.song_item, parent, false);
            return new SongViewHolder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull SongViewHolder holder, int position) {
            View v = holder.itemView;

            switch (entities.get(position).getEntityType()) {
                case SONG:
                    SongData sd = (SongData) entities.get(position).getData();
                    ArtistData ad = (ArtistData) ((App)getApplication()).getLibraryManager().getEntityForId(Entity.Type.ARTIST, sd.artistID).getData();
                    ((TextView)v.findViewById(R.id.songItemTitle)).setText(sd.title);
                    ((TextView)v.findViewById(R.id.songItemSubtitle)).setText(ad.name);
                    v.setTag(position);
                    v.setOnClickListener((e)->{
                        ArrayList<Long> queue = new ArrayList<>();
                        for (Entity en: entities) {
                            queue.add(((SongData)en.getData()).id);
                        }
                        getPlaybackCoordinator().queueList(queue, (int)v.getTag());
                    });
                    break;
            }
        }

        public void refresh() {
            switch (currentView) {
                case SONG:
                    entities = ((App)getApplication()).getLibraryManager().getSongs();
                    break;
                default:
                    entities = new ArrayList<>();
                    break;
            }
            sort();
            notifyDataSetChanged();
        }

        public void sort() {
            entities.sort(new EntityComparator());
        }

        @Override
        public int getItemCount() {
            return entities.size();
        }

        public class SongViewHolder extends RecyclerView.ViewHolder {

            public SongViewHolder(@NonNull View itemView) {
                super(itemView);
            }
        }
    }

    @Override
    protected void onDestroy() {
        Intent service = new Intent(MainActivity.this, MusicService.class);
        stopService(service);
        super.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public void onBackPressed() {
        if (!swiper.expanded) {
            super.onBackPressed();
        } else {
            swiper.collapse();
        }
    }

}