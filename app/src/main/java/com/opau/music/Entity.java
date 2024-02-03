package com.opau.music;

import android.net.Uri;

public class Entity {
    public long id;
    public String title;
    public String artist;
    public String album;
    public String duration;
    public Uri albumart;

    public String getTitle() {
        return title;
    }

    public Entity(long id, String title, String album, String artist, Uri art, String duration) {
        this.id = id;
        this.title = title;
        this.artist = artist;
        this.albumart = art;
        this.album = album;
        this.duration = duration;
    }
}
