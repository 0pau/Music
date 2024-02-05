package com.opau.music;

import android.net.Uri;

public class Entity {
    private Type entityType;
    private Object data;
    public Entity() {
    }
    public String getLabel() {
        switch (entityType) {
            case SONG:
                return ((SongData)data).title;
            case ALBUM:
                return ((AlbumData)data).title;
            case ARTIST:
                return ((ArtistData)data).name;
        }
        return "-";
    }

    public void setData(Object data) {
        this.data = data;
    }

    public void setEntityType(Type entityType) {
        this.entityType = entityType;
    }

    public Type getEntityType() {
        return entityType;
    }

    public Object getData() {
        return data;
    }

    public enum Type {SONG,ARTIST,ALBUM,PLAYLIST}
}