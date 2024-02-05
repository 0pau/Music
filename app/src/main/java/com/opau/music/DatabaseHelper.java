package com.opau.music;

import android.annotation.SuppressLint;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

public class DatabaseHelper extends SQLiteOpenHelper {
    public DatabaseHelper(@Nullable Context context) {
        super(context, "MediaLibrary.db", null, 5);
    }
    @SuppressLint("SQLiteString")
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE songs (id INTEGER PRIMARY KEY, title STRING, artist_id INTEGER, album_id INTEGER, duration INTEGER, valid INTEGER, path STRING)");
        db.execSQL("CREATE TABLE artists (id INTEGER, name STRING, valid INTEGER)");
        db.execSQL("CREATE TABLE albums (id INTEGER, title STRING, albumArtUri STRING, valid INTEGER)");
        db.execSQL("CREATE TABLE playlists (id INTEGER, title STRING)");
    }
    @Override
    public void onUpgrade(SQLiteDatabase db, int i, int i1) {
        db.execSQL("DROP TABLE IF EXISTS songs");
        db.execSQL("DROP TABLE IF EXISTS artists");
        db.execSQL("DROP TABLE IF EXISTS albums");
        db.execSQL("DROP TABLE IF EXISTS playlists");
        onCreate(db);
    }
}
