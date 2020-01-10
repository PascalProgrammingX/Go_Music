package com.pca.gomusic.Handlers

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteCantOpenDatabaseException
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.provider.MediaStore
import com.pca.gomusic.Classes.ListSongs
import com.pca.gomusic.ModelClass.Song
import java.util.*

class PlayerDBHandler(private val context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {
    var fetchedPlayingPos = -1
        private set

    fun getSongFromId(id: Long): Song? {
        System.gc()
        val where = MediaStore.Audio.Media._ID + "=" + id
        val musicCursor = context.contentResolver.query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                null, where, null, null)
        if (musicCursor != null && musicCursor.moveToFirst()) {
            val titleColumn = musicCursor.getColumnIndex(MediaStore.Audio.Media.TITLE)
            val idColumn = musicCursor.getColumnIndex(MediaStore.Audio.Media._ID)
            val artistColumn = musicCursor.getColumnIndex(MediaStore.Audio.Media.ARTIST)
            val pathColumn = musicCursor.getColumnIndex(MediaStore.Audio.Media.DATA)
            val albumIdColumn = musicCursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID)
            val albumColumn = musicCursor.getColumnIndex(MediaStore.Audio.Media.ALBUM)
            val addedDateColumn = musicCursor.getColumnIndex(MediaStore.Audio.Media.DATE_ADDED)
            val songDurationColumn = musicCursor.getColumnIndex(MediaStore.Audio.Media.DURATION)
            val songSizeColumn = musicCursor.getColumnIndex(MediaStore.Audio.Media.SIZE)
            val artistIdColumn = musicCursor.getColumnIndex(MediaStore.Audio.Media.ARTIST_ID)
            return Song(musicCursor.getLong(idColumn),
                    musicCursor.getString(titleColumn),
                    musicCursor.getString(artistColumn),
                    musicCursor.getString(pathColumn),
                    musicCursor.getLong(albumIdColumn),
                    musicCursor.getLong(addedDateColumn),
                    musicCursor.getInt(songSizeColumn).toLong(),
                    musicCursor.getLong(songDurationColumn),
                    musicCursor.getLong(artistIdColumn))
        }
        return null
    }

    override fun onCreate(db: SQLiteDatabase) {
        val CREATE_PLAYBACK_SONG_TABLE = "CREATE TABLE " + TABLE_PLAYBACK + " (" +
                SONG_KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                SONG_KEY_REAL_ID + " INTEGER," +
                SONG_KEY_LAST_PLAYED + " INTEGER)"
        db.execSQL(CREATE_PLAYBACK_SONG_TABLE)
    }

    override fun onUpgrade(db: SQLiteDatabase, i: Int, i1: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_PLAYBACK")
    }

    fun changePlaybackList(songs: ArrayList<Song>, currentPlaying: Int) {
        val db = this.writableDatabase
        clearList(db)
        for (i in songs.indices) {
            val values = ContentValues()
            values.putNull(SONG_KEY_ID)
            values.put(SONG_KEY_REAL_ID, songs[i].songId)
            if (i == currentPlaying) values.put(SONG_KEY_LAST_PLAYED, true) else values.put(SONG_KEY_LAST_PLAYED, false)
            db.insert(TABLE_PLAYBACK, null, values)
        }
    }

    //db.close();
    val allPlaybackSongs: ArrayList<Song>
        get() {
            val db = this.writableDatabase
            val query = "SELECT  * FROM $TABLE_PLAYBACK"
            val cursor = db.rawQuery(query, null)
            val playbackSongs = ArrayList<Song>()
            if (cursor.moveToFirst()) {
                var counter = 0
                do {
                    playbackSongs.add(ListSongs.getSong(context, cursor.getLong(1))!!)
                    if (cursor.getInt(2) == 1) fetchedPlayingPos = counter
                    counter++
                } while (cursor.moveToNext())
            }
            //db.close();
            return playbackSongs
        }

    fun addSong(song: Song) {
        val db = this.writableDatabase
        val values = ContentValues()
        values.putNull(SONG_KEY_ID)
        values.put(SONG_KEY_REAL_ID, song.songId)
        values.put(SONG_KEY_LAST_PLAYED, false)
        db.insert(TABLE_PLAYBACK, null, values)
        //  db.close();
    }

    fun updatePlayingPosition(songId: Long) {
        val db = this.writableDatabase
        db.execSQL("UPDATE $TABLE_PLAYBACK SET $SONG_KEY_LAST_PLAYED='0' WHERE $SONG_KEY_LAST_PLAYED='1'")
        db.execSQL("UPDATE " + TABLE_PLAYBACK + " SET " + SONG_KEY_LAST_PLAYED + "='1' WHERE "
                + SONG_KEY_REAL_ID + "='" + songId + "'")
        //db.close();
    }

    private fun clearList(db: SQLiteDatabase) {
        try {
            db.execSQL("DELETE FROM $TABLE_PLAYBACK")
            //db.close();
        } catch (e: SQLiteCantOpenDatabaseException) {
            e.printStackTrace()
        }
    }

    companion object {
        private const val DATABASE_VERSION = 1
        private const val DATABASE_NAME = "PlaybackDB"
        private const val TABLE_PLAYBACK = "songs"
        private const val SONG_KEY_ID = "song_id"
        private const val SONG_KEY_REAL_ID = "song_real_id"
        private const val SONG_KEY_LAST_PLAYED = "song_last_played"
    }

}