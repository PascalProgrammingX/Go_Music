package com.pca.gomusic.Adapters

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.pca.gomusic.ModelClass.VideoViewInfo
import com.pca.gomusic.R
import java.text.DecimalFormat
import java.util.*

class VideoGalleryAdapter(videoList: ArrayList<VideoViewInfo>, context: Context) : RecyclerView.Adapter<VideoGalleryAdapter.MyViewHolder>() {
    private val videos: MutableList<VideoViewInfo>
    private val context: Context

    inner class MyViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var title: AppCompatTextView = view.findViewById(R.id.title)
        var size: AppCompatTextView = view.findViewById(R.id.size)
        var imageView: AppCompatImageView = view.findViewById(R.id.imageView)

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.video_list_item, parent, false)
        return MyViewHolder(itemView)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val vp = videos[position]
        holder.title.text = vp.title + ".mp4"
        holder.size.text = convertKbToMb(vp.size)
        try {
            val imageFile = vp.thumbPath
            Glide.with(context)
                    .load(imageFile)
                    .into(holder.imageView)
        } catch (ignored: Exception) {
        }
    }

    private fun convertKbToMb(size: Long): String {
        val mB: String
        val m = size / 1024.0
        val g = size / 1048576.0
        val t = size / 1073741824.0
        val decimalFormat = DecimalFormat("0.0")
        mB = if (t > 1) {
            decimalFormat.format(t) + "TB"
        } else if (g > 1) {
            decimalFormat.format(g) + "MB"
        } else if (m > 1) {
            decimalFormat.format(m) + "KB"
        } else {
            decimalFormat.format(size) + "GB"
        }
        return mB
    }

    //deleting an item at position and notify changes.
    fun delete(position: Int) {
        val model = videos.removeAt(position)
        videos.remove(model)
        notifyItemRemoved(position)
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int {
        return videos.size
    }

    init {
        videos = videoList
        this.context = context
    }
}