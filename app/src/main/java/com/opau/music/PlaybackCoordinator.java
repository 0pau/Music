package com.opau.music;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.OptIn;
import androidx.media3.common.Format;
import androidx.media3.common.MediaItem;
import androidx.media3.common.MediaMetadata;
import androidx.media3.common.PlaybackException;
import androidx.media3.common.Player;
import androidx.media3.common.Tracks;
import androidx.media3.common.util.UnstableApi;
import androidx.media3.exoplayer.ExoPlayer;

import java.util.ArrayList;
import java.util.Collections;

public class PlaybackCoordinator {
    private int position = -1;
    private ArrayList<Long> queue = new ArrayList<>();
    Player player;
    Context context;
    LibraryManager libraryManager;
    private SoundFormatInfo currentSongFormatInfo;
    private boolean changingTrack = false;
    private boolean canPlayPrevious = false;
    private boolean canPlayNext = false;
    ArrayList<PlaybackCoordinatorEventListener> listeners = new ArrayList<>();

    public PlaybackCoordinator(Context c, LibraryManager lm) {
        context = c;
        libraryManager = lm;
        player = new ExoPlayer.Builder(context).build();
    }

    private void configurePlayer() {
        player.stop();
        player.release();
        player = null;
        player = new ExoPlayer.Builder(context).build();
        player.addListener(new Player.Listener() {
            @Override
            public void onPlaybackStateChanged(int playbackState) {
                Player.Listener.super.onPlaybackStateChanged(playbackState);
                if (playbackState == ExoPlayer.STATE_ENDED) {
                    tryToGoNext();
                }
            }

            @Override
            public void onTracksChanged(Tracks tracks) {
                //Log.v("Tracks", tracks.getGroups().get(0).getTrackFormat(0).sampleMimeType);
                Player.Listener.super.onTracksChanged(tracks);
            }

            @OptIn(markerClass = UnstableApi.class) @Override
            public void onIsPlayingChanged(boolean isPlaying) {
                Player.Listener.super.onIsPlayingChanged(isPlaying);
                if (isPlaying) {
                    Tracks tracks = player.getCurrentTracks();
                    Format fmt = tracks.getGroups().get(0).getTrackFormat(0);
                    currentSongFormatInfo = new SoundFormatInfo(fmt.sampleMimeType, fmt.sampleRate,0);
                }
                for (PlaybackCoordinatorEventListener listener: listeners) {
                    listener.onPlayStateChanged(isPlaying);
                }
            }
        });
    }

    private void tryToGoNext() {
        if (canPlayNext) {
            playNext(1);
        } else {
            playlistFinish();
        }
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
        configurePlayer();
        changingTrack = true;
        position += direction;
        if (position > queue.size()) {
            return;
        }
        Entity next = libraryManager.getEntityForId(Entity.Type.SONG, queue.get(position));
        String path = ((SongData)next.getData()).path;
        MediaItem mi = MediaItem.fromUri(path);
        player.setMediaItem(mi);
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
    public SongData getNext() {
        if (!canPlayNext) {
            return null;
        }
        Entity song = libraryManager.getEntityForId(Entity.Type.SONG, queue.get(position+1));
        return ((SongData) song.getData());
    }

    public void jumpTo(int index) {
        position = index-1;
        playNext(1);
    }

    public ArrayList<Long> getCurrentPlaylist() {
        return queue;
    }

    public int getCurrentIndex() {
        return position;
    }

    public long getCurrentId() {
        return queue.get(position);
    }

    public void removeAt(int index) {
        queue.remove(index);
        if (index == position) {
            position--;
            tryToGoNext();
        }
    }

    public int getPlaylistLength() {
        return queue.size();
    }

    public void swap(int p1, int p2) {
        Collections.swap(queue, p1, p2);
    }

    public SoundFormatInfo getCurrentSongFormatInfo() {
        return currentSongFormatInfo;
    }
}
