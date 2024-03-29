package com.opau.music;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Mesh;
import android.graphics.RenderEffect;
import android.graphics.Shader;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.TransitionDrawable;
import android.media.AudioManager;
import android.media.MediaRouter;
import android.os.Bundle;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.google.android.material.color.DynamicColors;
import com.google.android.material.color.DynamicColorsOptions;

import java.io.IOException;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link PlayerControlsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class PlayerControlsFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    private View v;
    private boolean needsUpdate = false;
    public int navbarPadding = 0;
    public int statusBarPadding = 0;

    public PlayerControlsFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment PlayerControlsFragment.
     */
    // TODO: Rename and change types and number of parameters
    TransitionDrawable transition;
    TransitionDrawable transition2;
    public static PlayerControlsFragment newInstance(String param1, String param2) {
        PlayerControlsFragment fragment = new PlayerControlsFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    PlaybackCoordinator getPlaybackCoordinator() {
        return ((App)getActivity().getApplication()).getPlaybackCoordinator();
    }

    LibraryManager getLibraryManager() {
        return ((App)getActivity().getApplication()).getLibraryManager();
    }

    void update() {
        Handler h = new Handler(Looper.getMainLooper());
        h.postDelayed(()->{
            if (needsUpdate) {
                ((TextView)v.findViewById(R.id.nowPlayingTrackCurrentPos)).setText(Utils.formatMsDuration(getPlaybackCoordinator().getPos()));
                ((SeekBar)v.findViewById(R.id.seekBar)).setProgress((int)getPlaybackCoordinator().getPos());
                update();
            }
        }, 500);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }

        Handler delay = new Handler(Looper.getMainLooper());
        delay.postDelayed(()->{
            getPlaybackCoordinator().addEventListener(new PlaybackCoordinatorEventListener() {
                @SuppressLint("RestrictedApi")
                @Override
                public void onTrackStarted(long track_id) {
                    LibraryManager lm = ((App)getActivity().getApplication()).getLibraryManager();
                    SongData sd = (SongData) lm.getEntityForId(Entity.Type.SONG, track_id).getData();
                    ((TextView)v.findViewById(R.id.playerFragmentTitle)).setText(sd.title);
                    ((TextView)v.findViewById(R.id.playerFragmentSubtitle)).setText(lm.getArtistNameForSongId(track_id));
                    ((TextView)v.findViewById(R.id.nowPlayingTrackTotal)).setText(Utils.formatMsDuration(sd.duration));
                    ((SeekBar)v.findViewById(R.id.seekBar)).setMax((int)sd.duration);

                    SongData sdNext = getPlaybackCoordinator().getNext();
                    if (getPlaybackCoordinator().pcCanPlayNext()) {
                        ((TextView)v.findViewById(R.id.nextSongTitle)).setText(sdNext.title);
                        //((TextView)v.findViewById(R.id.nextSongArtist)).setText(lm.getArtistNameForSongId(sdNext.id));
                    } else {
                        sdNext = new SongData();
                        ((TextView)v.findViewById(R.id.nextSongTitle)).setText("This is the last song");
                        //((TextView)v.findViewById(R.id.nextSongArtist)).setText("Tap here to see the playlist");
                    }

                    Bitmap artBitmap = null;
                    try {
                        artBitmap = getLibraryManager().getAlbumArt(sd.albumID);
                    } catch (IOException e) {}

                    changeCovers(artBitmap);

                }

                @Override
                public void onPlayStateChanged(boolean isPlaying) {
                    ImageView pp = v.findViewById(R.id.playerFragmentPlayPause);
                    pp.setImageResource((getPlaybackCoordinator().isPlaying())?R.drawable.pause:R.drawable.play_arrow);
                    ImageView playerFragmentNext = v.findViewById(R.id.playerFragmentNext);
                    ImageView playerFragmentPrev = v.findViewById(R.id.playerFragmentPrevious);
                    playerFragmentNext.setEnabled(getPlaybackCoordinator().pcCanPlayNext());
                    playerFragmentPrev.setEnabled(getPlaybackCoordinator().pcCanPlayPrevious());
                    needsUpdate = isPlaying;
                    update();
                    if (getPlaybackCoordinator().getCurrentSongFormatInfo().getMimeType().contains("FLAC")) {
                        v.findViewById(R.id.losslessCard).animate().alpha(0.65f).setDuration(250).start();
                        SoundFormatInfo format = getPlaybackCoordinator().getCurrentSongFormatInfo();
                        String desc = String.format("%s %.1f kHz", format.getMimeType(), (double)format.getSampleRate()/1000);
                        ((TextView)v.findViewById(R.id.formatDescription)).setText(desc);
                    } else {
                        v.findViewById(R.id.losslessCard).animate().alpha(0).setDuration(250).start();
                    }
                }
            });
            ((SeekBar)v.findViewById(R.id.seekBar)).setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {

                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {
                    getPlaybackCoordinator().seekTo((long)seekBar.getProgress());
                }
            });
        },100);
    }

    void changeCovers(Bitmap bmp) {
        ImageView art = v.findViewById(R.id.playerFragmentArt);
        ImageView im = v.findViewById(R.id.backdrop);

        Drawable d2 = null;
        if (bmp == null) {
            //d2 = getResources().getDrawable(R.drawable.launcher_bg, getActivity().getTheme());
            im.setImageResource(R.drawable.launcher_bg);
            art.setImageResource(R.drawable.unknown);
            return;
        } else {
            d2 = new BitmapDrawable(getResources(), bmp);
        }

        Drawable[] images = new Drawable[]{im.getDrawable(), d2};
        transition = new TransitionDrawable(images);
        transition2 = new TransitionDrawable(images);
        im.setImageDrawable(transition);
        art.setImageDrawable(transition2);
        transition.startTransition(500);
        transition2.startTransition(500);

    }

    public void pp(View a) {
        if (getPlaybackCoordinator().isPlaying()) {
            getPlaybackCoordinator().pause();
        } else {
            getPlaybackCoordinator().play();
        }
    }

    public void skipNext(View a) {
        getPlaybackCoordinator().playNext(1);
    }

    public void skipPrevious(View a) {
        getPlaybackCoordinator().playNext(-1);
    }

    public void showMediaRouter(View v) {
        MediaRouterDialog dialog = new MediaRouterDialog(getActivity());
        dialog.show();

    }

    public void showPlaylistSheet(View v) {
        PlaylistDialog pd = new PlaylistDialog(getActivity(), getPlaybackCoordinator());
        pd.show();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        v = inflater.inflate(R.layout.fragment_player_controls, container, false);
        ImageView backdrop = v.findViewById(R.id.backdrop);
        v.findViewById(R.id.playerFragmentPrevious).setOnClickListener(this::skipPrevious);
        v.findViewById(R.id.playerFragmentNext).setOnClickListener(this::skipNext);
        v.findViewById(R.id.mediaRouterButton).setOnClickListener(this::showMediaRouter);
        v.findViewById(R.id.nextCard).setOnClickListener(this::showPlaylistSheet);
        RenderEffect re = RenderEffect.createBlurEffect(200f,200f, Shader.TileMode.DECAL);
        backdrop.setRenderEffect(re);
        backdrop.measure(View.MeasureSpec.EXACTLY, View.MeasureSpec.EXACTLY);
        backdrop.setMinimumHeight(backdrop.getMeasuredHeight());
        v.findViewById(R.id.swipeHandle).setPadding(0, statusBarPadding, 0,0);
        v.findViewById(R.id.fragmentFrame).setPadding(0,0,0,navbarPadding);
        return v;
    }
}