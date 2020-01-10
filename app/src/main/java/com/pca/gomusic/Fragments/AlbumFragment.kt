package com.pca.gomusic.Fragments


import android.annotation.SuppressLint
import android.app.Activity
import android.os.AsyncTask
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.pca.gomusic.Adapters.AlbumAdapter
import com.pca.gomusic.ModelClass.Album
import com.pca.gomusic.R
import com.pca.gomusic.ui.SpaceItemDecoration


class AlbumFragment : Fragment() {

    private lateinit var albumGrid:RecyclerView
    private lateinit var albumAdapter:AlbumAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_album, container, false)

         albumGrid = view.findViewById(R.id.album_list)
        FetchAlbum().execute()

        return view
    }



    @SuppressLint("StaticFieldLeak")
    inner class FetchAlbum: AsyncTask<Void, Void, ArrayList<Album>>(){

        override fun doInBackground(vararg params: Void?): ArrayList<Album> {
            val albumList = java.util.ArrayList<Album>()
            val orderBy = MediaStore.Audio.Albums.ALBUM
            val projection = arrayOf(MediaStore.Audio.Albums._ID,
                    MediaStore.Audio.Albums.ALBUM,
                    MediaStore.Audio.Albums.ARTIST,
                    MediaStore.Audio.Albums.ALBUM_ART,
                    MediaStore.Audio.Albums.NUMBER_OF_SONGS)
            val musicCursor = context?.contentResolver?.query(MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI, projection, null, null, orderBy)
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


        override fun onPostExecute(result: ArrayList<Album>?) {
            super.onPostExecute(result)
            albumAdapter = context?.let { AlbumAdapter(it, context as Activity, result!! )}!!
            albumGrid.layoutManager = GridLayoutManager(context, 2)
            albumGrid.adapter = albumAdapter
        }
    }

}
