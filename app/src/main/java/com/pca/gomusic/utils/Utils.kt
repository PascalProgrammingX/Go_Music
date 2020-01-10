package com.pca.gomusic.utils

import android.content.ContentUris
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Toast
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat
import androidx.palette.graphics.Palette
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.perf.FirebasePerformance
import com.pca.gomusic.ModelClass.Song
import com.pca.gomusic.R
import java.io.File
import java.util.*


class Utils(private val context: Context) {




    fun fileExist(albumArtPath: String): Boolean {
        val imgFile = File(albumArtPath)
        return imgFile.exists()
    }

    /*private void animateEnter(View mainView, int position) {
        mainView.setAlpha(0f);
        mainView.setTranslationY(800.0f);
        mainView.animate()
                .setInterpolator(new FastOutSlowInInterpolator())
                .translationY(0.0f)
                .alpha(1.0f)
                .setDuration(500)
                .setStartDelay(10 + (position * 100))
                .start();
    }*/
    fun getAvailableColor(palette: Palette): IntArray {
        val temp = IntArray(3)
        if (palette.vibrantSwatch != null) {
            temp[0] = palette.vibrantSwatch!!.rgb
            temp[1] = palette.vibrantSwatch!!.bodyTextColor
            temp[2] = palette.vibrantSwatch!!.titleTextColor
        } else if (palette.darkVibrantSwatch != null) {
            temp[0] = palette.darkVibrantSwatch!!.rgb
            temp[1] = palette.darkVibrantSwatch!!.bodyTextColor
            temp[2] = palette.darkVibrantSwatch!!.titleTextColor
        } else if (palette.darkMutedSwatch != null) {
            temp[0] = palette.darkMutedSwatch!!.rgb
            temp[1] = palette.darkMutedSwatch!!.bodyTextColor
            temp[2] = palette.darkMutedSwatch!!.titleTextColor
        } else {
            temp[0] = ContextCompat.getColor(context, R.color.colorPrimary)
            temp[1] = ContextCompat.getColor(context, android.R.color.white)
            temp[2] = -0x1a1a1b
        }
        return temp
    }

    fun shareSongFile(items: ArrayList<Song>, position: Int) {
        val shareSongTrace = FirebasePerformance.getInstance().newTrace("share_song_trace")
        shareSongTrace.start()
        val share = Intent(Intent.ACTION_SEND)
        share.type = "audio/*"
        share.putExtra(Intent.EXTRA_STREAM,
                Uri.parse(items[position].path))
        context.startActivity(Intent.createChooser(share,
                context.getString(R.string.share_song_file)))

        val params = Bundle()
        val firebaseAnalytics = FirebaseAnalytics.getInstance(context)
        params.putString("song_title", items[position].name)
        firebaseAnalytics.logEvent("share_song", params)

        shareSongTrace.stop()
    }


    fun setRingtone(songID: Int){
        RingtoneManager.setActualDefaultRingtoneUri(
                context,
                RingtoneManager.TYPE_RINGTONE,
                getSongFileUri(songID ))
        Toast.makeText(context, R.string.Set_successfully, Toast.LENGTH_LONG).show()
    }

    fun getBitmapOfVector(@DrawableRes id: Int, height: Int, width: Int): Bitmap {
        var vectorDrawable: Drawable? = null
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            vectorDrawable = context.getDrawable(id)
        }
        vectorDrawable?.setBounds(0, 0, width, height)
        val bm = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bm)
        vectorDrawable?.draw(canvas)
        return bm
    }

    companion object {
        fun getAlbumArtUri(albumId: Long): Uri {
            return ContentUris.withAppendedId(Uri.parse("content://media/external/audio/albumart"), albumId)
        }

        fun getSongFileUri(songId: Int): Uri {
            return ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, songId.toLong())
        }
    }

}