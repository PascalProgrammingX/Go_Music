package com.pca.gomusic.Fragments

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.AsyncTask
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.google.android.material.snackbar.Snackbar
import com.pca.gomusic.Adapters.SongAdapter
import com.pca.gomusic.Classes.ListSongs
import com.pca.gomusic.ModelClass.Song
import com.pca.gomusic.R
import com.pca.gomusic.ui.FastScroller
import kotlinx.android.synthetic.main.content_main_frame.*
import kotlinx.coroutines.runBlocking
import java.util.ArrayList
import java.util.Objects


class SongsListFragment : Fragment() {


    private var songAdapter: SongAdapter? = null
    private var songArrayList: ArrayList<Song>? = null
    private var songrecycler: RecyclerView? = null
    private var mFastScroller: FastScroller? = null
    private var refreshList: SwipeRefreshLayout? = null
    private var mAdView: AdView? = null
    private var selection: String? = ""
    private lateinit var loading:  ProgressBar

    private var filter: IntentFilter? = null


    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {

            val searchWord = intent.getStringExtra("search")

            when (Objects.requireNonNull<String>(intent.action)) {
                ACTION_SEARCH -> {
                    songArrayList!!.clear()
                    songArrayList!!.addAll(ListSongs.getSongList(Objects.requireNonNull<Context>(getContext()), searchWord))
                    songAdapter!!.notifyDataSetChanged()

                    val snackbar = Snackbar.make(loading, "Pull down to refresh...", Snackbar.LENGTH_INDEFINITE)
                    snackbar.setAction("Refresh") {
                        refreshList?.isRefreshing = true
                        FetchData().execute()
                        refreshList!!.isRefreshing = false
                    }
                    snackbar.show()
                }
            }
        }
    }


    override fun onDestroy() {
        super.onDestroy()
        Objects.requireNonNull<Context>(context).unregisterReceiver(receiver)
    }


    override fun onResume() {
        super.onResume()
        Objects.requireNonNull<Context>(context).registerReceiver(receiver, filter)
    }


    private fun initViews(view: View) {
        mFastScroller = view.findViewById(R.id.fast_scroller)
        refreshList = view.findViewById(R.id.refresh)
        mAdView = view.findViewById(R.id.adView)
        songrecycler = view.findViewById(R.id.list)
        loading = view.findViewById(R.id.loading)

        filter = IntentFilter()
        filter!!.addAction(ACTION_DELETE)
        filter!!.addAction(ACTION_SEARCH)
        Objects.requireNonNull<Context>(context).registerReceiver(receiver, filter)
    }


    @SuppressLint("SetTextI18n")
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_songs_list, container, false)

        initViews(view)

        //Fetching All Songs Using AsyncTask so as not to block UI Thread.
        runBlocking {
            FetchData().execute()
        }

        refreshList!!.setOnRefreshListener {
           FetchData().execute()
            refreshList!!.isRefreshing = false
        }


        val adRequest = AdRequest.Builder().build()
        mAdView!!.loadAd(adRequest)
        mAdView!!.adListener = object : AdListener(){
            override fun onAdLoaded() {
                mAdView!!.visibility = View.VISIBLE
            }
        }
        return view
    }


    companion object {
        const val ACTION_DELETE = "ACTION_DELETE"
        const val ACTION_SEARCH = "ACTION_SEARCH"
    }


    @SuppressLint("StaticFieldLeak")
    inner  class  FetchData :AsyncTask<String, ArrayList<Song>, ArrayList<Song>>() {

        override fun doInBackground(vararg params: String?): ArrayList<Song> {
            val songList = ArrayList<Song>()
          //  val orderBy = MediaStore.Audio.Media.DATE_ADDED +" DESC"
            val orderBy = MediaStore.Audio.Media.DEFAULT_SORT_ORDER
            var selectionArgs: Array<String>? = null
            if (params.isNotEmpty()) {
                selection = "title LIKE ?"
                selectionArgs = arrayOf("%$params%")
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
            val musicCursor = context?.contentResolver?.query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
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
            } ; return songList
        }


        override fun onPostExecute(result: ArrayList<Song>?) {
            super.onPostExecute(result)
            loading.visibility = View.GONE
            songArrayList = result
            songAdapter = result?.let { context?.let { it1 -> SongAdapter(it, it1) } }
            songrecycler!!.adapter = songAdapter
            mFastScroller!!.setRecyclerView(songrecycler!!)
            songrecycler!!.layoutManager = LinearLayoutManager(context)
            songrecycler!!.itemAnimator = DefaultItemAnimator()
        }
    }

}


