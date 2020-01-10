package com.pca.gomusic.Activities


import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.AsyncTask
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.ProgressBar
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.appcompat.widget.Toolbar
import androidx.palette.graphics.Palette
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.perf.FirebasePerformance
import com.pca.gomusic.Adapters.ArtistListAdapter
import com.pca.gomusic.Classes.ListSongs
import com.pca.gomusic.Constant.Constant
import com.pca.gomusic.ModelClass.Song
import com.pca.gomusic.R
import com.pca.gomusic.utils.Utils
import java.io.File
import com.pca.gomusic.Services.PlayerService.Companion.ACTION_PLAY_ARTIST


class ArtistActivity : AppCompatActivity() {


    private var mAlertDialog: AlertDialog? = null
    private var playAll: FloatingActionButton? = null
    private var Name: AppCompatTextView? = null
    private var favoriteArtist: AppCompatImageView? = null
    private var bitmap: Bitmap? = null
    private var path: String? = null
    private var artistName: String? = ""
    private var toolbar: Toolbar? = null
    private lateinit var artist_list_songs: RecyclerView
    private lateinit var artistListAdapter: ArtistListAdapter
    private lateinit var artistListSongs: ArrayList<Song>
    private lateinit var loading:ProgressBar


    private fun initViews() {
        artist_list_songs = findViewById(R.id.artist)
        Name = findViewById(R.id.artist_name)
        Name!!.isSingleLine = true
        toolbar = findViewById(R.id.toolbar)
        playAll = findViewById(R.id.play_all)
        favoriteArtist = findViewById(R.id.artist_art)
       loading = findViewById(R.id.progress_bar)
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val sharedPref = PreferenceManager.getDefaultSharedPreferences(this)
        val currentTheme = sharedPref.getString(Constant.THEME, "default")
        when {
            currentTheme.equals("night") -> setTheme(R.style.AppTheme_MainDark)
            else -> setTheme(R.style.AppTheme)
        }

        setContentView(R.layout.activity_artist)

        initViews()

       when{
           currentTheme.equals("night" ) || currentTheme.equals("mimi") ->{
               toolbar!!.setNavigationIcon(R.drawable.ic_back_white)
           }else -> {
           toolbar!!.setNavigationIcon(R.drawable.ic_back_black)
       }
       }
        toolbar!!.setNavigationOnClickListener {
            finish()
            // overridePendingTransition(R.anim.slide_in_up, R.anim.slide_out_up);
        }

         artistName = intent.getStringExtra(Constant.ARTIST_NAME)
        Name!!.text = artistName
        val artistID = intent.getLongExtra(Constant.ARTIST_ID, 0)
        path = ListSongs.getAlbumArt(this, artistID)
        assert(artistName != null)

        // Fetching all songs related to artist name
        FetchArtistList().execute()

        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT) {
            getBitmap()
            Palette.from(bitmap!!).generate { palette ->
                assert(palette != null)
            //    val colors = getAvailableColor(palette!!)
             //   Name!!.setTextColor(colors[0])
            }
        }


        playAll!!.setOnClickListener {
            val playArtistTrace = FirebasePerformance.getInstance().newTrace("play_Artist_Trace")
            playArtistTrace.start()
            val playAllArtistSongs = Intent()
            playAllArtistSongs.action = ACTION_PLAY_ARTIST
            playAllArtistSongs.putExtra("name", artistName)
            playAllArtistSongs.putExtra("pos", 0)
            sendBroadcast(playAllArtistSongs)
            playArtistTrace.stop()
        }


        favoriteArtist!!.setOnClickListener { deveDialog() }
    }


    private fun fileExist(albumArtPath: String): Boolean {
        val imgFile = File(albumArtPath)
        return imgFile.exists()
    }


    private fun getBitmap() {
        if (path == null || !fileExist(path!!))
            bitmap = Utils(this)
                    .getBitmapOfVector(R.drawable._art000, 100, 200)
        else {
            val options = BitmapFactory.Options()
            options.inJustDecodeBounds = false
            options.inPreferredConfig = Bitmap.Config.RGB_565
            options.inDither = true
            bitmap = BitmapFactory.decodeFile(path.toString())
        }
    }


   /* private fun getAvailableColor(palette: Palette): IntArray {
        val temp = IntArray(3)
        when {
            palette.vibrantSwatch != null -> {
                temp[0] = palette.vibrantSwatch!!.rgb
                temp[1] = palette.vibrantSwatch!!.bodyTextColor
                temp[2] = palette.vibrantSwatch!!.titleTextColor
            }
            palette.darkVibrantSwatch != null -> {
                temp[0] = palette.darkVibrantSwatch!!.rgb
                temp[1] = palette.darkVibrantSwatch!!.bodyTextColor
                temp[2] = palette.darkVibrantSwatch!!.titleTextColor
            }
            palette.darkMutedSwatch != null -> {
                temp[0] = palette.darkMutedSwatch!!.rgb
                temp[1] = palette.darkMutedSwatch!!.bodyTextColor
                temp[2] = palette.darkMutedSwatch!!.titleTextColor
            }else -> {
                temp[0] = ContextCompat.getColor(this, R.color.dark)
                temp[1] = ContextCompat.getColor(this, android.R.color.white)
                temp[2] = -0x1a1a1b
            }
        }
        return temp
    }*/


    private fun deveDialog() {
        @SuppressLint("InflateParams") val inflater = layoutInflater.inflate(R.layout.under_development_dialog, null)
        val builder = AlertDialog.Builder(this)
        builder.setView(inflater)
                .setCancelable(true)
        mAlertDialog = builder.show()

        val ok = inflater.findViewById<AppCompatButton>(R.id.ok)
        ok.setOnClickListener { mAlertDialog!!.dismiss() }

    }


    override fun onBackPressed() {
        super.onBackPressed()
        //  overridePendingTransition(R.anim.slide_in_up, R.anim.slide_out_up);
    }



    @SuppressLint("StaticFieldLeak")
    inner class FetchArtistList: AsyncTask<Void, Void, ArrayList<Song>>() {

        override fun doInBackground(vararg params: Void?): ArrayList<Song> {
            val songList = java.util.ArrayList<Song>()
            val where = (MediaStore.Audio.Media.IS_MUSIC + "=1 AND "
                    + MediaStore.Audio.Media.ARTIST + "='" + artistName.toString().replace("'", "''") + "'")
            val orderBy = MediaStore.Audio.Media.DATE_ADDED + " DESC"
            val projection = arrayOf(MediaStore.Audio.Media.TITLE, MediaStore.Audio.Media._ID, MediaStore.Audio.Media.ARTIST, MediaStore.Audio.Media.DATA, MediaStore.Audio.Media.ALBUM_ID, MediaStore.Audio.Media.ALBUM, MediaStore.Audio.Media.DATE_ADDED, MediaStore.Audio.Media.DURATION, MediaStore.Audio.Media.SIZE,MediaStore.Audio.Media.ARTIST_ID)
            val musicCursor = contentResolver.query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
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


        override fun onPostExecute(result: ArrayList<Song>?) {
            super.onPostExecute(result)
            loading.visibility = View.GONE
            artistListSongs = result!!
            artistListAdapter = ArtistListAdapter(this@ArtistActivity, artistListSongs)
            artist_list_songs.layoutManager = LinearLayoutManager(applicationContext)
            artist_list_songs.itemAnimator = DefaultItemAnimator()
            artist_list_songs.addItemDecoration(DividerItemDecoration(applicationContext, LinearLayoutManager.VERTICAL))
            artist_list_songs.adapter = artistListAdapter
        }
    }
}
