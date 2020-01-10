package com.pca.gomusic.Fragments


import android.annotation.SuppressLint
import android.os.AsyncTask
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.pca.gomusic.Adapters.ArtistAdapter
import com.pca.gomusic.ModelClass.Artist
import com.pca.gomusic.R


class ArtistFragment : Fragment() {


    private var artistAdapter: ArtistAdapter? = null
    private var artistListView: RecyclerView? = null
    private lateinit var artistList: ArrayList<Artist>


    private fun init(view: View) {
        artistListView = view.findViewById(R.id.artist_gridView)
    }


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_artist, container, false)
        init(view)
        FetchArtist().execute()
        return view
    }



    @SuppressLint("StaticFieldLeak")
    inner class FetchArtist: AsyncTask<Void, Void, ArrayList<Artist>>() {

        override fun doInBackground(vararg params: Void?): ArrayList<Artist> {
            val albumList = java.util.ArrayList<Artist>()
            val orderBy = MediaStore.Audio.Artists.ARTIST
            val selection = MediaStore.Audio.Artists._ID
            val projection = arrayOf(MediaStore.Audio.Artists.ARTIST,
                    MediaStore.Audio.Artists._ID,
                    MediaStore.Audio.Artists.NUMBER_OF_ALBUMS,
                    MediaStore.Audio.Artists.NUMBER_OF_TRACKS)
            val musicCursor = context?.contentResolver?.query(MediaStore.Audio.Artists.EXTERNAL_CONTENT_URI, projection, selection, null, orderBy)

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


        override fun onPostExecute(result: ArrayList<Artist>?) {
            super.onPostExecute(result)
            artistList = result!!
            artistAdapter = context?.let { ArtistAdapter(it, artistList) }
            artistListView?.layoutManager = LinearLayoutManager(context)
            artistListView!!.adapter = artistAdapter
        }
    }


}
