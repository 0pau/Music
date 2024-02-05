package com.opau.music;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import androidx.media3.common.MediaItem;
import androidx.media3.common.PlaybackException;
import androidx.media3.common.Player;
import androidx.media3.exoplayer.ExoPlayer;

import java.util.ArrayList;

public class PlaybackCoordinator {
    private int position = -1;
    private ArrayList<Long> queue = new ArrayList<>();
    Player player;
    Context context;
    LibraryManager libraryManager;
    private boolean changingTrack = false;
    private boolean canPlayPrevious = false;
    private boolean canPlayNext = false;
    ArrayList<PlaybackCoordinatorEventListener> listeners = new ArrayList<>();

    public PlaybackCoordinator(Context c, LibraryManager lm) {
        context = c;
        player = new ExoPlayer.Builder(context).build();
        libraryManager = lm;
        player.addListener(new Player.Listener() {

            @Override
            public void onPlaybackStateChanged(int playbackState) {
                Player.Listener.super.onPlaybackStateChanged(playbackState);
                if (playbackState == ExoPlayer.STATE_ENDED) {
                    if (canPlayNext) {
                        playNext(1);
                    } else {
                        playlistFinish();
                    }
                }
            }

            @Override
            public void onIsPlayingChanged(boolean isPlaying) {
                Player.Listener.super.onIsPlayingChanged(isPlaying);
                for (PlaybackCoordinatorEventListener listener: listeners) {
                    listener.onPlayStateChanged(isPlaying);
                }
            }
        });
    }

    private void playlistFinish() {
        position = -1;
        for (PlaybackCoordinatorEventListener listener: listeners) {
            listener.onPlayListFinished();
        }
    }

    public void pause() {
        player.pause();
    }

    public void play() {
        if (position == -1) {
            startPlayback();
        } else {
            player.play();
        }
    }

    public void queueList(ArrayList<Long> items, int playFrom) {
        queue.clear();
        queue.addAll(items);
        position = playFrom-1;
        startPlayback();
    }

    public void startPlayback() {
        playNext(1);
    }

    public void playNext(int direction) {
        changingTrack = true;
        position += direction;
        if (position > queue.size()) {
            return;
        }
        Entity next = libraryManager.getEntityForId(Entity.Type.SONG, queue.get(position));
        String path = ((SongData)next.getData()).path;
        player.setMediaItem(MediaItem.fromUri(path));
        player.prepare();
        playTrack();
    }

    public void seekTo(long pos) {
        player.seekTo(pos);
    }

    private void playTrack() {
        canPlayPrevious = (position!=0);
        canPlayNext = (position<queue.size()-1);
        player.play();
        for (PlaybackCoordinatorEventListener l: listeners) {
            l.onTrackStarted(queue.get(position));
        }
        changingTrack = false;
    }

    public long getPos() {
        return player.getCurrentPosition();
    }

    public void addEventListener(PlaybackCoordinatorEventListener l) {
        listeners.add(l);
    }

    public interface PlaybackCoordinatorEventListener {
        void onTrackStarted(long track_id);
        void onPlayBackError();
        void onPlayListFinished();
        void onPlayStateChanged(boolean isPlaying);
    }

    public boolean pcCanPlayNext() {
        return canPlayNext;
    }

    public boolean pcCanPlayPrevious() {
        return canPlayPrevious;
    }

    public boolean isPlaying() {
        return player.isPlaying();
    }

    public SongData getCurrentlyPlaying() {
        Entity song = libraryManager.getEntityForId(Entity.Type.SONG, queue.get(position));
        return ((SongData) song.getData());
    }
}
