package com.opau.music;

import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaRouter;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

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
                @Override
                public void onTrackStarted(long track_id) {
                    LibraryManager lm = ((App)getActivity().getApplication()).getLibraryManager();
                    SongData sd = (SongData) lm.getEntityForId(Entity.Type.SONG, track_id).getData();
                    ((TextView)v.findViewById(R.id.playerFragmentTitle)).setText(sd.title);
                    ((TextView)v.findViewById(R.id.playerFragmentSubtitle)).setText(lm.getArtistNameForSongId(track_id));
                    ((TextView)v.findViewById(R.id.nowPlayingTrackTotal)).setText(Utils.formatMsDuration(sd.duration));
                    ((SeekBar)v.findViewById(R.id.seekBar)).setMax((int)sd.duration);
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
        /*
        MediaRouter mr = (MediaRouter)getActivity().getSystemService(Context.MEDIA_ROUTER_SERVICE);
        //Log.i("mediaRoute", (String) mr.getSelectedRoute(MediaRouter.ROUTE_TYPE_LIVE_AUDIO).getName());
        for (int i = 0; i < mr.getRouteCount(); i++) {
            Log.i("mediaRoute", (String) mr.getRouteAt(i).getName());
        }

        MediaRouteSelector mediaRouteSelector = new MediaRouteSelector.Builder()
                .addControlCategory(MediaControlIntent.CATEGORY_LIVE_AUDIO).build();

        MediaRouteControllerDialog dialog = new MediaRouteControllerDialog(getContext());
        dialog.show();*/

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        v = inflater.inflate(R.layout.fragment_player_controls, container, false);
        v.findViewById(R.id.playerFragmentPrevious).setOnClickListener(this::skipPrevious);
        v.findViewById(R.id.playerFragmentNext).setOnClickListener(this::skipNext);
        v.findViewById(R.id.mediaRouterButton).setOnClickListener(this::showMediaRouter);
        return v;
    }
}