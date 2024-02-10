package com.opau.music;

import android.annotation.SuppressLint;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
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

    public int updateLibrary() {
        int updateCount = 0;
        long start = System.currentTimeMillis();
        /*
        updateCount += updateSongs();
        updateCount += updateArtists();
        updateCount += updateAlbums();
        */
        updateCount = updateField(Entity.Type.SONG)+updateField(Entity.Type.ARTIST)+updateField(Entity.Type.ALBUM);
        try {
            Thread.sleep(1000);
        } catch (Exception e) {};
        long end = System.currentTimeMillis();
        Log.i("Performance", "Library update took " + (end-start) + " ms");
        return updateCount;
    }

    @SuppressLint("Range")
    public ArrayList<Entity> getEntities(Entity.Type type) {
        ArrayList<Entity> ret = new ArrayList<>();
        String table = "";

        switch (type) {
            case SONG:
                table = "songs";
                break;
            case ARTIST:
                table = "artists";
                break;
            case ALBUM:
                table = "albums";
                break;
        }

        Cursor c = db.rawQuery("SELECT id FROM "+table, null);
        if (c != null && c.moveToFirst()) {
            do {
                Entity e = getEntityForId(type, c.getLong(0));
                ret.add(e);
            } while (c.moveToNext());
        }
        c.close();
        return ret;
    }



    int updateField(Entity.Type type) {
        Uri musicUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        int newItems = 0, updatedItems = 0, deletedItems = 0;
        String table = "";
        String[] projection = new String[]{};
        switch (type) {
            case SONG:
                projection = new String[]{MediaStore.Audio.Media._ID, MediaStore.Audio.Media.TITLE, MediaStore.Audio.Media.ARTIST_ID, MediaStore.Audio.Media.ALBUM_ID, MediaStore.Audio.Media.DURATION, MediaStore.Audio.Media.DATA};
                table = "songs";
                break;
            case ARTIST:
                projection = new String[]{MediaStore.Audio.Media.ARTIST_ID, MediaStore.Audio.Media.ARTIST};
                table = "artists";
                break;
            case ALBUM:
                projection = new String[]{MediaStore.Audio.Media.ALBUM_ID, MediaStore.Audio.Media.ALBUM};
                table = "albums";
                break;
        }
        ContentValues updaterValues = new ContentValues();
        updaterValues.put("valid", 0);
        db.update(table, updaterValues, null, null);

        Cursor c = context.getContentResolver().query(musicUri, projection, MediaStore.Audio.Media.IS_MUSIC + "!= 0", null, "artist_id ASC");
        if (c == null || !c.moveToFirst()) {
            return 0;
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
            switch (type) {
                case SONG:
                    cv.put("id", id);
                    cv.put("title", c.getString(1));
                    cv.put("artist_id", c.getLong(2));
                    cv.put("album_id", c.getLong(3));
                    cv.put("duration", c.getString(4));
                    cv.put("path", c.getString(5));
                    break;
                case ARTIST:
                    cv.put("id", id);
                    cv.put("name", c.getString(1));
                    break;
                case ALBUM:
                    cv.put("id", id);
                    cv.put("title", c.getString(1));
                    break;
            }

            if (isEntityInLibrary(type, id)) {
                if (hasEntityChanged(type, id, cv)) {
                    db.update(table, cv, "id = " + id, null);
                    updatedItems++;
                }
                cv = new ContentValues();
                cv.put("valid", 1);
                db.update(table, cv, "id = " + id, null);
            } else {
                db.insert(table, null, cv);
                newItems++;
            }

        } while (c.moveToNext());
        deletedItems = db.delete("albums", "valid = 0", null);
        Log.i("LibraryManager/"+table, newItems + " added, " + updatedItems + " updated, " + deletedItems + " deleted.");
        c.close();
        return newItems+updatedItems+deletedItems;
    }

    public boolean hasEntityChanged(Entity.Type type, long id, ContentValues newValues) {
        newValues.put("valid", 0);
        String table = "";
        switch (type) {
            case SONG:
                table = "songs";
                break;
            case ARTIST:
                table = "artists";
                break;
            case ALBUM:
                table = "albums";
                break;
        }
        Cursor c =  db.rawQuery("SELECT * FROM "+table+" WHERE id = " + id, null);
        c.moveToFirst();
        ContentValues oldValues = new ContentValues();
        DatabaseUtils.cursorRowToContentValues(c, oldValues);
        //Log.v("LM/Check", oldValues.toString() + " new: " + newValues.toString());
        c.close();

        return !oldValues.toString().equals(newValues.toString());
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
                c = db.rawQuery("SELECT id, title FROM albums WHERE id = " + id, null);
                if (c.moveToFirst()) {
                    e.setEntityType(Entity.Type.ALBUM);
                    AlbumData ad = new AlbumData();
                    ad.id = c.getLong(0);
                    ad.title = c.getString(1);
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

    public void deleteLibrary() {
        db.execSQL("DROP TABLE IF EXISTS songs");
        db.execSQL("DROP TABLE IF EXISTS artists");
        db.execSQL("DROP TABLE IF EXISTS albums");
        db.execSQL("DROP TABLE IF EXISTS playlists");
        dbHelper.onCreate(db);
    }
}
