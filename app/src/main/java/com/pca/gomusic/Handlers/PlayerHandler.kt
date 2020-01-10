package com.pca.gomusic.Handlers


import android.content.Context
import android.content.Intent
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.media.MediaPlayer
import android.net.Uri
import android.provider.MediaStore
import android.widget.Toast
import com.pca.gomusic.Activities.MainActivity
import com.pca.gomusic.Adapters.SongAdapter
import com.pca.gomusic.Classes.ListSongs
import com.pca.gomusic.ModelClass.Song
import com.pca.gomusic.Services.PlayerService
import java.io.IOException
import java.util.ArrayList
import java.util.Random


class PlayerHandler(private val context: Context, private val service: PlayerService) {

    private val preferenceHandler: UserPreferenceHandler
    private val dbHandler: PlayerDBHandler
    private var currentPlayingSongs: ArrayList<Song>? = null
    var currentPlayingPos = -1
    private var state: Boolean = false


    private val mAudioManager: AudioManager
    private val mOnAudioFocusChangeListener = AudioManager.OnAudioFocusChangeListener{ focusChange ->
        if (focusChange == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT || focusChange == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK) {
            if (mediaPlayer!!.isPlaying){
                mediaPlayer?.pause()
                service.updatePlayer()
            }
        } else if (focusChange == AudioManager.AUDIOFOCUS_GAIN || focusChange == AudioManager.AUDIOFOCUS_GAIN_TRANSIENT) {
            if (mediaPlayer!!.isPlaying){
                service.updatePlayer()
            }
        } else if (focusChange == AudioManager.AUDIOFOCUS_LOSS) {
            running()
        }
    }


    val songDuration: Long
        get() = currentPlayingSongs!![currentPlayingPos].durationLong


    val currentPlayingSongId: Long
        get() = if (currentPlayingPos == -1 || currentPlayingPos >= currentPlayingSongs!!.size) -1 else currentPlayingSongs!![currentPlayingPos].songId

    val currentPlayingSong: Song get() = currentPlayingSongs!![currentPlayingPos]


    private fun requestAudioFocus() {
        mAudioManager.requestAudioFocus(mOnAudioFocusChangeListener, AudioManager.STREAM_MUSIC,
                AudioManager.AUDIOFOCUS_GAIN)
    }

    /*private fun dropAudioFocus() {
        mAudioManager.abandonAudioFocus(mOnAudioFocusChangeListener)
    }*/


    init {
        mediaPlayer = MediaPlayer()
        currentPlayingSongs = ArrayList()
        dbHandler = PlayerDBHandler(context)
        preferenceHandler = UserPreferenceHandler(context)
        mAudioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager

        requestAudioFocus()
    }

    @Throws(IOException::class)
    fun playAlbumSongs( albumId: Long, startSongPos: Int) {
        stopPlayer()
        requestAudioFocus()
        setCurrentPlayingSongs(ListSongs.getAlbumSongList(context, albumId))
        setPlayingPos(startSongPos)
        mediaPlayer = MediaPlayer.create(context,Uri.parse(currentPlayingSongs!![startSongPos].path))
            mediaPlayer!!.start()
            service.updatePlayer()
        mediaPlayer!!.setOnCompletionListener {
            try {
                playNextSong( shuffle())
            } catch (e: IOException) {
                e.printStackTrace()
                Toast.makeText(context, e.toString(),
                        Toast.LENGTH_SHORT).show()
            }
        }

    }


    private fun stopPlayer() {
        if (mediaPlayer!!.isPlaying) {
            mediaPlayer!!.stop()
        }
    }


    @Throws(IOException::class)
    fun playArtistSongs( name: String, pos: Int) {
        val songs = ListSongs.getSongsListOfArtist(context, name)
        setCurrentPlayingSongs(songs)
        configurePlayer( pos)
    }


    @Throws(IOException::class)
    fun playAllSongs( songId: Long) {
      setCurrentPlayingSongs(ListSongs.getSongList(context, "") )
        val songToPlayPos = findForASongInArrayList(songId)
        configurePlayer( songToPlayPos)
    }


    @Throws(IOException::class)
    fun playAllAlbumSongs( albumId: Long, songId: Long) {
        setCurrentPlayingSongs(ListSongs.getAlbumSongList(context, albumId))
        val songToPlayPos = findForASongInArrayList(songId)
        configurePlayer( songToPlayPos)
    }


    @Throws(IOException::class)
    fun playAllSongsFromLastAdded( pos: Int) {
        setCurrentPlayingSongs(ListSongs.getSongListByDateAdded(context))
        configurePlayer( pos)
    }


    fun playOrStop() {
        if (mediaPlayer!!.isPlaying) {
            mediaPlayer!!.pause()
            state = false
            updateSongAdapter()
            requestAudioFocus()
            service.stopForeground(true)
        } else {
            mediaPlayer!!.start()
            state = true
            updateSongAdapter()
        }
    }


    private fun updateSongAdapter() {
        val intent = Intent()
        intent.action = SongAdapter.ACTION_PLAY_PAUSE
        intent.putExtra("running", state)
        context.sendBroadcast(intent)
    }


     fun running() {
        val intent = Intent()
        intent.putExtra("running", state)
        intent.action = MainActivity.ACTION_CHANGE_BUTTON
        context.sendBroadcast(intent)
    }


     private fun getNextSongPosition(currentPos: Int): Int {
        return when {
            preferenceHandler.isRepeatOneEnabled ->
                currentPos
            preferenceHandler.isShuffleEnabled -> {
                shuffle()
            }
            else -> if (currentPos == currentPlayingSongs!!.size - 1 && preferenceHandler.isRepeatAllEnabled) {
                //Play 1st song as it was last song
                0
            } else if (currentPos == currentPlayingSongs!!.size - 1) {
                currentPos
            } else {
                currentPos + 1
            }
        }
    }


    @Throws(IOException::class)
    private fun configurePlayer( pos: Int) {
        stopPlayer()
        requestAudioFocus()
        setPlayingPos(pos)
        mediaPlayer = MediaPlayer.create(context, Uri.parse(currentPlayingSongs?.get(pos)?.path))
            mediaPlayer!!.start()
            service.updatePlayer()
        mediaPlayer!!.setOnCompletionListener {
            try {
                playNextSong( getNextSongPosition(pos))
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }


    @Throws(IOException::class)
    fun playNextSong( nextSongPos: Int) {
        if (nextSongPos < currentPlayingSongs!!.size) {
            if (preferenceHandler.isShuffleEnabled) {
                configurePlayer(nextSongPos)
                setPlayingPos(nextSongPos)
            } else {
                configurePlayer(nextSongPos)
                setPlayingPos(nextSongPos)
            }
        } else if (preferenceHandler.isRepeatAllEnabled) {
            configurePlayer( 0)
        }
    }


    @Throws(IOException::class)
    fun playPrevSong( prevSongPos: Int) {
        requestAudioFocus()
        if (mediaPlayer!!.currentPosition / 1000 <= 2) {
            val position: Int = if (prevSongPos == -1 && preferenceHandler.isRepeatAllEnabled) {
                //If song pos is more than 0
                getCurrentPlayingSongs()!!.size - 1
            } else if (prevSongPos == -1) {
                0
            } else
                prevSongPos
            stopPlayer()
            setPlayingPos(position)
            mediaPlayer = MediaPlayer.create(context, Uri.parse(currentPlayingSongs!![position].path))
            mediaPlayer!!.start()
            mediaPlayer!!.setOnCompletionListener {
                try {
                    playNextSong( getNextSongPosition(prevSongPos))
                } catch (e: IOException) {
                    e.printStackTrace()
                    Toast.makeText(context, e.toString(),
                            Toast.LENGTH_SHORT).show()
                }
            }
        } else {
            mediaPlayer!!.seekTo(0)
        }
    }


    fun nextSongDisplayName(): String {
        return currentPlayingSongs!![getNextSongPosition(currentPlayingPos)].name!!
    }

    private fun findForASongInArrayList(searchId: Long): Int {
        for (i in currentPlayingSongs!!.indices) {
            if (currentPlayingSongs!![i].songId == searchId) {
                return i
            }
        }
        return -1
    }

    @Throws(IOException::class)
    fun playSingleSong( pos: Int) {
        stopPlayer()
        requestAudioFocus()
        setPlayingPos(pos)
        val songToPlay = ListSongs.getSongList(context, "")
        setCurrentPlayingSongs(songToPlay)
        mediaPlayer = MediaPlayer.create(context, Uri.parse(songToPlay[pos].path))
        mediaPlayer!!.start()
        mediaPlayer!!.setOnCompletionListener {
            playNextSong(pos)
        }
    }


    private fun setPlayingPos(pos: Int) {
        currentPlayingPos = pos
        state = true
        if (getCurrentPlayingSongs()!!.size != 0) {
            dbHandler.updatePlayingPosition(currentPlayingSongs!![pos].songId)
        }
    }


    fun repeatSong() {
        mediaPlayer!!.isLooping = getMediaPlayer() != null && UserPreferenceHandler(context).isRepeatOneEnabled
    }


    fun shuffleSongs() {
        if (mediaPlayer != null) {
            mediaPlayer!!.setOnCompletionListener {
                try {
                    playNextSong(shuffle())
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        }
    }


    fun shuffleOff( pos: Int) {
        if (mediaPlayer != null) {
            mediaPlayer!!.setOnCompletionListener {
                try {
                    playNextSong( pos)
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        }
    }


    private fun shuffle(): Int {
        val random = Random()
        return random.nextInt(currentPlayingSongs!!.size - 1 + 1)
    }

    private fun getCurrentPlayingSongs(): ArrayList<Song>? {
        return currentPlayingSongs
    }

    private fun setCurrentPlayingSongs(currentPlayingSongs: ArrayList<Song>) {
        this.currentPlayingSongs!!.clear()
        this.currentPlayingSongs = currentPlayingSongs
    }

    fun getMediaPlayer(): MediaPlayer? {
        return mediaPlayer
    }

    private fun getSongFromId(id: Long): Song? {
        return dbHandler.getSongFromId(id)
    }


    fun seek(seek: Int) {
        mediaPlayer!!.seekTo(seek)
    }

    fun addSongToQueue(songId: Long) {
        val song = getSongFromId(songId)
        if (mediaPlayer != null && mediaPlayer!!.isPlaying) {
                currentPlayingSongs!!.add(getNextSongPosition(currentPlayingPos), song!!)
            dbHandler.addSong(song)
        } else {
            Toast.makeText(context, "can't play. \n MediaPlayer is not active", Toast.LENGTH_LONG).show()
        }
    }


    companion object{
        var  mediaPlayer: MediaPlayer? = null
    }


}
