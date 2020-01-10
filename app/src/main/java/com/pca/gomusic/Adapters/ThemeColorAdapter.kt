package com.pca.gomusic.Adapters

import android.app.Activity
import android.content.SharedPreferences
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatImageView
import androidx.cardview.widget.CardView
import androidx.core.app.ActivityCompat.recreate
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.RecyclerView
import com.pca.gomusic.Constant.Constant
import com.pca.gomusic.ModelClass.Theme
import com.pca.gomusic.R

class ThemeColorAdapter(private var activity: Activity, private var colors: ArrayList<Theme>):
        RecyclerView.Adapter<ThemeColorAdapter.MyViewHolder>() {

    private lateinit var sharedPreferences: SharedPreferences
    private var currentTheme: String? = null

    inner class MyViewHolder(var view: View) : RecyclerView.ViewHolder(view) {
        var colorHolderCard: CardView = view.findViewById(R.id.color_holder)
        var activeThemeMarker: AppCompatImageView = view.findViewById(R.id.active_theme_marker)

        init {
            sharedPreferences = PreferenceManager.getDefaultSharedPreferences(activity)
            currentTheme = sharedPreferences.getString(Constant.THEME, "default")
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val mainView = layoutInflater.inflate(R.layout.theme_color_holder, parent, false)
        return MyViewHolder(mainView)
    }

    override fun getItemCount(): Int {
        return colors.size
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
      //  val theme = colors[position]
        when (position) {
            0 -> {
                holder.colorHolderCard.setCardBackgroundColor(activity.resources.getColor(R.color.lightergray))
                if (currentTheme.equals("default")){
                    holder.activeThemeMarker.visibility = View.VISIBLE
                }else{
                    holder.activeThemeMarker.visibility = View.GONE
                }
            }
            1 -> {
                holder.colorHolderCard.setCardBackgroundColor(activity.resources.getColor(R.color.night))
                if (currentTheme.equals("night")){
                    holder.activeThemeMarker.visibility = View.VISIBLE
                }else{
                    holder.activeThemeMarker.visibility = View.GONE
                }
            }
        }


        holder.colorHolderCard.setOnClickListener {
            setTheme(position)
        }
    }




    private fun setTheme(code:Int){
        when (code) {
            0 -> {theme("default")}
            1 -> {theme("night")}
            2 -> {theme("cyran")}
            3 -> {theme("mimi")}
        }
    }

    private fun theme(name:String){
        val sharedPre = PreferenceManager.getDefaultSharedPreferences(activity)
        val editor = sharedPre.edit()
        editor.putString(Constant.THEME, name)
        editor.putBoolean(Constant.hasChanged, true)
        editor.apply()
        recreate(activity)
    }
}