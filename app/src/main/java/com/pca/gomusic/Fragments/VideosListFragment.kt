package com.pca.gomusic.Fragments


import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.content.Intent
import android.media.ThumbnailUtils
import android.os.AsyncTask
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.pca.gomusic.Activities.VideoPlayerActivity
import com.pca.gomusic.Adapters.VideoGalleryAdapter
import com.pca.gomusic.ModelClass.VideoViewInfo
import com.pca.gomusic.R
import com.pca.gomusic.ui.RecyclerTouchListener
import java.io.File
import java.util.*


class VideosListFragment : Fragment() {

    private var vData: String? = null
    private var videoGalleryAdapter: VideoGalleryAdapter? = null
    private lateinit var videoView:RecyclerView
    private var videos: ArrayList<VideoViewInfo>? = null
    private var mAlertDialog: AlertDialog? = null
    private var positionDelete: Int = 0
    private lateinit var loading:ProgressBar


    @SuppressLint("SetTextI18n")
    @TargetApi(Build.VERSION_CODES.KITKAT)
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_videos_list, container, false)

        //Fetching All Videos using AsyncTask to keep UI Thread Save from ANR.
        //this is a heavy task.
        FetchVideos().execute()

        videoView = view.findViewById(R.id.videoGrid)
        loading = view.findViewById(R.id.loading)

        videoView.addOnItemTouchListener(RecyclerTouchListener(context, videoView, object:RecyclerTouchListener.ClickListener {
            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            override fun onClick(view:View, position:Int) {
                vData = videos!![position].filePath
                val videoActivity = Intent(activity, VideoPlayerActivity::class.java)
                videoActivity.putExtra(VideoPlayerActivity.VIDEO_DATA, vData)
                startActivity(videoActivity)
            }

            override fun onLongClick(view: View?, position: Int) {
                positionDelete = position
                val videoTitle = videos!![position].title
                delete(videoTitle)
            }
        }))



        return view
    }


    @SuppressLint("SetTextI18n")
    private fun delete(title: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            @SuppressLint("InflateParams") val inflater = layoutInflater.inflate(R.layout.delete_dialog, null)
            val builder = context?.let { AlertDialog.Builder(it) }
            builder?.setView(inflater)
                    ?.setCancelable(true)
            mAlertDialog = builder?.show()

            val deleteTitle = inflater.findViewById<TextView>(R.id.delete_song_title)
            deleteTitle.text = "$title.mp4"

            val ok = inflater.findViewById<TextView>(R.id.ok)
            ok.setOnClickListener {
                mAlertDialog!!.dismiss()
                val file = File(videos!![positionDelete].filePath)
                deleteVideo(file, positionDelete)
            }

            val close = inflater.findViewById<TextView>(R.id.close)
            close.setOnClickListener { mAlertDialog!!.dismiss() }
        }
    }


    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private fun deleteVideo(file: File, position: Int) {
        val contentResolver = (context)?.contentResolver
        val uri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI
        val where = MediaStore.Video.Media.DATA + "=?"
        val selectionArgs = arrayOf(file.absolutePath)
        contentResolver?.delete(uri, where, selectionArgs)
        Toast.makeText(context, R.string.deleted, Toast.LENGTH_SHORT).show()
        videoGalleryAdapter!!.delete(position)
    }


    @SuppressLint("StaticFieldLeak")
    inner class FetchVideos :AsyncTask<Void, Void, ArrayList<VideoViewInfo>>(){

        override fun doInBackground(vararg params: Void?): ArrayList<VideoViewInfo> {
            val videos = ArrayList<VideoViewInfo>()
            val mediaColumns = arrayOf(MediaStore.Video.Media._ID, MediaStore.Video.Media.DATA, MediaStore.Video.Media.TITLE, MediaStore.Video.Media.SIZE)
            val contentResolver = context!!.contentResolver
            val uri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI
            val cursor = contentResolver.query(uri, mediaColumns, MediaStore.Video.Media._ID, null, MediaStore.Video.Media.DATE_ADDED + " DESC")

            if (cursor != null && cursor.moveToFirst()) {
                do {
                    val newVVI = VideoViewInfo()
                    newVVI.filePath = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATA))
                    newVVI.title = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.TITLE))
                    newVVI.thumbPath = ThumbnailUtils.createVideoThumbnail(newVVI.filePath, MediaStore.Video.Thumbnails.FULL_SCREEN_KIND)
                    newVVI.size = cursor.getLong(cursor.getColumnIndex(MediaStore.Video.Media.SIZE))
                    videos.add(newVVI)
                } while (cursor.moveToNext())
                cursor.close()
            }
            return videos
        }

        override fun onPostExecute(result: ArrayList<VideoViewInfo>?) {
            super.onPostExecute(result)
            loading.visibility = View.GONE
            videos  = result
            videoGalleryAdapter = VideoGalleryAdapter(videos!!, context!!)
            videoView.layoutManager = GridLayoutManager(context, 2)
                videoView.adapter = videoGalleryAdapter
        }
    }
}



