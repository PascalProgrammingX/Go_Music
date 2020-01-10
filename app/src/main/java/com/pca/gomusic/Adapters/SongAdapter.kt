package com.pca.gomusic.Adapters


import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.*
import android.graphics.Typeface
import android.os.Build
import android.provider.MediaStore
import android.provider.Settings
import android.view.*
import android.widget.PopupMenu
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.crashlytics.android.answers.Answers
import com.crashlytics.android.answers.CustomEvent
import com.google.firebase.perf.FirebasePerformance
import com.pca.gomusic.Constant.Constant
import com.pca.gomusic.Fragments.SongsListFragment
import com.pca.gomusic.ModelClass.Song
import com.pca.gomusic.R
import com.pca.gomusic.Services.PlayerService
import com.pca.gomusic.ui.BubbleTextGetter
import com.pca.gomusic.utils.Utils
import es.claucookie.miniequalizerlibrary.EqualizerView
import java.io.File
import java.util.ArrayList
import java.util.Objects


class SongAdapter(private val songs: ArrayList<Song>, private val context: Context) : RecyclerView.Adapter<SongAdapter.MyViewHolder>(), BubbleTextGetter {

    private val EMPTY_VIEW = 1111
    private var mainView: View? = null
    private var mPositionID: Long = 0
    private var playing: Boolean = false


    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            mPositionID = intent.getLongExtra("songId", 0)
            playing = intent.getBooleanExtra("running", false)
            if (intent.action.equals( ACTION_PLAY_PAUSE) || intent.action.equals( ACTION_ID_RECIEVED)) {
             notifyDataSetChanged()
            }
        }
    }


    inner class MyViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        var title: AppCompatTextView = view.findViewById(R.id.title)
        var artist: AppCompatTextView = view.findViewById(R.id.artist)
        var duration: AppCompatTextView = view.findViewById(R.id.duration)
        var size: AppCompatTextView = view.findViewById(R.id.size)
        var albumArt: AppCompatImageView = view.findViewById(R.id.albumArt)
        var equalizerView: EqualizerView = view.findViewById(R.id.equalizer)
        var songMenu: AppCompatButton = view.findViewById(R.id.optionsMenu)

        init {
            mainView = view
            val filter = IntentFilter()
            filter.addAction(ACTION_ID_RECIEVED)
            filter.addAction(ACTION_PLAY_PAUSE)
            context.registerReceiver(receiver, filter)
        }
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val mainView: View
           mainView = layoutInflater.inflate(R.layout.song_list_style, parent, false)

        return MyViewHolder(mainView)
    }


    override fun getTextToShowInBubble(pos: Int): String {
        return try {
            songs[pos].name!![0].toString()
        } catch (e: Exception) {
            e.printStackTrace()
            "-"
        }

    }


    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        if (getItemViewType(position) != EMPTY_VIEW) {
            val model = songs[holder.adapterPosition]
            holder.title.text = model.name
            holder.duration.text = model.convertingDuration(model.durationLong)
            Glide.with(context).load(Utils.getAlbumArtUri(model.albumId)).placeholder(R.drawable._art000).into(holder.albumArt)

            if (model.songId == mPositionID && playing) {
                    holder.equalizerView.animateBars()
                    holder.equalizerView.visibility = View.VISIBLE
                    holder.albumArt.visibility = View.GONE

                if (Build.VERSION.SDK_INT>=Build.VERSION_CODES.O){
                    holder.title.typeface = Typeface.DEFAULT_BOLD
                }
            }else{
                holder.title.typeface = Typeface.DEFAULT
                holder.equalizerView.visibility = View.GONE
                holder.equalizerView.stopBars()
                holder.albumArt.visibility = View.VISIBLE
            }

            val mm = model.convertKbToMb(model.size)
            holder.size.text = mm
            if (model.artist == "<unknown>") {
                holder.artist.setText(R.string.unkown_artist)
            } else {
                holder.artist.text = model.artist
            }

            val sharedPref = PreferenceManager.getDefaultSharedPreferences(context)
            val currentTheme = sharedPref.getString(Constant.THEME, "default")
            when{
                currentTheme.equals("mimi") ||  currentTheme.equals("cyran") -> holder.songMenu.setBackgroundResource(R.drawable.ic_options_menu_black)
                else -> holder.songMenu.setBackgroundResource(R.drawable.ic_options_menu_default)
            }

            holder.songMenu.setOnClickListener {
                val wrapper = ContextThemeWrapper(context, R.style.popupMenuStyle)
                val popupMenu = PopupMenu(wrapper, holder.songMenu)
                popupMenu.setOnMenuItemClickListener popupMenu@{ item ->
                    when (item.itemId) {
                        R.id.play_next -> {
                            val intent = Intent()
                            intent.putExtra("songId", songs[position].songId)
                            intent.action = PlayerService.ACTION_ADD_QUEUE
                            context.sendBroadcast(intent)
                            Toast.makeText(context, "${songs[position].name}  added to queue", Toast.LENGTH_LONG).show()
                        }

                        R.id.delete -> {
                            @SuppressLint("InflateParams")
                            val deleteInflater = LayoutInflater.from(context).inflate(R.layout.delete_dialog, null)
                            val deleteBuilder = AlertDialog.Builder(Objects.requireNonNull(context))
                            deleteBuilder.setView(deleteInflater).setCancelable(true)
                            val optionDeleteAlertDialog = deleteBuilder.show()

                            val deleteSongTitle = deleteInflater.findViewById<TextView>(R.id.delete_song_title)
                            deleteSongTitle.text = context.resources.getString(R.string.sure_to_delete) + "\n" + "( " + songs[position].name + " )"
                            deleteInflater.findViewById<View>(R.id.ok).setOnClickListener {
                                val songFile = File(songs[position].path.toString())
                                if (songFile.delete()) {
                                    context.contentResolver.delete(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                                            MediaStore.MediaColumns._ID + "='" + songs[position].songId + "'", null)
                                    songs.removeAt(position)
                                    notifyItemRemoved(position)

                                    val deleted = Intent()
                                    deleted.putExtra("size", songs.size)
                                    deleted.action = SongsListFragment.ACTION_DELETE
                                    deleted.action = AlbumListAdapter.UPDATE_ADAPTER_LIST
                                    context.sendBroadcast(deleted)

                                    notifyDataSetChanged()
                                    optionDeleteAlertDialog.dismiss()
                                    Toast.makeText(context, R.string.deleted, Toast.LENGTH_SHORT).show()
                                }
                            }

                            deleteInflater.findViewById<View>(R.id.close).setOnClickListener { optionDeleteAlertDialog.dismiss() }
                        }

                        R.id.share -> {
                            Utils(context).shareSongFile(songs, position)
                        }

                        R.id.make_ringtone -> {
                            val ringtoneTrace = FirebasePerformance.getInstance().newTrace("make_ringtone_trace")
                            ringtoneTrace.start()
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
                                val canWrite = Settings.System.canWrite(context)
                                if (!canWrite){
                                    context.startActivity(Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS))
                                }else{
                                    Utils(context).setRingtone(model.songId.toInt())
                                }
                            }else {
                                Utils(context).setRingtone(model.songId.toInt())
                            }
                            ringtoneTrace.stop()
                        }
                    }; true
                }
                popupMenu.inflate(R.menu.song_menu)
                popupMenu.show()
            }


            mainView!!.setOnClickListener {
                val myTrace = FirebasePerformance.getInstance().newTrace("song_click_trace")
                myTrace.start()
                    val playerIntent = Intent()
                    playerIntent.action = PlayerService.ACTION_PLAY_ALL_SONGS
                    playerIntent.putExtra("songId", songs[holder.adapterPosition].songId)
                    context.sendBroadcast(playerIntent)
                Answers.getInstance().logCustom( CustomEvent("Played Song  ${songs[holder.adapterPosition].name}")
                        .putCustomAttribute("Artist name ", songs[holder.adapterPosition].artist))
                myTrace.stop()
            }
        }

    }


    override fun getItemViewType(position: Int): Int {
        return if (songs.isEmpty()) {
            EMPTY_VIEW
        } else super.getItemViewType(position)
    }


    override fun getItemCount(): Int {
        return if (songs.size > 0) songs.size else 1
    }


   /* override fun onViewRecycled(holder: MyViewHolder) {
        super.onViewRecycled(holder)
        holder.albumArt.setImageURI(null)

    }*/


    companion object {
        const val ACTION_ID_RECIEVED = "ACTION_ID_RECIEVED"
        const val ACTION_PLAY_PAUSE = "ACTION_PLAY_PAUSE"
    }

}