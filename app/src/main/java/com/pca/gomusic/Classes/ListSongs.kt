package com.pca.gomusic.Classes

import android.content.Context
import android.database.Cursor
import android.provider.MediaStore
import com.pca.gomusic.ModelClass.Album
import com.pca.gomusic.ModelClass.Artist
import com.pca.gomusic.ModelClass.Song
import java.lang.Exception
import java.util.ArrayList
import java.util.Objects


object ListSongs{

        fun getSongsListOfArtist(context: Context, artistName: String): ArrayList<Song> {
            val songList = ArrayList<Song>()
            val where = (MediaStore.Audio.Media.IS_MUSIC + "=1 AND "
                    + MediaStore.Audio.Media.ARTIST + "='" + artistName.replace("'", "''") + "'")
            val orderBy = MediaStore.Audio.Media.DATE_ADDED + " DESC"
            val projection = arrayOf(MediaStore.Audio.Media.TITLE, MediaStore.Audio.Media._ID, MediaStore.Audio.Media.ARTIST, MediaStore.Audio.Media.DATA, MediaStore.Audio.Media.ALBUM_ID, MediaStore.Audio.Media.ALBUM, MediaStore.Audio.Media.DATE_ADDED, MediaStore.Audio.Media.DURATION, MediaStore.Audio.Media.SIZE, MediaStore.Audio.Media.ARTIST_ID)
            val musicCursor = context.contentResolver.query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                    projection, where, null, orderBy)
            if (musicCursor != null && musicCursor.moveToFirst()) {
                val titleColumn = musicCursor.getColumnIndex(MediaStore.Audio.Media.TITLE)
                val idColumn = musicCursor.getColumnIndex(MediaStore.Audio.Media._ID)
                val artistColumn = musicCursor.getColumnIndex(MediaStore.Audio.Media.ARTIST)
                val pathColumn = musicCursor.getColumnIndex(MediaStore.Audio.Media.DATA)
                val albumIdColumn = musicCursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID)
                val addedDateColumn = musicCursor.getColumnIndex(MediaStore.Audio.Media.DATE_ADDED)
                val songDurationColumn = musicCursor.getColumnIndex(MediaStore.Audio.Media.DURATION)
                val songSizeColumn = musicCursor.getColumnIndex(MediaStore.Audio.Media.SIZE)
                val artistIdColumn = musicCursor.getColumnIndex(MediaStore.Audio.Media.ARTIST_ID)
                do {
                    songList.add(Song(musicCursor.getLong(idColumn),
                            musicCursor.getString(titleColumn),
                            musicCursor.getString(artistColumn),
                            musicCursor.getString(pathColumn),
                            musicCursor.getLong(albumIdColumn),
                            musicCursor.getLong(addedDateColumn),
                            musicCursor.getLong(songSizeColumn),
                            musicCursor.getLong(songDurationColumn),
                            musicCursor.getLong(artistIdColumn)))
                } while (musicCursor.moveToNext())
                musicCursor.close()
            }
            System.gc()
            return songList
        }


        fun getAlbumList(context: Context): ArrayList<Album> {
            val albumList = ArrayList<Album>()
            val orderBy = MediaStore.Audio.Albums.ALBUM
            val projection = arrayOf(MediaStore.Audio.Albums._ID,
                    MediaStore.Audio.Albums.ALBUM,
                    MediaStore.Audio.Albums.ARTIST,
                    MediaStore.Audio.Albums.ALBUM_ART,
                    MediaStore.Audio.Albums.NUMBER_OF_SONGS)
            val musicCursor = context.contentResolver.query(MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI, projection, null, null, orderBy)
            if (musicCursor != null && musicCursor.moveToFirst()) {
                do {
                    albumList.add(Album(musicCursor.getLong(0),
                            musicCursor.getString(1),
                            musicCursor.getString(2),
                            false, musicCursor.getString(3),
                            musicCursor.getInt(4)))
                } while (musicCursor.moveToNext())
                musicCursor.close()
                System.gc()
            }
            return albumList
        }


        fun getAlbumArt(context: Context, albumdId: Long): String {
            val cursor = context.contentResolver.query(MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI,
                    arrayOf(MediaStore.Audio.Albums._ID, MediaStore.Audio.Albums.ALBUM_ART),
                    MediaStore.Audio.Albums._ID + "=?",
                    arrayOf(albumdId.toString()), null)
            var imagePath = ""
            if (cursor != null && cursor.moveToFirst()) {
                imagePath = try {
                    cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Albums.ALBUM_ART))
                }catch (e:Exception){
                    ""
                }
            }
            cursor?.close()
            return imagePath
        }


        fun getAlbumSongList(context: Context, albumId: Long): ArrayList<Song> {
            val musicCursor: Cursor?
            val songs = ArrayList<Song>()
            val where = MediaStore.Audio.Media.ALBUM_ID + "=?"
            val whereVal = arrayOf(albumId.toString())
            val orderBy = MediaStore.Audio.Media.DEFAULT_SORT_ORDER
            val projection = arrayOf(MediaStore.Audio.Media.TITLE,
                    MediaStore.Audio.Media._ID,
                    MediaStore.Audio.Media.ARTIST,
                    MediaStore.Audio.Media.DATA,
                    MediaStore.Audio.Media.ALBUM_ID,
                    MediaStore.Audio.Media.ALBUM,
                    MediaStore.Audio.Media.DATE_ADDED,
                    MediaStore.Audio.Media.DURATION,
                    MediaStore.Audio.Media.SIZE,
                    MediaStore.Audio.Media.ARTIST_ID)
            try {
                musicCursor = context.contentResolver.query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                        projection, where, whereVal, orderBy)
                if (musicCursor != null && musicCursor.moveToFirst()) {
                    do {
                        songs.add(Song(musicCursor.getLong(1),
                                musicCursor.getString(0),
                                musicCursor.getString(2),
                                musicCursor.getString(3),
                                musicCursor.getLong(4),
                                musicCursor.getLong(6),
                                musicCursor.getLong(8),
                                musicCursor.getLong(7),
                                musicCursor.getLong(9)))
                    } while (musicCursor.moveToNext())
                    musicCursor.close()
                    System.gc()
                }
            }catch (e:Exception){
            }

            return songs
        }


        fun getSong(context: Context, songId: Long): Song? {
            val where = (MediaStore.Audio.Media.IS_MUSIC + "=1 AND "
                    + MediaStore.Audio.Media._ID + "='" + songId + "'")
            val projection = arrayOf(MediaStore.Audio.Media.TITLE,
                    MediaStore.Audio.Media._ID,
                    MediaStore.Audio.Media.ARTIST,
                    MediaStore.Audio.Media.DATA,
                    MediaStore.Audio.Media.ALBUM_ID,
                    MediaStore.Audio.Media.ALBUM,
                    MediaStore.Audio.Media.DATE_ADDED,
                    MediaStore.Audio.Media.DURATION,
                    MediaStore.Audio.Media.SIZE,
                    MediaStore.Audio.Media.ARTIST_ID)
            val musicCursor = context.contentResolver.query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                    projection, where, null, null)
            if (musicCursor != null && musicCursor.moveToFirst()) {
                return Song(musicCursor.getLong(1),
                        musicCursor.getString(0),
                        musicCursor.getString(2),
                        musicCursor.getString(3),
                        musicCursor.getLong(4),
                        musicCursor.getLong(6),
                        musicCursor.getLong(8),
                        musicCursor.getLong(7),
                        musicCursor.getLong(9))
            }
            musicCursor?.close()
            System.gc()
            return null
        }


        fun getArtistList(context: Context): ArrayList<Artist> {
            val albumList = ArrayList<Artist>()
            val orderBy = MediaStore.Audio.Artists.ARTIST
            val selection = MediaStore.Audio.Artists._ID
            val projection = arrayOf(MediaStore.Audio.Artists.ARTIST,
                    MediaStore.Audio.Artists._ID,
                    MediaStore.Audio.Artists.NUMBER_OF_ALBUMS,
                    MediaStore.Audio.Artists.NUMBER_OF_TRACKS)
            val musicCursor = context.contentResolver.query(MediaStore.Audio.Artists.EXTERNAL_CONTENT_URI, projection, selection, null, orderBy)

            if (musicCursor != null && musicCursor.moveToFirst()) {
                do {
                    albumList.add(Artist(musicCursor.getLong(1),
                            musicCursor.getString(0),
                            musicCursor.getInt(2),
                            musicCursor.getInt(3)))
                } while (musicCursor.moveToNext())
            }
            musicCursor?.close()
            System.gc()
            return albumList
        }


        //Here i query for songs(All)
        //return an Array.
        fun getSongList(context: Context, searchString: String?): ArrayList<Song> {
            val songList = ArrayList<Song>()
            val orderBy = MediaStore.Audio.Media.DEFAULT_SORT_ORDER
            var selectionArgs: Array<String>? = null
            val selection: String
            if (searchString != null && searchString.isNotEmpty()) {
                selection = "title LIKE ?"
                selectionArgs = arrayOf("%$searchString%")
            } else {
                selection = ""
            }
            val projection = arrayOf(MediaStore.Audio.Media.TITLE,
                    MediaStore.Audio.Media._ID,
                    MediaStore.Audio.Media.ARTIST,
                    MediaStore.Audio.Media.DATA,
                    MediaStore.Audio.Media.ALBUM_ID,
                    MediaStore.Audio.Media.ALBUM,
                    MediaStore.Audio.Media.DATE_ADDED,
                    MediaStore.Audio.Media.DURATION,
                    MediaStore.Audio.Media.SIZE,
                    MediaStore.Audio.Media.ARTIST_ID)
            val musicCursor = context.contentResolver.query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                    projection, selection, selectionArgs, orderBy)
            if (musicCursor != null && musicCursor.moveToFirst()) {
                do {
                    songList.add(Song(musicCursor.getLong(1),
                            musicCursor.getString(0),
                            musicCursor.getString(2),
                            musicCursor.getString(3),
                            musicCursor.getLong(4),
                            musicCursor.getLong(6),
                            musicCursor.getLong(8),
                            musicCursor.getLong(7),
                            musicCursor.getLong(9)))
                } while (musicCursor.moveToNext())
                musicCursor.close()
                System.gc()
            }
            return songList
        }


        fun getSongListByDateAdded(context: Context): ArrayList<Song> {
            val songList = ArrayList<Song>()
            //Sort list according to recently added.
            val sortOrder = MediaStore.Audio.Media.DATE_ADDED + " DESC"
            val projection = arrayOf(MediaStore.Audio.Media.TITLE,
                    MediaStore.Audio.Media._ID,
                    MediaStore.Audio.Media.ARTIST,
                    MediaStore.Audio.Media.DATA,
                    MediaStore.Audio.Media.ALBUM_ID,
                    MediaStore.Audio.Media.ALBUM,
                    MediaStore.Audio.Media.DATE_ADDED,
                    MediaStore.Audio.Media.DURATION,
                    MediaStore.Audio.Media.SIZE,
                    MediaStore.Audio.Media.ARTIST_ID)
            val musicCursor = context.contentResolver.query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                    projection, "date_added ", null, sortOrder)
            if (musicCursor != null && musicCursor.moveToFirst()) {
                do {
                    songList.add(Song(musicCursor.getLong(1),
                            musicCursor.getString(0),
                            musicCursor.getString(2),
                            musicCursor.getString(3),
                            musicCursor.getLong(4),
                            musicCursor.getLong(6),
                            musicCursor.getLong(8),
                            musicCursor.getLong(7),
                            musicCursor.getLong(9)))
                } while (musicCursor.moveToNext())
                musicCursor.close()
                System.gc()
            }
            return songList
        }

        fun getArtistIdFromName(context: Context, name: String): Long {
            val where = MediaStore.Audio.Artists.ARTIST + "='" + name + "'"
            val musicCursor = context.contentResolver.query(MediaStore.Audio.Artists.EXTERNAL_CONTENT_URI,
                    arrayOf(MediaStore.Audio.Artists._ID), where, null, null)
            if (musicCursor != null && musicCursor.moveToFirst()) {
                return musicCursor.getLong(0)
            }
            Objects.requireNonNull<Cursor>(musicCursor).close()
            System.gc()
            return 0
        }


        fun getAlbumListOfArtist(context: Context, artistId: Long): ArrayList<Album> {
            val albumList = ArrayList<Album>()
            System.gc()
            val projection = arrayOf(MediaStore.Audio.Artists.Albums.ALBUM, MediaStore.Audio.Artists._ID, MediaStore.Audio.Artists.Albums.ARTIST, MediaStore.Audio.Albums.NUMBER_OF_SONGS_FOR_ARTIST, MediaStore.Audio.Albums.ALBUM_ART)
            val musicCursor = context.contentResolver.query(MediaStore.Audio.Artists.Albums.getContentUri("external", artistId),
                    projection, null, null, MediaStore.Audio.Albums.DEFAULT_SORT_ORDER)
            if (musicCursor != null && musicCursor.moveToFirst()) {
                val titleColumn = musicCursor.getColumnIndex(MediaStore.Audio.Artists.Albums.ALBUM)
                val idColumn = musicCursor.getColumnIndex(MediaStore.Audio.Artists._ID)
                val artistColumn = musicCursor.getColumnIndex(MediaStore.Audio.Artists.Albums.ARTIST)
                val numOfSongsColumn = musicCursor.getColumnIndex(MediaStore.Audio.Albums.NUMBER_OF_SONGS_FOR_ARTIST)
                val albumArtColumn = musicCursor.getColumnIndex(MediaStore.Audio.Albums.ALBUM_ART)
                do {
                    albumList.add(Album(musicCursor.getLong(idColumn),
                            musicCursor.getString(titleColumn),
                            musicCursor.getString(artistColumn),
                            false, musicCursor.getString(albumArtColumn),
                            musicCursor.getInt(numOfSongsColumn)))
                } while (musicCursor.moveToNext())
                musicCursor.close()
            }
            return albumList
        }

    }
