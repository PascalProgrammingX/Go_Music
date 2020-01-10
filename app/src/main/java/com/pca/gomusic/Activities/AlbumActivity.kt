package com.pca.gomusic.Activities


import android.annotation.SuppressLint
import android.content.Intent
import android.database.Cursor
import android.os.AsyncTask
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.InterstitialAd
import com.google.firebase.perf.FirebasePerformance
import com.pca.gomusic.Adapters.AlbumListAdapter
import com.pca.gomusic.Constant.Constant
import com.pca.gomusic.ModelClass.Song
import com.pca.gomusic.R
import com.pca.gomusic.Services.PlayerService.Companion.ACTION_PLAY_ALBUM
import com.pca.gomusic.utils.Utils
import kotlinx.android.synthetic.main.activity_album.*
import kotlinx.android.synthetic.main.content_album_activity.*
import java.lang.Exception
import java.util.ArrayList


class AlbumActivity : AppCompatActivity() {

    private lateinit var interstitialAd: InterstitialAd

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val sharedPref = PreferenceManager.getDefaultSharedPreferences(this)
        val currentTheme = sharedPref.getString(Constant.THEME, "default")
        when {
            currentTheme.equals("night") -> setTheme(R.style.AppTheme_MainDark)
            else -> setTheme(R.style.AppTheme)
        }
        setContentView(R.layout.activity_album)

        interstitialAd = InterstitialAd(this)
        interstitialAd.adUnitId = resources.getString(R.string.home_interstial)
        interstitialAd.loadAd(AdRequest.Builder().build())


        when{
            currentTheme.equals("night") || currentTheme.equals("mimi") ->{
                toolbar!!.setNavigationIcon(R.drawable.ic_back_white)
            }else -> {
            toolbar!!.setNavigationIcon(R.drawable.ic_back_black)
        } }
        toolbar.setNavigationOnClickListener {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                finishAfterTransition()
            }else{
                finish()
            }
        }

            albumID = intent.getLongExtra(Constant.ALBUM_ID, 0)
            val album_name = intent.getStringExtra(Constant.ALBUM_NAME)
            name.text = album_name
            Glide.with(this).load(Utils.getAlbumArtUri(albumID)).error(R.drawable._art000).into(album_art)

        //get Songs from Album
        FetchAlbumSongs().execute()



        interstitialAd.adListener = object : AdListener(){
            override fun onAdClosed() {
                interstitialAd.loadAd(AdRequest.Builder().build())
            }
            override fun onAdFailedToLoad(p0: Int) {
                interstitialAd.loadAd(AdRequest.Builder().build())
            }

            override fun onAdLoaded() {
                interstitialAd.show()
            }
        }


            play_all_album!!.setOnClickListener {
                val playAlbumTrace = FirebasePerformance.getInstance().newTrace("play_Album_Trace")
                playAlbumTrace.start()
                val playerIntent = Intent()
                playerIntent.action = ACTION_PLAY_ALBUM
                playerIntent.putExtra("albumId", albumID)
                playerIntent.putExtra("songPos", 0)
                sendBroadcast(playerIntent)
                playAlbumTrace.stop()
            }
        }


    companion object {
        var albumID: Long = 0
    }


        override fun onBackPressed() {
            super.onBackPressed()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                finishAfterTransition()
            }else{
                finish()
            }
        }


    @SuppressLint("StaticFieldLeak")
    inner class FetchAlbumSongs:AsyncTask<Void, Void, ArrayList<Song>>() {

        override fun doInBackground(vararg params: Void?): ArrayList<Song> {
            val musicCursor: Cursor?
            val songs = ArrayList<Song>()
            val where = MediaStore.Audio.Media.ALBUM_ID + "=?"
            val whereVal = arrayOf(albumID.toString())
            val orderBy = MediaStore.Audio.Media.DATE_ADDED + " DESC"
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
                musicCursor = contentResolver.query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
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
            }catch (e: Exception){
            }

            return songs
        }


        override fun onPostExecute(result: ArrayList<Song>?) {
            super.onPostExecute(result)
            val list:ArrayList<Song> = result!!
            progress.visibility = View.GONE
            val albumListAdapter = AlbumListAdapter(this@AlbumActivity, list)
            albums.layoutManager = LinearLayoutManager(applicationContext)
            val decoration = DividerItemDecoration(applicationContext, LinearLayoutManager.VERTICAL)
            albums.addItemDecoration(decoration)
            albums.adapter = albumListAdapter
            albums!!.itemAnimator = DefaultItemAnimator()
        }
    }
}
