package com.opau.music;

import android.annotation.SuppressLint;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;
import android.util.Size;

import java.io.IOException;
import java.util.ArrayList;

public class LibraryManager {
    DatabaseHelper dbHelper;
    Context context;
    private SQLiteDatabase db;
    private int songCount;

    public LibraryManager(Context c) {
        context = c;
    }

    public LibraryManager open() {
        dbHelper = new DatabaseHelper(context);
        db = dbHelper.getWritableDatabase();
        return this;
    }
    public void close() {
        dbHelper.close();
    }

    public int getSongCount() {
        return songCount;
    }

    public boolean isEntityInLibrary(Entity.Type type, long id) {
        String cat = "undefined";
        switch (type) {
            case SONG:
                cat = "songs";
                break;
            case ALBUM:
                cat = "albums";
                break;
            case ARTIST:
                cat = "artists";
                break;
            default:
                return false;
        }
        Cursor c = db.query(cat, new String[]{"id"}, "id = " + id, null, null, null,null);
        if (c.getCount() == 1) {
            return true;
        }
        c.close();
        return false;
    }

    public void updateLibrary() {
        updateSongs();
        updateArtists();
        updateAlbums();
    }

    @SuppressLint("Range")
    public ArrayList<Entity> getSongs() {
        ArrayList<Entity> ret = new ArrayList<>();
        Cursor songCursor = db.rawQuery("SELECT id FROM songs", null);

        if (songCursor != null && songCursor.moveToFirst()) {
            do {
                Entity e = getEntityForId(Entity.Type.SONG, songCursor.getLong(0));
                ret.add(e);
            } while (songCursor.moveToNext());
        }
        songCount = ret.size();
        return ret;
    }

    public void updateSongs() {
        ContentValues updaterValues = new ContentValues();
        updaterValues.put("valid", 0);
        db.update("songs", updaterValues, null, null);
        String[] projection = {MediaStore.Audio.Media._ID, MediaStore.Audio.Media.TITLE, MediaStore.Audio.Media.ARTIST_ID, MediaStore.Audio.Media.ALBUM_ID, MediaStore.Audio.Media.DURATION, MediaStore.Audio.Media.DATA};
        Uri musicUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        Cursor musicCursor = context.getContentResolver().query(musicUri, projection, MediaStore.Audio.Media.IS_MUSIC + " != 0", null, "TITLE ASC");
        int newSongs = 0;

        if (musicCursor == null || !musicCursor.moveToNext()) {
            return;
        }
        do {
            ContentValues cv = new ContentValues();
            long id = musicCursor.getLong(0);
            cv.put("id", id);
            cv.put("title", musicCursor.getString(1));
            cv.put("artist_id", musicCursor.getLong(2));
            cv.put("album_id", musicCursor.getLong(3));
            cv.put("duration", musicCursor.getString(4));
            cv.put("path", musicCursor.getString(5));
            cv.put("valid", 1);
            if (isEntityInLibrary(Entity.Type.SONG, id)) {
                db.update("songs", cv, "id = " + id, null);
            } else {
                db.insert("songs", null, cv);
                newSongs++;
            }
        } while (musicCursor.moveToNext());
        int deletedSongs = db.delete("songs", "valid = 0", null);
        Log.i("LM/Songs", newSongs + " added, " + deletedSongs + " deleted.");
        musicCursor.close();
    }

    void updateArtists() {
        int newArtists = 0;
        ContentValues updaterValues = new ContentValues();
        updaterValues.put("valid", 0);
        db.update("artists", updaterValues, null, null);
        String[] projection = {MediaStore.Audio.Media.ARTIST_ID, MediaStore.Audio.Media.ARTIST};
        Uri musicUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        Cursor c = context.getContentResolver().query(musicUri, projection, MediaStore.Audio.Media.IS_MUSIC + "!= 0", null, "artist_id ASC");
        if (c == null || !c.moveToFirst()) {
            return;
        }
        ArrayList<Long> ids = new ArrayList<>();
        do {
            long id = c.getLong(0);
            if (ids.contains(id)) {
                continue;
            } else {
                ids.add(id);
            }
            ContentValues cv = new ContentValues();
            cv.put("id", id);
            cv.put("name", c.getString(1));
            cv.put("valid", 1);
            if (isEntityInLibrary(Entity.Type.ARTIST, id)) {
                db.update("artists", cv, "id = " + id, null);
            } else {
                db.insert("artists", null, cv);
                newArtists++;
            }
        } while (c.moveToNext());
        int deleted = db.delete("artists", "valid = 0", null);
        Log.i("LM/Artists", newArtists + " added, " + deleted + " deleted.");
        c.close();
    }

    void updateAlbums() {
        int newAlbums = 0;
        Uri artworkUri = Uri.parse("content://media/external/audio/albumart");
        ContentValues updaterValues = new ContentValues();
        updaterValues.put("valid", 0);
        db.update("albums", updaterValues, null, null);
        String[] projection = {MediaStore.Audio.Media.ALBUM_ID, MediaStore.Audio.Media.ALBUM};
        Uri musicUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        Cursor c = context.getContentResolver().query(musicUri, projection, MediaStore.Audio.Media.IS_MUSIC + "!= 0", null, "artist_id ASC");
        if (c == null || !c.moveToFirst()) {
            return;
        }
        ArrayList<Long> ids = new ArrayList<>();
        do {
            long id = c.getLong(0);
            if (!ids.contains(id)) {
                ids.add(id);
            } else {
                continue;
            }
            ContentValues cv = new ContentValues();
            cv.put("id", id);
            cv.put("valid", 1);
            cv.put("title", c.getString(1));
            Uri art_path = ContentUris.withAppendedId(artworkUri, id);
            cv.put("albumArtUri", art_path.toString());

            if (isEntityInLibrary(Entity.Type.ALBUM, id)) {
                db.update("albums", cv, "id =" + id, null);
            } else {
                db.insert("albums", null, cv);
                newAlbums++;
            }

        } while (c.moveToNext());
        int deleted = db.delete("albums", "valid = 0", null);
        Log.i("LM/Albums", newAlbums + " added, " + deleted + " deleted.");

        c.close();
    }

    @SuppressLint("Range")
    public Entity getEntityForId(Entity.Type type, long id) {
        Entity e = new Entity();
        Cursor c;
        switch (type) {
            case SONG:
                c = db.rawQuery("SELECT * FROM songs WHERE id = " + id, null);
                if (c.moveToFirst()) {
                    e.setEntityType(Entity.Type.SONG);
                    SongData sd = new SongData();
                    sd.id = c.getLong(0);
                    sd.title = c.getString(1);
                    sd.path = c.getString(c.getColumnIndex("path"));
                    sd.albumID = c.getLong(c.getColumnIndex("album_id"));
                    sd.duration = c.getLong(c.getColumnIndex("duration"));
                    sd.artistID = c.getLong(c.getColumnIndex("artist_id"));
                    e.setData(sd);
                }
                c.close();
                break;
            case ARTIST:
                c = db.rawQuery("SELECT id, name FROM artists WHERE id = " + id, null);
                if (c.moveToFirst()) {
                    e.setEntityType(Entity.Type.ARTIST);
                    ArtistData ad = new ArtistData();
                    ad.id = c.getLong(0);
                    ad.name = c.getString(1);
                    e.setData(ad);
                }
                c.close();
                break;
            case ALBUM:
                c = db.rawQuery("SELECT id, title,albumArtUri FROM albums WHERE id = " + id, null);
                if (c.moveToFirst()) {
                    e.setEntityType(Entity.Type.ALBUM);
                    AlbumData ad = new AlbumData();
                    ad.id = c.getLong(0);
                    ad.title = c.getString(1);
                    ad.albumArtUri = Uri.parse(c.getString(2));
                    e.setData(ad);
                }
                c.close();
                break;
        }
        return e;
    }

    public String getArtistNameForSongId(long id) {
        Entity e = getEntityForId(Entity.Type.ARTIST,((SongData)getEntityForId(Entity.Type.SONG, id).getData()).artistID);
        return ((ArtistData)e.getData()).name;
    }

    public String getAlbumNameForSongId(long id) {
        Entity e = getEntityForId(Entity.Type.ALBUM,((SongData)getEntityForId(Entity.Type.SONG, id).getData()).albumID);
        return ((AlbumData)e.getData()).title;
    }

    public Bitmap getAlbumArtThumbnail(int size, long id) throws IOException {
        Uri contentUri = ContentUris.withAppendedId(MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI, id);
        return context.getContentResolver().loadThumbnail(contentUri, new Size(size,size), null);
    }

    public Bitmap getAlbumArt(long id) throws IOException {
        return getAlbumArtThumbnail(512, id);
    }

    public Uri getAlbumArtUri(long id) {
        return ContentUris.withAppendedId(MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI, id);
    }
}
