package com.opau.music;

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;

import com.google.android.material.color.DynamicColors;

import java.util.UUID;

public class App extends Application {
    private LibraryManager libraryManager;
    private PlaybackCoordinator playbackCoordinator;
    @Override
    public void onCreate() {
        super.onCreate();
        NotificationChannel channel = new NotificationChannel(
                "media_channel",
                "Media Channel",
                NotificationManager.IMPORTANCE_LOW
        );
        NotificationManager notificationManager = getSystemService(NotificationManager.class);
        notificationManager.createNotificationChannel(channel);
        DynamicColors.applyToActivitiesIfAvailable(this);
        libraryManager = new LibraryManager(this);
        libraryManager.open();
    }

    public void setPlaybackCoordinator(PlaybackCoordinator c) {
        playbackCoordinator = c;
    }

    public PlaybackCoordinator getPlaybackCoordinator() {
        return playbackCoordinator;
    }

    public LibraryManager getLibraryManager() {
        return libraryManager;
    }

}
