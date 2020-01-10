package com.pca.gomusic.Adapters

import android.annotation.SuppressLint
import android.app.Activity
import android.app.ActivityOptions
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.pca.gomusic.Activities.AlbumActivity
import com.pca.gomusic.Classes.ListSongs
import com.pca.gomusic.Constant.Constant
import com.pca.gomusic.ModelClass.Album
import com.pca.gomusic.R
import com.pca.gomusic.utils.Utils
import es.claucookie.miniequalizerlibrary.EqualizerView
import java.util.*


class AlbumAdapter(private val context: Context, private val activity: Activity,  private val albums: ArrayList<Album>) :
        RecyclerView.Adapter<AlbumAdapter.MyViewHolder>(){


    private var bitmap: Bitmap? = null
    private var path: String? = null
    private var albumID:Long = 0
    private var playing:Boolean = false

    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            albumID = intent.getLongExtra("albumID", 0)
            playing = intent.getBooleanExtra("running", false)
            if (intent.action.equals( ACTION_PLAYING_ALBULM) && playing) {
               notifyDataSetChanged()
            }
        }
    }




    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val mainView = layoutInflater.inflate(R.layout.album_grid_item, parent, false)
        return MyViewHolder(mainView)
    }

    override fun getItemCount(): Int {
      return  albums.size
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val album = albums[position]
        holder.name.text = album.albumTitle
        holder.artist.text = album.albumArtist + "  " + album.songNumber

        setAlbumImg(holder.albumArt, album.albumId)
        getBitmap()

        if (playing && album.albumId == albumID){
            holder.eq.animateBars()
            holder.eq.visibility = View.VISIBLE
        }else{
            holder.eq.stopBars()
            holder.eq.visibility = View.GONE
        }

        // Removing the bgColor of Album items.
       /*  Palette.from(bitmap!!).generate { palette ->
             assert(palette != null)
             val colors = Utils(context).getAvailableColor(palette!!)
             holder.bottomBg.setBackgroundColor(colors[0])
             holder.name.setTextColor(colors[1])
             holder.artist.setTextColor(colors[2])
         }*/

        holder.view.setOnClickListener {
            val intent = Intent(context, AlbumActivity::class.java)
            intent.putExtra(Constant.ALBUM_ID, albums[position].albumId)
            intent.putExtra(Constant.ALBUM_NAME, album.albumTitle)
            val options: ActivityOptions
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                options = ActivityOptions.makeSceneTransitionAnimation(activity, holder.albumArt, "robot")
               context.startActivity(intent, options.toBundle())
            } else {
                context.startActivity(intent)
            }
        }
    }


    inner class MyViewHolder(var view: View) : RecyclerView.ViewHolder(view) {
        var name: AppCompatTextView = view.findViewById(R.id.album_list_name)
        var artist: AppCompatTextView = view.findViewById(R.id.album_list_artist)
        var bottomBg: LinearLayoutCompat = view.findViewById(R.id.album_list_bottom)
        var albumArt: AppCompatImageView = view.findViewById(R.id.album_list_img)
        var eq: EqualizerView = view.findViewById(R.id.equalizer)


        init {
            val filter = IntentFilter()
            filter.addAction(ACTION_PLAYING_ALBULM)
            context.registerReceiver(receiver, filter)
        }

    }





    private fun setAlbumImg(img: ImageView, ID: Long) {
        path = ListSongs.getAlbumArt(context, ID)
        Glide.with(context)
                .load(Utils.getAlbumArtUri(ID))
                .placeholder(R.drawable._art000)
                .into(img)
    }


    private fun getBitmap() {
        if (path == null || !Utils(context).fileExist(path!!))
            bitmap = Utils(context)
                    .getBitmapOfVector(R.drawable._art000, 100, 200)
        else {
            val options = BitmapFactory.Options()
            options.inJustDecodeBounds = false
            options.inPreferredConfig = Bitmap.Config.RGB_565
            options.inDither = true
            bitmap = BitmapFactory.decodeFile(path)
        }
    }



    companion object {
        const val ACTION_PLAYING_ALBULM = "ACTION_PLAYING_ALBULM"
    }
}
