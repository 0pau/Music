package com.opau.music;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.Lifecycle;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.animation.ValueAnimator;
import androidx.fragment.app.FragmentManager;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import android.content.ContentUris;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.media.Image;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.util.Log;
import android.util.Size;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.window.OnBackInvokedDispatcher;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;

public class MainActivity extends AppCompatActivity {
    NowPlayingSwipeListener2 swiper;
    HomeScreenAdapter homeScreenAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        long start = System.currentTimeMillis();

        if (!Permissions.checkPermissions(this)) {
            Intent i = new Intent(this, PermissionAlert.class);
            i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(i);
            finish();
            return;
        }

        Intent svc=new Intent(this, MusicService.class);
        startService(svc);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);

        View loadIndicator = getLayoutInflater().inflate(R.layout.library_load_pb, null);
        loadIndicator.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        ((Toolbar)findViewById(R.id.toolbar2)).addView(loadIndicator);
        ((Toolbar)findViewById(R.id.toolbar2)).setOnMenuItemClickListener((itm)->{
            if (itm.getItemId() == R.id.go_to_settings) {
                Intent i = new Intent(this, Settings.class);
                startActivity(i);
            }
            return true;
        });

        BottomNavigation nav = findViewById(R.id.nav);

        Thread updateRequester = new Thread(){
            @Override
            public void run() {
                super.run();
                int u = ((App)getApplication()).getLibraryManager().updateLibrary();
                finished(u);
            }
            void finished(int updateCount) {
                runOnUiThread(()->{
                    loadIndicator.setVisibility(View.GONE);
                    if (updateCount != 0) {
                        //adapter.refresh();
                    }
                    int songs = ((App)getApplication()).getLibraryManager().getSongCount();
                    ((Toolbar)findViewById(R.id.toolbar2)).setSubtitle(getResources().getQuantityString(R.plurals.song_count, songs, songs, songs));
                });
            }
        };
        updateRequester.start();
        final Handler handler = new Handler(Looper.getMainLooper());
        handler.postDelayed(this::setupPcListener, 100);

        homeScreenAdapter = new HomeScreenAdapter(getSupportFragmentManager(), getLifecycle());
        ViewPager2 homeViewPager = findViewById(R.id.homeViewPager);
        homeViewPager.setAdapter(homeScreenAdapter);
        nav.setOnItemSelectedListener(homeViewPager::setCurrentItem);

        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        PlayerControlsFragment fragment = new PlayerControlsFragment();
        fragmentTransaction.add(R.id.fragmentContainerView, fragment);
        fragmentTransaction.commit();
        setWindowInsets(nav, fragment);

        LinearLayout nowPlayingPanel = findViewById(R.id.nowPlayingPanel);
        swiper = new NowPlayingSwipeListener2(this, nowPlayingPanel, findViewById(R.id.fragmentContainerView), findViewById(R.id.nowPlayingHead), this);
        nowPlayingPanel.setOnTouchListener(swiper);
        long end = System.currentTimeMillis();

        Log.i("Performance", "MainActivity load time was " + (end-start) + " ms");
    }

    void setWindowInsets(BottomNavigation nav, PlayerControlsFragment playerControlsFragment) {
        int navId = getResources().getIdentifier("navigation_bar_height", "dimen", "android");
        int navbarSize = getResources().getDimensionPixelSize(navId);
        nav.setPaddingToScreen(navbarSize);
        playerControlsFragment.navbarPadding = navbarSize;
        int statusBarId = getResources().getIdentifier("status_bar_height", "dimen", "android");
        int statusBarSize = getResources().getDimensionPixelSize(statusBarId);
        findViewById(R.id.toolbarContainer).setPadding(0,statusBarSize,0,0);
        playerControlsFragment.statusBarPadding = statusBarSize;
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

    private class HomeScreenAdapter extends FragmentStateAdapter {

        public HomeScreenAdapter(@NonNull FragmentManager fragmentManager, @NonNull Lifecycle lifecycle) {
            super(fragmentManager, lifecycle);
        }

        @NonNull
        @Override
        public Fragment createFragment(int position) {
            switch (position) {
                case 0:
                    return new SongLibraryFragment();
            }
            return null;
        }

        @Override
        public int getItemCount() {
            return 1;
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