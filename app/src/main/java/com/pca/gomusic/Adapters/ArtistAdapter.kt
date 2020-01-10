package com.pca.gomusic.Adapters


import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.recyclerview.widget.RecyclerView
import com.pca.gomusic.Activities.ArtistActivity
import com.pca.gomusic.Constant.Constant
import com.pca.gomusic.ModelClass.Artist
import com.pca.gomusic.R
import java.util.ArrayList


class ArtistAdapter(private val context: Context,  private val artists: ArrayList<Artist>) : RecyclerView.Adapter<ArtistAdapter.MyViewHolder>(){


    private var artistId:Long = 0
    private var playing:Boolean = false


    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            artistId = intent.getLongExtra("artistId", 0)
            playing = intent.getBooleanExtra("running", false)
            if (intent.action.equals( ACTION_PLAYING_ARTIST) && playing) {
                notifyDataSetChanged()
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
         val mainView = layoutInflater.inflate(R.layout.artist_grid_item, parent, false)
        return MyViewHolder(mainView)
    }

    override fun getItemCount(): Int {
        return  artists.size
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val artist = artists[position]
        holder.name.text = artist.artistName
        if (artist.numberOfSongs > 1) {
            holder.numberOfSongsTextView.text = artist.numberOfSongs.toString() + "  " + "tracks"
        } else {
            holder.numberOfSongsTextView.text = artist.numberOfSongs.toString() + "  " + context.resources.getString(R.string.Songs)
        }


        if (playing && artist.artistId == artistId){
            holder.active.visibility = View.VISIBLE
        }else{
            holder.active.visibility = View.GONE
        }

        holder.main.setOnClickListener {
            val intent = Intent(context, ArtistActivity::class.java)
            intent.putExtra(Constant.ARTIST_NAME, artists[position].artistName)
            intent.putExtra(Constant.ARTIST_ID, artist.artistId)
            context.startActivity(intent)
        }

    }




    inner class MyViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var name: AppCompatTextView = view.findViewById(R.id.artist_Name)
        var numberOfSongsTextView: AppCompatTextView = view.findViewById(R.id.number_of_songs)
        var active: AppCompatImageView = view.findViewById(R.id.active)
        var main  = view


        init {
            val filter = IntentFilter()
            filter.addAction(ACTION_PLAYING_ARTIST)
            context.registerReceiver(receiver, filter)
        }

    }



    companion object {
        const val ACTION_PLAYING_ARTIST = "ACTION_PLAYING_ARTIST"
    }


}

