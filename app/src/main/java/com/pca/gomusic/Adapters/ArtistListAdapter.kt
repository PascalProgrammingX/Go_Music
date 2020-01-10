package com.pca.gomusic.Adapters

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.MediaStore
import android.provider.Settings
import android.view.*
import android.widget.PopupMenu
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.AppCompatTextView
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.RecyclerView
import com.pca.gomusic.Constant.Constant
import com.pca.gomusic.Fragments.SongsListFragment
import com.pca.gomusic.ModelClass.Song
import com.pca.gomusic.R
import com.pca.gomusic.Services.PlayerService
import com.pca.gomusic.utils.Utils
import java.io.File
import java.util.*

class ArtistListAdapter(var activity: Activity, var songs: ArrayList<Song>) : RecyclerView.Adapter<ArtistListAdapter.MyViewHolder>() {
    private val EMPTY_VIEW = 1111
    private var mainView: View? = null
    private lateinit var deleteDialog: AlertDialog

    inner class MyViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var title: AppCompatTextView = view.findViewById(R.id.title)
        var duration: AppCompatTextView = view.findViewById(R.id.duration)
        var size: AppCompatTextView = view.findViewById(R.id.size)
        var count: AppCompatTextView = view.findViewById(R.id.album_item_count)
        var songMenu: AppCompatButton = view.findViewById(R.id.optionsMenu)

        init {
            mainView = view
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val mainView: View
        mainView = if (viewType == EMPTY_VIEW) {
            layoutInflater.inflate(R.layout.empty_list, parent, false)
        } else {
            layoutInflater.inflate(R.layout.artist_list_item, parent, false)
        }
        return MyViewHolder(mainView)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        if (getItemViewType(position) != EMPTY_VIEW) {
            val model = songs[holder.adapterPosition]
            holder.title.text = model.name
            holder.count.text = (position + 1).toString()
            holder.duration.text = model.convertingDuration(model.durationLong)
            val mm = model.convertKbToMb(model.size)
            holder.size.text = mm

            val sharedPref = PreferenceManager.getDefaultSharedPreferences(activity)
            val currentTheme = sharedPref.getString(Constant.THEME, "default")
            when{
                currentTheme.equals("mimi") || currentTheme.equals("cyran") -> holder.songMenu.setBackgroundResource(R.drawable.ic_options_menu_black)
                else -> holder.songMenu.setBackgroundResource(R.drawable.ic_options_menu_default)
            }

            holder.songMenu.setOnClickListener {
                val wrapper: Context = ContextThemeWrapper(activity, R.style.popupMenuStyle)
                val popupMenu = PopupMenu(wrapper, holder.songMenu)
                popupMenu.setOnMenuItemClickListener { item: MenuItem ->
                    when (item.itemId) {
                        R.id.play_next -> {
                            val intent = Intent()
                            intent.putExtra("songId", songs[position].songId)
                            intent.action = PlayerService.ACTION_ADD_QUEUE
                            activity.sendBroadcast(intent)
                            return@setOnMenuItemClickListener true
                        }

                        R.id.delete -> {
                            @SuppressLint("InflateParams") val deleteInflater = LayoutInflater.from(activity).inflate(R.layout.delete_dialog, null)
                            val deleteBuilder:AlertDialog.Builder = AlertDialog.Builder(Objects.requireNonNull(activity))
                            deleteBuilder.setView(deleteInflater).setCancelable(true)
                            deleteDialog = deleteBuilder.show()
                            val deleteSongTitle = deleteInflater.findViewById<TextView>(R.id.delete_song_title)
                            deleteSongTitle.text = activity.resources.getString(R.string.sure_to_delete) + "\n" + "( " + songs[position].name + " )"
                            deleteInflater.findViewById<View>(R.id.ok).setOnClickListener {
                                val songFile = File(songs[position].path.toString())
                                if (songFile.delete()) {
                                    activity.contentResolver.delete(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                                            MediaStore.MediaColumns._ID + "='" + songs[position].songId + "'", null)
                                    songs.removeAt(position)
                                    notifyItemRemoved(position)
                                    val deleted = Intent()
                                    deleted.putExtra("size", songs.size)
                                    deleted.action = SongsListFragment.ACTION_DELETE
                                    deleted.action = AlbumListAdapter.UPDATE_ADAPTER_LIST
                                    activity.sendBroadcast(deleted)
                                    notifyDataSetChanged()
                                    deleteDialog.dismiss()
                                    Toast.makeText(activity, R.string.deleted, Toast.LENGTH_SHORT).show()
                                } }
                            deleteInflater.findViewById<View>(R.id.close).setOnClickListener { deleteDialog.dismiss() }
                            return@setOnMenuItemClickListener true
                        }

                        R.id.share -> {
                            Utils(activity).shareSongFile(songs, position)
                            return@setOnMenuItemClickListener true
                        }

                        R.id.make_ringtone -> {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
                                val canWrite = Settings.System.canWrite(activity)
                                if (!canWrite){
                                    activity.startActivity(Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS))
                                }else{
                                    Utils(activity).setRingtone(model.songId.toInt())
                                }
                            }else {
                                Utils(activity).setRingtone(model.songId.toInt())
                            }
                            return@setOnMenuItemClickListener true
                        }
                    }
                    true
                }
                popupMenu.inflate(R.menu.song_menu)
                popupMenu.show()
            }
            mainView!!.setOnClickListener {
                val i = Intent()
                i.action = PlayerService.ACTION_PLAY_ALL_ALBUM_SONGS
                i.putExtra("songId", songs[position].songId)
                i.putExtra("albumId", songs[position].albumId)
                activity.sendBroadcast(i)
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        return if (songs.size == 0) {
            EMPTY_VIEW
        } else super.getItemViewType(position)
    }

    override fun getItemCount(): Int {
        return if (songs.size > 0) songs.size else 1
    }

}