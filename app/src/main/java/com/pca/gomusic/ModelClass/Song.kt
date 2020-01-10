package com.pca.gomusic.ModelClass

import java.text.DecimalFormat
import java.util.*

class Song {
    var songId: Long = 0
        private set
    var albumId: Long = 0
        private set
    var dateAdded: Long = 0
        private set
    var size: Long = 0
        private set
    var name: String? = null
        private set
    var artist: String? = null
        private set
    var path: String? = null
        private set
    var durationLong: Long = 0
        private set
    var artistId: Long = 0
        private set

    constructor() : super()
    constructor(songId: Long, name: String?, artist: String?, path: String?, albumId: Long,
                dateAdded: Long, size: Long, duration: Long, artistId:Long) {
        this.songId = songId
        this.name = name
        this.artist = artist
        this.path = path
        this.dateAdded = dateAdded
        this.size = size
        this.albumId = albumId
        durationLong = duration
        this.artistId = artistId
    }

    fun compareToIgnoreCase(song: Song): Int {
        return name!!.compareTo(song.name!!, ignoreCase = true)
    }

    fun convertKbToMb(size: Long): String {
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

    fun convertingDuration(duration: Long): String {
        var minutes = duration / 1000 / 60
        val seconds = duration / 1000 % 60
        return if (minutes < 60) {
            String.format(Locale.getDefault(), "%01d:%02d", minutes, seconds)
        } else {
            val hours = minutes / 60
            minutes = minutes % 60
            String.format(Locale.getDefault(), "%d:%02d:%02d", hours, minutes, seconds)
        }
    }
}