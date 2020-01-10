package com.pca.gomusic.Services

import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Handler
import android.os.IBinder
import android.widget.Toast
import com.pca.gomusic.Adapters.AlbumAdapter
import com.pca.gomusic.Adapters.ArtistAdapter
import com.pca.gomusic.Adapters.SongAdapter
import com.pca.gomusic.Handlers.NotificationHandler
import com.pca.gomusic.Handlers.PlayerHandler
import kotlinx.coroutines.runBlocking
import java.io.IOException
import java.util.Objects


open class PlayerService : Service() {
    private var musicPlayerHandler: PlayerHandler? = null

    private var context: Context? = null
    private var notificationHandler: NotificationHandler? = null
    private val playerServiceBroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            try {
                handleBroadcastReceived( intent, notificationHandler)
            } catch (e: IOException) {
                e.printStackTrace()
                Toast.makeText(this@PlayerService, e.toString(), Toast.LENGTH_SHORT).show()
            }

        }
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        context = this

        if (musicPlayerHandler == null)
            musicPlayerHandler = PlayerHandler(context as PlayerService, this)
        val filter = IntentFilter()
        filter.addAction(ACTION_PLAY_SINGLE)
        filter.addAction(ACTION_PLAY_ALL_SONGS)
        filter.addAction(ACTION_PLAY_ALL_SONGS_FROM_LASTADDED)
        filter.addAction(ACTION_PLAY_ALL_ALBUM_SONGS)
        filter.addAction(ACTION_PLAY_ALBUM)
        filter.addAction(ACTION_GET_SONG)
        filter.addAction(ACTION_NEXT_SONG)
        filter.addAction(ACTION_PREV_SONG)
        filter.addAction(ACTION_PAUSE_SONG)
        filter.addAction(ACTION_SEEK_SONG)
        filter.addAction(ACTION_CHANGE_SONG)
        filter.addAction(ACTION_PLAY_PLAYLIST)
        filter.addAction(ACTION_PLAY_ARTIST)
        filter.addAction(ACTION_ADD_QUEUE)
        filter.addAction(ACTION_REPEAT)
        filter.addAction(SHURFFLE_ON)
        filter.addAction(SHUFFLE_OFF)

        registerReceiver(playerServiceBroadcastReceiver, filter)
        notificationHandler = NotificationHandler(context!!, this)
        return START_NOT_STICKY
    }

    @Throws(IOException::class)
    private fun handleBroadcastReceived( intent: Intent, notificationHandler: NotificationHandler?) {
        when (Objects.requireNonNull<String>(intent.action)) {
            ACTION_PLAY_SINGLE -> {
                musicPlayerHandler!!.playSingleSong(
                        intent.getIntExtra("pos", 0))
                updatePlayer()
            }
            ACTION_PLAY_ALL_SONGS -> {
                Handler().postDelayed({
                    musicPlayerHandler!!.playAllSongs(intent.getLongExtra("songId", 0))
                },1000)
            }
            ACTION_PLAY_ALL_SONGS_FROM_LASTADDED -> { musicPlayerHandler!!.playAllSongsFromLastAdded( intent.getIntExtra("pos", 0))
                updatePlayer()
            }
            ACTION_PLAY_ALL_ALBUM_SONGS -> { musicPlayerHandler!!.playAllAlbumSongs( intent.getLongExtra("albumId", 0),
                            intent.getLongExtra("songId", 0))
                updatePlayer()
            }
            ACTION_PLAY_ALBUM -> playAlbumSongs(intent)
            ACTION_GET_SONG -> try {
                updatePlayer()
            } catch (e: ArrayIndexOutOfBoundsException) {
                e.printStackTrace()
            }

            ACTION_NEXT_SONG -> notificationHandler?.let { musicPlayerHandler!!.playNextSong( musicPlayerHandler!!.currentPlayingPos + 1)
            }
            ACTION_PREV_SONG -> {
                    musicPlayerHandler!!.playPrevSong( musicPlayerHandler!!.currentPlayingPos - 1)
                updatePlayer()
            }
            ACTION_PAUSE_SONG -> { musicPlayerHandler!!.playOrStop()
                updatePlayer()
            }
            ACTION_SEEK_SONG -> musicPlayerHandler!!.seek(intent.getIntExtra("seek", 0))
            ACTION_CHANGE_SONG -> musicPlayerHandler!!.playNextSong( intent.getIntExtra("pos", 0))
            //            case ACTION_PLAY_PLAYLIST:
            //                musicPlayerHandler.playPlaylist(intent.getIntExtra("id", 0),
            //                        intent.getIntExtra("pos", 0));
            //                updatePlayer();
            //                break;
            ACTION_PLAY_ARTIST -> {
                    musicPlayerHandler!!.playArtistSongs(intent.getStringExtra("name"), intent.getIntExtra("pos", 0))
                updatePlayer()
            }
            ACTION_ADD_QUEUE -> musicPlayerHandler!!.addSongToQueue(intent.getLongExtra("songId", 0))
            ACTION_REPEAT -> musicPlayerHandler!!.repeatSong()
            SHURFFLE_ON -> musicPlayerHandler!!.shuffleSongs()
            SHUFFLE_OFF -> musicPlayerHandler!!.shuffleOff( musicPlayerHandler!!.currentPlayingPos + 1)
        }
    }

    @Throws(IOException::class)
    private fun playAlbumSongs(intent: Intent) {
        musicPlayerHandler!!.playAlbumSongs( intent.getLongExtra("albumId", 0),
                intent.getIntExtra("songPos", 0))
    }

   open fun updatePlayer() {
        val i = Intent()
        i.action = SongAdapter.ACTION_ID_RECIEVED
        i.putExtra("running", musicPlayerHandler!!.getMediaPlayer()!!.isPlaying)
        i.putExtra("songId", musicPlayerHandler!!.currentPlayingSongId)
        i.putExtra("songName", musicPlayerHandler!!.currentPlayingSong.name)
        i.putExtra("albumId", musicPlayerHandler!!.currentPlayingSong.albumId)
        i.putExtra("seek", musicPlayerHandler!!.getMediaPlayer()!!.currentPosition)
        i.putExtra("pos", musicPlayerHandler!!.currentPlayingPos)
        i.putExtra("duration", musicPlayerHandler!!.songDuration)
        i.putExtra("next_song_name", musicPlayerHandler!!.nextSongDisplayName())
        i.putExtra("_art0", musicPlayerHandler!!.currentPlayingSong.artist)
        context!!.sendOrderedBroadcast(i, null)
        updateNotificationPlayer()
       runBlocking {
           updateAlbumAdapter()
           updateArtistAdapter()
       }
    }



    open fun updateAlbumAdapter(){
    val i = Intent()
        i.action = AlbumAdapter.ACTION_PLAYING_ALBULM
        i.putExtra("running", true)
        i.putExtra("albumID", musicPlayerHandler?.currentPlayingSong?.albumId)
        context?.sendBroadcast(i)
    }




    open fun updateArtistAdapter(){
        val i = Intent()
        i.action = ArtistAdapter.ACTION_PLAYING_ARTIST
        i.putExtra("running", true)
        i.putExtra("artistId", musicPlayerHandler?.currentPlayingSong?.artistId)
        context?.sendBroadcast(i)
    }



    private fun updateNotificationPlayer() {
        if (notificationHandler!!.isNotificationActive) {
            notificationHandler!!.setNotificationPlayer(true)
            notificationHandler!!.changeNotificationDetails(musicPlayerHandler!!
                    .currentPlayingSong.name, musicPlayerHandler!!
                    .currentPlayingSong.artist, musicPlayerHandler!!
                    .currentPlayingSong.albumId, musicPlayerHandler!!
                    .getMediaPlayer()!!.isPlaying)
        } else {
            notificationHandler!!.setNotificationPlayer(false)
            notificationHandler!!.changeNotificationDetails(musicPlayerHandler!!
                    .currentPlayingSong.name, musicPlayerHandler!!
                    .currentPlayingSong.artist, musicPlayerHandler!!
                    .currentPlayingSong.albumId, musicPlayerHandler!!
                    .getMediaPlayer()!!.isPlaying)
        }
    }


    override fun onBind(intent: Intent): IBinder? {
        return null
    }

 /*   override fun onDestroy() {
        super.onDestroy()
        val mp = musicPlayerHandler!!.getMediaPlayer()
        if (mp != null) {
            mp.stop()
            mp.release()
            context!!.unregisterReceiver(playerServiceBroadcastReceiver)
        }
    }*/

    companion object {

        const val ACTION_PLAY_SINGLE = "ACTION_PLAY_SINGLE"
        const val ACTION_PLAY_ALL_SONGS = "ACTION_PLAY_ALL_SONGS"
        const val ACTION_PLAY_ALL_ALBUM_SONGS = "ACTION_PLAY_ALL_ALBUM_SONGS"
        const val ACTION_PLAY_ALL_SONGS_FROM_LASTADDED = "ACTION_PLAY_ALL_SONGS_FROM_LASTADDED"
        const val ACTION_PLAY_ALBUM = "ACTION_PLAY_ALBUM"
        const val ACTION_PLAY_PLAYLIST = "ACTION_PLAY_PLAYLIST"
        const val ACTION_PLAY_ARTIST = "ACTION_PLAY_ARTIST"
        const val ACTION_GET_SONG = "ACTION_GET_SONG"
        const val ACTION_CHANGE_SONG = "ACTION_CHANGE_SONG"
        const val ACTION_SEEK_SONG = "ACTION_SEEK_SONG"
        const val ACTION_NEXT_SONG = "ACTION_NEXT_SONG"
        const val ACTION_PREV_SONG = "ACTION_PREV_SONG"
        const val ACTION_PAUSE_SONG = "ACTION_PAUSE_SONG"
        const val ACTION_ADD_QUEUE = "ACTION_ADD_QUEUE"
        const val ACTION_REPEAT = "ACTION_REPEAT"
        const val SHURFFLE_ON = "SHURFFLE"
        const val SHUFFLE_OFF = "SHUFFLE_OFF"
    }

}
