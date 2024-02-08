package com.opau.music;

import static androidx.media3.common.Player.*;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadata;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.IBinder;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.OptIn;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.media3.common.MediaItem;
import androidx.media3.common.Player;
import androidx.media3.common.util.UnstableApi;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.media3.session.MediaSession;
import androidx.media3.session.MediaSessionService;

import java.io.IOException;

public class MusicService extends Service {
    MediaSessionCompat ms;
    LibraryManager libraryManager;
    PlaybackCoordinator pc;

    @Override
    public void onCreate() {
        super.onCreate();
        ms = new MediaSessionCompat(this, "MusicPlayerSession");
        libraryManager = ((App)getApplication()).getLibraryManager();

        pc = new PlaybackCoordinator(this, libraryManager);
        ((App)getApplication()).setPlaybackCoordinator(pc);
        pc.addEventListener(new PlaybackCoordinatorEventListener() {
            @Override
            public void onTrackStarted(long track_id) {
                updatePlayerMetadata();
            }

            @Override
            public void onPlayListFinished() {
                ms.setActive(false);
                updatePlayerState(PlaybackStateCompat.STATE_NONE);
            }

            @Override
            public void onPlayStateChanged(boolean isPlaying) {
                if (isPlaying) {
                    updatePlayerState(PlaybackStateCompat.STATE_PLAYING);
                } else {
                    updatePlayerState(PlaybackStateCompat.STATE_PAUSED);
                }
            }
        });
        ms.setCallback(new MediaSessionCompat.Callback() {
            @Override
            public void onPlay() {
                super.onPlay();
                pc.play();
                updatePlayerState(PlaybackStateCompat.STATE_PLAYING);
            }

            @Override
            public void onPause() {
                super.onPause();
                pc.pause();
                updatePlayerState(PlaybackStateCompat.STATE_PAUSED);
            }

            @Override
            public void onSkipToNext() {
                super.onSkipToNext();
                pc.playNext(1);
            }

            @Override
            public void onSkipToPrevious() {
                super.onSkipToPrevious();
                pc.playNext(-1);
            }

            @Override
            public void onSeekTo(long pos) {
                super.onSeekTo(pos);
                pc.seekTo(pos);
            }
        });
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);

        return START_STICKY;
    }

    void updatePlayerState(int state) {
        PlaybackStateCompat.Builder stateBuilder = new PlaybackStateCompat.Builder();

        long actions = PlaybackStateCompat.ACTION_PLAY_PAUSE|PlaybackStateCompat.ACTION_SEEK_TO;
        if (pc.pcCanPlayNext()) {
            actions |= PlaybackStateCompat.ACTION_SKIP_TO_NEXT;
        }
        if (pc.pcCanPlayPrevious()) {
            actions |= PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS;
        }

        stateBuilder.setActions(actions);
        stateBuilder.setState(state, pc.getPos(), 1.0f);
        ms.setPlaybackState(stateBuilder.build());
    }

    void updatePlayerMetadata()  {
        ms.setActive(true);
        SongData sd = pc.getCurrentlyPlaying();

        Bitmap art = BitmapFactory.decodeResource(getResources(), R.drawable.unknown);

        try {
            art = libraryManager.getAlbumArt(sd.albumID);
        } catch (IOException e) {}

        MediaMetadataCompat metadata = new MediaMetadataCompat.Builder()
                .putString(MediaMetadataCompat.METADATA_KEY_TITLE, sd.title)
                .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, libraryManager.getArtistNameForSongId(sd.id))
                .putString(MediaMetadataCompat.METADATA_KEY_ALBUM, "Album")
                .putLong(MediaMetadataCompat.METADATA_KEY_DURATION, sd.duration)
                .putBitmap(MediaMetadataCompat.METADATA_KEY_ART, art)
                .build();
        ms.setMetadata(metadata);

        updatePlayerState(PlaybackStateCompat.STATE_PLAYING);
        Intent notifyIntent = new Intent(this, MainActivity.class);
        notifyIntent.setAction("android.intent.action.MAIN");
        notifyIntent.addCategory("android.intent.category.LAUNCHER");
        notifyIntent.putExtra("resumeFrom", 1);
        PendingIntent notifyPendingIntent = PendingIntent.getActivity(
                this, 400, notifyIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        Notification notification = new NotificationCompat.Builder(this, "media_channel")
                .setSmallIcon(R.drawable.logo_normal)
                .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.unknown))
                .setContentTitle("Music")
                .setContentText("Playback notification")
                .setContentIntent(notifyPendingIntent)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setStyle(new androidx.media.app.NotificationCompat.MediaStyle()
                        .setMediaSession(ms.getSessionToken()).setShowActionsInCompactView(0,1,2))
                .build();
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
            NotificationManagerCompat.from(this).notify(1, notification);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        NotificationManagerCompat.from(this).cancel(1);
        ms.setActive(false);
        updatePlayerState(PlaybackStateCompat.STATE_NONE);
        ms.release();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
